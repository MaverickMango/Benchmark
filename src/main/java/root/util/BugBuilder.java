package root.util;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.Tree;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.astDiff.actions.ASTDiff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.analysis.CompilationUnitManipulator;
import root.analysis.MethodManipulator;
import root.diff.RefactoringMiner;
import root.analysis.StringFilter;
import root.entities.benchmarks.Defects4JBug;
import root.entities.ci.*;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BugBuilder implements GitAccess {

    private static final Logger logger = LoggerFactory.getLogger(BugBuilder.class);

    public static void buildCIBug(BugWithHistory bugWithHistory, String dataDir, boolean originalFixing) {
        if (!(bugWithHistory instanceof Defects4JBug)) {
            return;
        }
        Repository repository = ((Defects4JBug) bugWithHistory).getGitRepository("b");
        String fixingCommit = bugWithHistory.getOriginalFixingCommit();
        String inducingCommit = bugWithHistory.getInducingCommit();
        String originalCommit = ((Defects4JBug) bugWithHistory).getOriginalCommit();
        String buggyCommit = ((Defects4JBug) bugWithHistory).getBuggyCommit();

        //0. extract diff file
        logger.info("extract diff file...");
        String inducingDiffFile = dataDir + "/" + bugWithHistory.getBugName() + "/patches/inducing.diff";
        if (true) {//FileUtils.notExists(inducingDiffFile)
            String inducingDiff = gitAccess.diff(repository, inducingCommit);
            FileUtils.writeToFile(inducingDiff, inducingDiffFile, false);
        }
        String fixingDiffFile = dataDir + "/" + bugWithHistory.getBugName() + "/patches/cleaned.fixing.diff";
        if (true) {//FileUtils.notExists(fixingDiffFile)
            String modified_classes = dataDir + "/" + bugWithHistory.getBugName() + "/properties/modified_classes/inducing/";
            RefactoringMiner miner = new RefactoringMiner();
            Set<ASTDiff> astDiffs = miner.diffAtCommit(repository, inducingCommit);
            for (ASTDiff astDiff :astDiffs) {
                FileUtils.writeToFile(astDiff.getDstContents(), modified_classes + astDiff.getDstPath(), false);
            }
            cleanedOneByOne(bugWithHistory.getBugName(), dataDir);
        }
        List<String> fixingDiff = FileUtils.readEachLine(fixingDiffFile);
        fixingDiff = fixingDiff.stream().filter(line -> !line.startsWith("Only in")).collect(Collectors.toList());

        List<PatchDiff> patchDiffs = new ArrayList<>();
        Actions actions = new Actions();
        //1. extract inducing infos
        logger.info("extract inducing infos...");
        try {
            extractChangesFromCommits(((Defects4JBug) bugWithHistory).getWorkingDir(), repository, originalCommit, inducingCommit, patchDiffs, actions);
        } catch (Exception e) {
            e.printStackTrace();
        }
        bugWithHistory.setInducingChanges(patchDiffs);
        bugWithHistory.setInducingType(actions);

        //2. extract fixing infos
        logger.info("extract fixing infos...");
        patchDiffs = new ArrayList<>();
        actions = new Actions();
        try {
            if (!originalFixing) {
                String fixingDir = dataDir + "/" + bugWithHistory.getBugName() + "/cleaned/fixing/";
                String inducingDir = dataDir + "/" + bugWithHistory.getBugName() + "/properties/modified_classes/inducing/";
                extractChangesFromDirs(repository, new File(inducingDir).getAbsolutePath(),
                        new File(fixingDir).getAbsolutePath(), patchDiffs, actions,
                        FileUtils.getStrOfIterable(fixingDiff, "\n").toString());
            } else {
                extractChangesFromCommits(((Defects4JBug) bugWithHistory).getWorkingDir(), repository, buggyCommit, fixingCommit, patchDiffs, actions);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        bugWithHistory.setFixingChanges(patchDiffs);
        bugWithHistory.setFixingType(actions);

        //3. failing test
        logger.info("extract failing test...");
        List<FailedTest> failedTests = new ArrayList<>();
        try {
            String failing_tests;
            if (!originalFixing) {
                ((Defects4JBug) bugWithHistory).switchAndClean(repository, inducingCommit, "inducing", "D4J_CLEANED_" + bugWithHistory.getBugName());
                failing_tests = dataDir + "/" + bugWithHistory.getBugName() + "/properties/failing_tests/inducing";
            } else {
                failing_tests = ConfigurationProperties.getProperty("defects4j") + "/framework/projects/"
                        + ((Defects4JBug) bugWithHistory).getProj() + "/trigger_tests/" + ((Defects4JBug) bugWithHistory).getId();
            }
            //extract message
            failedTests = extractFailedTests(failing_tests);
        } catch (Exception e) {
            e.printStackTrace();
        }
        bugWithHistory.setTriggerTests(failedTests);


        //4. get method des if $originalFixing
        if (originalFixing) {
            logger.info("get method descriptors for originalFixing...");
            String d4JFix = ((Defects4JBug) bugWithHistory).getD4JFix();
            boolean res = ((Defects4JBug) bugWithHistory).checkAndCompile("f");
            String d4jinfoDir = ConfigurationProperties.getProperty("d4jinfo");
            if (d4jinfoDir != null ){
                d4jinfoDir += ((Defects4JBug) bugWithHistory).getProj().toLowerCase() + File.separator + ((Defects4JBug) bugWithHistory).getId() + ".txt";
            }
            List<String> path = FileUtils.readEachLine(d4jinfoDir);
            List<PatchDiff> fixingChanges = bugWithHistory.getFixingChanges();
            List<String> functionAdded = bugWithHistory.getFixingType().getAddFunctions().getQualifiedNames();
            List<String> inducing_functionAdded = bugWithHistory.getInducingType().getAddFunctions().getQualifiedNames();
            boolean regression = false;
            List<String> descriptors = new ArrayList<>();
            for (PatchDiff diff :fixingChanges) {
                List<String> functionsChanges = diff.getChangedFunctions().get(1).getQualifiedNames();
                List<String> changed = functionsChanges.stream().filter(f -> !functionAdded.contains(f)).collect(Collectors.toList());
                List<String> inducing_changed = functionsChanges.stream().filter(f -> !inducing_functionAdded.contains(f)).collect(Collectors.toList());
                boolean flag = !inducing_changed.isEmpty();
                List<String> tmp = FileUtils.getDescriptor(changed,
                        path.get(1), ((Defects4JBug) bugWithHistory).getWorkingDir());
                if (!tmp.isEmpty() && flag) {
                    tmp = tmp.stream().map(f -> f + ":regression").collect(Collectors.toList());
                    regression = true;
                }
                descriptors.addAll(tmp);
            }
            bugWithHistory.setRegression(regression ? regression : !descriptors.isEmpty());
            bugWithHistory.setPatchChangedMths(new NameList(descriptors));
        }
    }

    public static void extractChangesFromCommits(String workingDir, Repository repository, String srcCommit, String dstCommit, List<PatchDiff> patchDiffs, Actions actions) throws Exception {
        //get changed methods
        Set<String> dst_mths = new HashSet<>();
        Set<String> src_mths = new HashSet<>();

        Set<String> filePathsBefore = new LinkedHashSet<>();
        Set<String> filePathsCurrent = new LinkedHashSet<>();
        Map<String, String> renamedFilesHint = new HashMap<>();
        RefactoringMiner gitHistoryRefactoringMiner = new RefactoringMiner();
        gitHistoryRefactoringMiner.fileTreeDiff(repository, gitAccess.getCommit(repository, srcCommit),
                gitAccess.getCommit(repository, dstCommit), filePathsBefore, filePathsCurrent, renamedFilesHint);

        //extract changed files first, except add and delete classes.
        Set<String> intersection = new HashSet<>(filePathsBefore);
        intersection.retainAll(filePathsCurrent);
        for (String path :intersection) {
            PatchDiff patchDiff = getInfoFromASTDiff(repository, srcCommit, dstCommit,
                    src_mths, dst_mths, true, path, path);
            if (patchDiff == null)
                continue;
            patchDiffs.add(patchDiff);
        }
//        List<String> changedFiles = gitAccess.getFileStatDiffBetweenCommits(workingDir, srcCommit, dstCommit);
//        for (String m_p :changedFiles) {
//            String[] split = m_p.split("\t");
//            String filePath = split[1];
//            if (split[0].equals("M")) {
//            }
//            if (split[0].startsWith("R")) {
//
//            }
//            if (split[0])
//        }
//        Set<ASTDiff> astDiffs = gitHistoryRefactoringMiner.diffAtCommit(repository, dstCommit);
//        for (ASTDiff astDiff :astDiffs) {
//            PatchDiff patchDiff = getInfoFromASTDiff(repository, srcCommit, dstCommit,
//                    src_mths, dst_mths, true, astDiff, null);
//            if (patchDiff == null)
//                continue;
//            patchDiffs.add(patchDiff);
//        }
        //function actions
        Set<String> addSet = (Set<String>) FileUtils.difference(dst_mths, src_mths);
        actions.setAddFunctions(addSet.stream().collect(Collectors.toList()));
        Set<String> deleteSet = (Set<String>) FileUtils.difference(src_mths, dst_mths);
        actions.setDeleteFunctions(deleteSet.stream().collect(Collectors.toList()));

        //add and delete classes
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
                                                boolean onlyJavaSource, String srcPath, String dstPath) {
        //for each changed files, mapping the line number with methods.
        boolean srcFlag = srcPath.contains("test") || srcPath.endsWith("Test.java");
        boolean dstFlag = dstPath.contains("test") || dstPath.endsWith("Test.java");
        boolean flag = srcFlag || dstFlag;
        if (onlyJavaSource && flag)// filter changes about test
            return null;
        Set<Integer> ori_pos = new HashSet<>();
        Set<Integer> bic_pos = new HashSet<>();
        StringFilter filter = new StringFilter(StringFilter.NOT_EQUALS);
        filter.addPattern(dstPath);
        String inducingDiff = gitAccess.diffWithFilter(repository, srcCommit, dstCommit, filter);
        List<String> classes = new ArrayList<>();
        classes.add(0, srcPath);
        classes.add(1, dstPath);
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
        CompilationUnitManipulator manipulator = new CompilationUnitManipulator(8);
        String srcContents = gitAccess.getFileAtCommit(repository, srcCommit, srcPath);
        methods = manipulator.extractMethodByPos(srcContents.toCharArray(), ori_pos, true);
        MethodManipulator methodManipulator = new MethodManipulator();
        List<String> mths_sig = methods.stream().map(methodManipulator::getFunctionSig).collect(Collectors.toList());
        src_mths.addAll(mths_sig);
        changed.add(0, new NameList(mths_sig));
        String dstContents = gitAccess.getFileAtCommit(repository, dstCommit, dstPath);
        methods = manipulator.extractMethodByPos(dstContents.toCharArray(), bic_pos, true);
        mths_sig = methods.stream().map(methodManipulator::getFunctionSig).collect(Collectors.toList());
        dst_mths.addAll(mths_sig);
        changed.add(1, new NameList(mths_sig));
        patchDiff.setChangedFunctions(changed);

        List<Operation> operations = new ArrayList<>();
        RefactoringMiner gitHistoryRefactoringMiner = new RefactoringMiner();
        Set<ASTDiff> astDiffs = gitHistoryRefactoringMiner.diffBetweenContents(srcPath, dstPath, srcContents, dstContents);
        for (ASTDiff astDiff: astDiffs) {
            for (Action action : astDiff.editScript.asList()) {
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
        }
        patchDiff.setOperations(operations);
        return patchDiff;
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
        CompilationUnitManipulator manipulator = new CompilationUnitManipulator(8);
        String srcContents = astDiff.getSrcContents();
        methods = manipulator.extractMethodByPos(srcContents.toCharArray(), ori_pos, true);
        MethodManipulator methodManipulator = new MethodManipulator();
        List<String> mths_sig = methods.stream().map(methodManipulator::getFunctionSig).collect(Collectors.toList());
        src_mths.addAll(mths_sig);
        changed.add(0, new NameList(mths_sig));
        String dstContents = astDiff.getDstContents();
        methods = manipulator.extractMethodByPos(dstContents.toCharArray(), bic_pos, true);
        mths_sig = methods.stream().map(methodManipulator::getFunctionSig).collect(Collectors.toList());
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
                for (; i + j < failings.size(); j++) {
                    line = failings.get(i + j);
                    if (j == 1) {
                        int splitPos = line.indexOf(":") == line.length() - 1 ? line.length() - 2 : line.indexOf(":");
                        if (splitPos < 0) {
                            splitPos = line.length();
                        } else {
                            String message = line.substring(splitPos + 1).trim();
                            failedTest.setMessage(message);
                        }
                        String exception = line.substring(0, splitPos).trim();
                        failedTest.setException(exception);
                    }
                    if (line.startsWith("Expected")) {
                        failedTest.setMessage(failedTest.getMessage() + line);
                    }
                    if (line.startsWith("Result")) {
                        failedTest.setMessage(failedTest.getMessage() + "\n" + line);
                    }
                    if (line.startsWith("\tat")) {
                        String[] split1 = failedTest.getTestClass().split("\\.");
                        String regex = "\\(" + split1[split1.length - 1] + "\\.java:(\\d+)\\)";
                        Pattern pattern = Pattern.compile(regex);
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.find()) {
                            String lineNumber = matcher.group(1);
                            String temp = failings.get(j + 1);
                            if (temp.endsWith("invoke0(Native Method)") || !pattern.matcher(temp).find()) {
                                failedTest.setLineNumber(lineNumber);
                                break;
                            }
                        }
                    }
                }
                i += j - 1;
                failedTests.add(failedTest);
            }
        }
        return failedTests;
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
