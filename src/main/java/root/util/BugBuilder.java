package root.util;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.Tree;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.astDiff.actions.ASTDiff;
import root.analysis.ASTManipulator;
import root.analysis.RefactoringMiner;
import root.analysis.StringFilter;
import root.bean.benchmarks.Defects4JBug;
import root.bean.ci.*;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BugBuilder implements GitAccess {

    public static void buildCIBug(CIBug ciBug, String dataDir) {
        if (!(ciBug instanceof Defects4JBug)) {
            return;
        }
        Repository repository = ((Defects4JBug) ciBug).getGitRepository("b");
        String fixingCommit = ciBug.getOriginalFixingCommit();
        String inducingCommit = ciBug.getInducingCommit();
        String originalCommit = gitAccess.getNextCommit(repository, inducingCommit, false);

        //0. extract diff file
        String inducingDiffFile = dataDir + "/" + ciBug.getBugName() + "/patches/inducing.diff";
        if (FileUtils.notExists(inducingDiffFile)) {
            String inducingDiff = gitAccess.diff(repository, inducingCommit);
            FileUtils.writeToFile(inducingDiff, inducingDiffFile, false);
        }
        String fixingDiffFile = dataDir + "/" + ciBug.getBugName() + "/patches/cleaned.fixing.diff";
        if (true) {//FileUtils.notExists(fixingDiffFile)
            String modified_classes = dataDir + "/" + ciBug.getBugName() + "/properties/modified_classes/inducing/";
            RefactoringMiner miner = new RefactoringMiner();
            Set<ASTDiff> astDiffs = miner.diffAtCommit(repository, inducingCommit);
            for (ASTDiff astDiff :astDiffs) {
                FileUtils.writeToFile(astDiff.getDstContents(), modified_classes + astDiff.getDstPath(), false);
            }
            cleanedOneByOne(ciBug.getBugName(), dataDir);
        }
        List<String> fixingDiff = FileUtils.readEachLine(fixingDiffFile);
        fixingDiff = fixingDiff.stream().filter(line -> !line.startsWith("Only in")).collect(Collectors.toList());

        List<PatchDiff> patchDiffs = new ArrayList<>();
        Actions actions = new Actions();
        //1. extract inducing infos
        try {
            extractChangesFromCommits(repository, originalCommit, inducingCommit, patchDiffs, actions);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ciBug.setInducingChanges(patchDiffs);
        ciBug.setInducingType(actions);

        //2. extract fixing infos
        patchDiffs = new ArrayList<>();
        actions = new Actions();
        try {
            String fixingDir = dataDir + "/" + ciBug.getBugName() + "/cleaned/fixing/";
            String inducingDir = dataDir + "/" + ciBug.getBugName() + "/properties/modified_classes/inducing/";
            extractChangesFromDirs(repository, new File(inducingDir).getAbsolutePath(),
                    new File(fixingDir).getAbsolutePath(), patchDiffs, actions,
                    FileUtils.getStrOfIterable(fixingDiff, "\n").toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        ciBug.setFixingChanges(patchDiffs);
        ciBug.setFixingType(actions);

        //3. failing test
        ((Defects4JBug) ciBug).switchAndClean(repository, inducingCommit, "inducing", "D4J_CLEANED_" + ciBug.getBugName());
        //extract message
        String failing_tests = dataDir + "/" + ciBug.getBugName() + "/properties/failing_tests/inducing";
        List<FailedTest> failedTests = extractFailedTests(failing_tests);
        ciBug.setTriggerTests(failedTests);
    }

    public static void extractChangesFromCommits(Repository repository, String srcCommit, String dstCommit, List<PatchDiff> patchDiffs, Actions actions) throws Exception {
        //get changed methods
        Set<String> dst_mths = new HashSet<>();
        Set<String> src_mths = new HashSet<>();

        //extract changed files first, except add and delete classes.
        RefactoringMiner gitHistoryRefactoringMiner = new RefactoringMiner();
        Set<ASTDiff> astDiffs = gitHistoryRefactoringMiner.diffAtCommit(repository, dstCommit);
        for (ASTDiff astDiff :astDiffs) {
            PatchDiff patchDiff = getInfoFromASTDiff(repository, srcCommit, dstCommit,
                    src_mths, dst_mths, true, astDiff, null);
            if (patchDiff == null)
                continue;
            patchDiffs.add(patchDiff);
        }
        //function actions
        Set<String> addSet = (Set<String>) FileUtils.difference(dst_mths, src_mths);
        actions.setAddFunctions(addSet.stream().collect(Collectors.toList()));
        Set<String> deleteSet = (Set<String>) FileUtils.difference(src_mths, dst_mths);
        actions.setDeleteFunctions(deleteSet.stream().collect(Collectors.toList()));

        //add and delete classes
        Set<String> filePathsBefore = new LinkedHashSet<>();
        Set<String> filePathsCurrent = new LinkedHashSet<>();
        Map<String, String> renamedFilesHint = new HashMap<>();
        gitHistoryRefactoringMiner.fileTreeDiff(repository, gitAccess.getCommit(repository, srcCommit),
                gitAccess.getCommit(repository, dstCommit), filePathsBefore, filePathsCurrent, renamedFilesHint);
        addSet = (Set<String>) FileUtils.difference(filePathsCurrent, filePathsBefore);
        List<String> temp = addSet.stream().filter(n -> !n.contains("test") && !n.endsWith("Test.java")).collect(Collectors.toList());
        actions.setAddClasses(temp);
        deleteSet = (Set<String>) FileUtils.difference(filePathsBefore, filePathsCurrent);
        temp = deleteSet.stream().filter(n -> !n.contains("test") && !n.endsWith("Test.java")).collect(Collectors.toList());
        actions.setDeleteClasses(temp);
    }

    public static void extractChangesFromDirs(Repository repository, String srcDir, String dstDir,
                                              List<PatchDiff> patchDiffs, Actions actions, String diff) throws Exception {
        //get changed methods
        Set<String> dst_mths = new HashSet<>();
        Set<String> src_mths = new HashSet<>();

        //extract changed files first, except add and delete classes.
        RefactoringMiner gitHistoryRefactoringMiner = new RefactoringMiner();
        Set<ASTDiff> astDiffs = gitHistoryRefactoringMiner.diffAtDirectories(new File(srcDir), new File(dstDir));
        for (ASTDiff astDiff :astDiffs) {
            PatchDiff patchDiff = getInfoFromASTDiff(repository, null, null,
                    src_mths, dst_mths, true, astDiff, diff);
            if (patchDiff == null)
                continue;
            patchDiffs.add(patchDiff);
        }
        //function actions
        Set<String> addSet = (Set<String>) FileUtils.difference(dst_mths, src_mths);
        actions.setAddFunctions(addSet.stream().collect(Collectors.toList()));
        Set<String> deleteSet = (Set<String>) FileUtils.difference(src_mths, dst_mths);
        actions.setDeleteFunctions(deleteSet.stream().collect(Collectors.toList()));

        //add classes
        List<String> temp = new ArrayList<>();
        actions.setAddClasses(temp);
        actions.setDeleteClasses(temp);
    }

    private static PatchDiff getInfoFromASTDiff(Repository repository, String srcCommit, String dstCommit,
                                       Set<String> src_mths, Set<String> dst_mths,
                                       boolean onlyJavaSource, ASTDiff astDiff, String diff) {
        //for each changed files, mapping the line number with methods.
        boolean srcFlag = astDiff.getSrcPath().contains("test") || astDiff.getSrcPath().endsWith("Test.java");
        boolean dstFlag = astDiff.getDstPath().contains("test") || astDiff.getDstPath().endsWith("Test.java");
        boolean flag = srcFlag || dstFlag;
        if (onlyJavaSource && flag)// filter changes about test
            return null;
        Set<Integer> ori_pos = new HashSet<>();
        Set<Integer> bic_pos = new HashSet<>();
        StringFilter filter = new StringFilter(StringFilter.NOT_EQUALS);
        filter.addPattern(astDiff.getDstPath());
        String inducingDiff = diff == null ? gitAccess.diffWithFilter(repository, srcCommit, dstCommit, filter) : diff;
        List<String> classes = new ArrayList<>();
        classes.add(0, astDiff.getSrcPath());
        classes.add(1, astDiff.getDstPath());
        PatchDiff patchDiff = new PatchDiff("UPDATE", classes);
        patchDiff.setDiff(inducingDiff);

        Set<MethodDeclaration> methods;
        //extract changed lines by diff file
        FileUtils.getPositionsOfDiff(List.of(inducingDiff.split("\n")), ori_pos, bic_pos, true);
        List<NameList> changed = new ArrayList<>();
        List<Object> temp = Stream.of(ori_pos.toArray()).sorted().collect(Collectors.toList());
        List<String> names = temp.stream().map(String::valueOf).collect(Collectors.toList());
        changed.add(0, new NameList(names));
        temp = Stream.of(bic_pos.toArray()).sorted().collect(Collectors.toList());
        names = temp.stream().map(String::valueOf).collect(Collectors.toList());
        changed.add(1, new NameList(names));
        patchDiff.setChangedLines(changed);

        changed = new ArrayList<>();
        ASTManipulator manipulator = new ASTManipulator(8);
        String srcContents = astDiff.getSrcContents();
        methods = manipulator.extractMethodByPos(srcContents.toCharArray(), ori_pos, true);
        List<String> mths_sig = methods.stream().map(manipulator::getFunctionSig).collect(Collectors.toList());
        src_mths.addAll(mths_sig);
        changed.add(0, new NameList(mths_sig));
        String dstContents = astDiff.getDstContents();
        methods = manipulator.extractMethodByPos(dstContents.toCharArray(), bic_pos, true);
        mths_sig = methods.stream().map(manipulator::getFunctionSig).collect(Collectors.toList());
        dst_mths.addAll(mths_sig);
        changed.add(1, new NameList(mths_sig));
        patchDiff.setChangedFunctions(changed);

        List<Operation> operations = new ArrayList<>();
        for (Action action: astDiff.editScript.asList()) {
            Operation operation = new Operation();
            Tree tree = action.getNode();
            String name = action.getName();
            operation.setType(name);
            if (name.contains("insert")) {
                operation.setTo(tree.toString());
            }
            if (name.contains("delete")) {
                operation.setFrom(tree.toString());
            }
            operations.add(operation);
        }
        patchDiff.setOperations(operations);
        return patchDiff;
    }

    public static List<FailedTest> extractFailedTests(String failing_tests) {
        List<String> failings = FileUtils.readEachLine(failing_tests);
        List<FailedTest> failedTests = new ArrayList<>();
        FailedTest failedTest;
        for (int i = 0; i < failings.size(); i ++) {
            String line = failings.get(i);
            if (line.startsWith("--- ")) {
                failedTest = new FailedTest();
                String[] split = line.substring(4).split("::");
                failedTest.setTestClass(split[0]);
                if (split.length > 1)
                    failedTest.setTestFunction(split[1]);
                int j = 1;
                failedTest.setMessage("");
                for (; j < failings.size(); j++) {
                    line = failings.get(i + j);
                    if (j == 1) {
                        int splitPos = line.indexOf(":") == line.length() - 1 ? line.length() - 2 : line.indexOf(":");
                        if (splitPos < 0)
                            splitPos = line.length() - 1;
                        String exception = line.substring(0, splitPos).trim();
                        String message = line.substring(splitPos + 1).trim();
                        failedTest.setException(exception);
                        failedTest.setMessage(message);
                        break;
                    }
                    if (line.startsWith("Expected")) {
                        failedTest.setMessage(failedTest.getMessage() + line);
                    }
                    if (line.startsWith("Result")) {
                        failedTest.setMessage(failedTest.getMessage() + "\n" + line);
                    }
                }
                i += j - 1;
                failedTests.add(failedTest);
            }
        }
        return failedTests;
    }
    private static Set<String> intersection(Set<String> one, Set<String> another) {
        if (another == null)
            return one;
        Set<String> inter = new HashSet<>();
        for (String md :one) {
            if (another.contains(md)) {
                inter.add(md);
            }
        }
        return inter;
    }

    public static void cleanedOneByOne(String bugName, String dataDir) {
        // get diff infos
        String originalDir = dataDir + "/" + bugName + "/properties/modified_classes/inducing/";
        String fixingDir = dataDir + "/" + bugName + "/cleaned/fixing/";
        String inducingDir = dataDir + "/" + bugName + "/cleaned/inducing/";
        String fixingDiff = dataDir + "/" + bugName + "/patches/cleaned.fixing.diff";
        String inducingDiff = dataDir + "/" + bugName + "/patches/cleaned.inducing.diff";
        List<String> diffs = FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "diff -u -r " + originalDir + " " + inducingDir});
        FileUtils.writeToFile(FileUtils.getStrOfIterable(diffs, "\n").toString(), inducingDiff, false);
        diffs = FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "diff -u -r " + originalDir + " " + fixingDir});
        FileUtils.writeToFile(FileUtils.getStrOfIterable(diffs, "\n").toString(), fixingDiff, false);
    }

    public static void getDiffInfo(Repository repository, String srcCommit, String dstCommit, String version, String outputPath) {
        List<String> modifiedClasses = new ArrayList<>();
        String filePath = outputPath;
        RefactoringMiner miner = new RefactoringMiner();
        Set<ASTDiff> astDiffs = miner.diffAtCommit(repository, srcCommit, dstCommit);
        for (ASTDiff diff :astDiffs) {
            Tree root = diff.src.getRoot();
//            LineNumberReader reader = new LineNumberReader(new CharArrayReader("".toCharArray()));
            String path = diff.getSrcPath();
            modifiedClasses.add(path);
            List<Action> actions = diff.editScript.asList();
            String dir = filePath + "/actions/" + version + File.separator + path.substring(path.lastIndexOf(File.separator) + 1, path.indexOf("."));
            FileUtils.writeToFile(FileUtils.getStrOfIterable(actions, "\n").toString(), dir, false);
        }
        filePath = filePath + "/properties/modified_classes/" + version + ".txt";
        FileUtils.writeToFile(FileUtils.getStrOfIterable(modifiedClasses, "\n").toString(), filePath, false);
    }
}
