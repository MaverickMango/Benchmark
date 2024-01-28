package root.script;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jgit.lib.Repository;
import org.junit.Test;
import org.refactoringminer.astDiff.actions.ASTDiff;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import root.analysis.CompilationUnitManipulator;
import root.entities.otherdataset.BugFixCommit;
import root.entities.ci.BugFunction;
import root.entities.otherdataset.CommitInfo;
import root.entities.benchmarks.Defects4JBug;
import root.util.BugBuilder;
import root.util.ConfigurationProperties;
import root.util.FileUtils;
import root.util.GitAccess;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class BugInfoUtilTest implements GitAccess {

    @Test
    public void bugFunctionExtractor() throws IOException {
        String bugInfo = "src/test/resources/BugFixInfo_total.csv";
        String total_unfixed ="Jsoup_4,Jsoup_88,Closure_114,Closure_99,Codec_6,Math_55,Math_54,Compress_26,Time_2,Jsoup_26,Time_5,Lang_19,Codec_10,Codec_8,Math_45,Math_26,Compress_8,Closure_27,Mockito_11,Closure_153,Math_17,Closure_170,Jsoup_9,Compress_1,Compress_44,Mockito_12,Time_1,Lang_28,Math_106,Compress_7,Closure_82,Closure_91,Closure_80,Jsoup_79,Compress_39,Math_14,Lang_4,Closure_65,Codec_14,JacksonCore_14,JacksonCore_4,Cli_29,Math_92,Compress_3,Compress_6,Lang_49,Closure_85,JacksonCore_6,Lang_64,Jsoup_27,Jsoup_85,Lang_31,Closure_61,Closure_133,Closure_59,Math_103,Closure_60,Closure_76,Closure_75,Lang_53,Closure_68,Closure_17,Math_23,Math_28,Closure_30,Lang_13,Lang_14,Lang_25,Closure_12,Closure_131,Closure_66,Closure_64,Closure_8,Closure_121,Math_60,Math_42,Time_16";
        List<String> total = List.of(total_unfixed.split(","));
        List<List<String>> d4jinfos = FileUtils.readCsv(bugInfo, true);
        Map<String, String> unfixed_bugInput = new HashMap<>();
        for (int i = 0; i < d4jinfos.size(); i ++) {
            String bugName = d4jinfos.get(i).get(2);
            String proj = bugName.split("_")[0];
            String id = bugName.split("_")[1];
            String bug_tag = proj + "_" + id;
            if (!bug_tag.equals("Compress_1"))
                continue;
            if (!total.contains(bug_tag)) {
                continue;
            }
            String fixingDir = "data/changesInfo/" + bug_tag + "/cleaned/fixing";
            if (FileUtils.notExists(fixingDir)) {
                FileUtils.writeToFile(bug_tag + "\n", "src/test/resources/uncleaned", true);
                continue;
            }
            String workingDir = "../bugs/" + bugName;
            Defects4JBug defects4JBug = new Defects4JBug(proj, id,  workingDir);
            Repository repository = defects4JBug.getGitRepository("b");
            String bugInducingCommit = d4jinfos.get(i).get(5);
            String indufingDir = "data/changesInfo/" + bug_tag + "/properties/modified_classes/inducing";

            //get changed method of inducing commit
            Set<MethodDeclaration> ori2bicMths = new HashSet<>();
            Set<MethodDeclaration> ori_mths = new HashSet<>();
            Set<Integer> ori_pos = new HashSet<>();
            Set<Integer> bic_pos = new HashSet<>();
            String inducingDiff = "data/changesInfo/" + bug_tag + "/patches/inducing.diff";
            //extract changed lines by diff file
            FileUtils.getPositionsOfDiff(FileUtils.readEachLine(inducingDiff), ori_pos, bic_pos, true);
            GitHistoryRefactoringMinerImpl gitHistoryRefactoringMiner = new GitHistoryRefactoringMinerImpl();
            Set<ASTDiff> astDiffs = gitHistoryRefactoringMiner.diffAtCommit(repository, bugInducingCommit);
            Set<String> srcs = new HashSet<>(), dst = new HashSet<>();
            for (ASTDiff astDiff :astDiffs) {
                boolean flag = astDiff.getSrcPath().contains("test") || astDiff.getSrcPath().endsWith("Test.java");
                if (flag)
                    continue;
                String srcContents = astDiff.getSrcContents();
                String dstContents = astDiff.getDstContents();
//                Set<Integer> positions = new HashSet<>();
//                List<Action> actions = astDiff.editScript.asList();
//                for (Action action :actions) {
//                    int pos = action.getNode().getPos();
//                    positions.add(pos);
//                }
                srcs.add(srcContents);
                dst.add(dstContents);
            }
            for (String srcContents :srcs) {
                CompilationUnitManipulator manipulator = new CompilationUnitManipulator(8);
                Set<MethodDeclaration> methods = manipulator.extractMethodByPos(srcContents.toCharArray(), ori_pos, true);
                ori_mths.addAll(methods);
            }
            for (String dstContents :dst) {
                CompilationUnitManipulator manipulator = new CompilationUnitManipulator(8);
                Set<MethodDeclaration> methods = manipulator.extractMethodByPos(dstContents.toCharArray(), bic_pos, true);
                ori2bicMths.addAll(methods);
            }
//            gitHistoryRefactoringMiner.detectAtCommit(repository, bugInducingCommit, new RefactoringHandler() {
//                @Override
//                public void handle(String commitId, List<Refactoring> refactorings) {
//                    for (Refactoring refctor :refactorings) {
//                        String name = refctor.getName();
//                    }
//                }
//            });
            //only consider that which is same with fixing function
            bic_pos = new HashSet<>();
            Set<Integer> fix_pos = new HashSet<>();
            String cleanedFixing = "data/changesInfo/" + bug_tag + "/patches/cleaned.fixing.diff";
            //extract changed lines by diff file
            FileUtils.getPositionsOfDiff(FileUtils.readEachLine(cleanedFixing), bic_pos, fix_pos, true);
            List<String> fixedFiles = FileUtils.findAllFilePaths(fixingDir, ".java");
            String buggyFile = "data/changesInfo/" + bug_tag + "/properties/modified_classes/inducing/";
            List<String> bicFiles = FileUtils.findAllFilePaths(buggyFile, ".java");
            List<String> fixedNames = fixedFiles.stream().map(s -> s.substring(s.lastIndexOf("/") + 1)).collect(Collectors.toList());
            Set<MethodDeclaration> bic_mths = new HashSet<>(), fix_mths = new HashSet<>();
            for (String bicFile :bicFiles) {
                boolean flag = bicFile.contains("test") || bicFile.endsWith("Test.java");
                if (flag)
                    continue;
                String bicName = bicFile.substring(bicFile.lastIndexOf("/") + 1);
                CompilationUnitManipulator manipulator = new CompilationUnitManipulator(8);
                char[] bic = FileUtils.readFileByChars(bicFile);
                bic_mths.addAll(manipulator.extractMethodByPos(bic, bic_pos, true));
                bic_mths = intersection(bic_mths, ori2bicMths);
                if (!fixedNames.contains(bicName)){
                    continue;
                }
                int idx = fixedNames.indexOf(bicName);
                String fixedFilePath = fixedFiles.get(idx);
                char[] fixed = FileUtils.readFileByChars(fixedFilePath);
                fix_mths.addAll(manipulator.extractMethodByPos(fixed, fix_pos, true));
            }
            ori_mths = intersection(ori_mths, bic_mths);
            if (bic_mths.isEmpty()) {
                FileUtils.writeToFile(bug_tag + "\n", "src/test/resources/noBuggy", true);
            }
            BugFunction bugFunction = new BugFunction();
            bugFunction.setOriginal(FileUtils.getStrOfIterable(ori_mths, "\n").toString());
            bugFunction.setBuggy(FileUtils.getStrOfIterable(bic_mths, "\n").toString());
            bugFunction.setFix(FileUtils.getStrOfIterable(fix_mths, "\n").toString());
            unfixed_bugInput.put(bug_tag.replace("_", "-"), FileUtils.bean2Json(bugFunction));
        }
        System.out.println("single function unfixed total len: " + unfixed_bugInput.size());
        StringBuilder builder = new StringBuilder("{\n");
        for (Map.Entry<String, String> entries : unfixed_bugInput.entrySet()) {
            builder.append(entries.getKey()).append(":")
                    .append(entries.getValue()).append(",");
        }
        builder.replace(builder.length() - 1, builder.length(), "}");
        FileUtils.writeToFile(FileUtils.jsonFormatter(builder.toString()), "src/test/resources/single-function-repair-unfixed.json", false);
    }


    private Set<MethodDeclaration> intersection(Set<MethodDeclaration> one, Set<MethodDeclaration> another) {
        if (another == null)
            return one;
        Set<MethodDeclaration> inter = new HashSet<>();
        List<String> names = another.stream().map(m -> m.getName().toString()).collect(Collectors.toList());
        for (MethodDeclaration md :one) {
            if (names.contains(md.getName().toString())) {
                inter.add(md);
            }
        }
        return inter;
    }

    @Test
    public void mergeBugInfos() {
        String baseDir = "/home/liumengjiao/Downloads/";
        String bugInfo = baseDir + "BugInfo.csv";
        String Fonte = baseDir + "Fonte-BIC.csv";
        String bugFixInfo = "src/test/resources/BugFixInfo.csv";
        String bugFixInfo_total = "src/test/resources/BugFixInfo_total.csv";

        List<List<String>> d4jinfos = FileUtils.readCsv(bugFixInfo, true);
        List<String> d4jinfos_Name = d4jinfos.stream().map(bug -> bug.get(2)).collect(Collectors.toList());
        List<List<String>> bugInfos = FileUtils.readCsv(bugInfo, true);
        List<String> bugInfos_Name = bugInfos.stream().map(bug -> bug.get(1) + "_buggy").collect(Collectors.toList());
        List<List<String>> FonteInfos = FileUtils.readCsv(Fonte, true);
        List<String> FonteInfos_Name = FonteInfos.stream().map(bug -> bug.get(0) + "_" + bug.get(1) + "_buggy").collect(Collectors.toList());
        Collection<?> difference = FileUtils.difference(FonteInfos_Name, d4jinfos_Name);

        List<BugFixCommit> Bugs = new ArrayList<>();
        int idx = 1;
        for (int index = 0; index < d4jinfos.size(); index ++, idx ++) {
            String bugName = d4jinfos.get(index).get(2);
            String proj = bugName.split("_")[0];
            String id = bugName.split("_")[1];
            String bug_tag = proj + "_" + id;
            String workingDir = "../bugs/" + bugName;
            Defects4JBug defects4JBug = new Defects4JBug(proj, id,  workingDir);
            Repository repository = defects4JBug.getGitRepository("b");
            BugFixCommit bugFixCommit = gitAccess.getBugFixCommit(bugName, String.valueOf(idx),
                    repository, d4jinfos.get(index).get(5), d4jinfos.get(index).get(3));
            String bugInduingCommit = bugFixCommit.getInducingCommit().getSha();
            StringBuilder stringBuilder = new StringBuilder();

            int i = bugInfos_Name.indexOf(bugName);
            assert i != -1;
            List<String> bugInfo_Note = bugInfos.get(i);
            assert bugInfo_Note.get(1).equals(bug_tag);
            String note = bugInfo_Note.get(2);
            if (FonteInfos_Name.contains(bugName)) {
                //检查是否BIC一致
                i = FonteInfos_Name.indexOf(bugName);
                assert i != -1;
                List<String> Fonte_Info = FonteInfos.get(i);
                assert bug_tag.equals(Fonte_Info.get(0) + "_" + Fonte_Info.get(1));
                String BIC = Fonte_Info.get(2);
                String source = Fonte_Info.get(3);
                if (!BIC.equals(bugInduingCommit)) {
                    //BIC不一致，如果BugInfo中的版本不对，就更改为Fonte的版本，否则以BugInfo中的版本为准
                    if (!note.equals("√") && !note.equals("。")) {
                        CommitInfo commitInfo = gitAccess.getCommitInfo(repository, BIC);
                        bugFixCommit.setInducingCommit(commitInfo);
                    }
                }
            } else if (!note.equals("√") && !note.equals("。")) {
                //如果BugInfo中的版本还是不对，就删除这个bug
                idx --;
                continue;
            }
            String buginfo = bugFixCommit.getBugId() + "," + "defects4j" +
                    "," + bugName +
                    "," + bugFixCommit.getFixedCommit().getSha() +
                    "," + bugFixCommit.getBuggyCommit().getSha() +
                    "," + bugFixCommit.getInducingCommit().getSha() +
                    "," + bugFixCommit.getOriginalCommit().getSha() + "\n";
            stringBuilder.append(buginfo);
            FileUtils.writeToFile(stringBuilder.toString(), bugFixInfo_total, true);
            Bugs.add(bugFixCommit);
        }
        for (Object one :difference) {
            String name = (String) one;
            int i = FonteInfos_Name.indexOf(name);
            List<String> Fonte_Info = FonteInfos.get(i);
            String proj = Fonte_Info.get(0);
            String id = Fonte_Info.get(1);
            String BIC = Fonte_Info.get(2);
            String bug_tag = proj + "_" + id;
            String workingDir = "../bugs/" + bug_tag + "_buggy";
            Defects4JBug defects4JBug = new Defects4JBug(proj, id,  workingDir);
            Repository repository = defects4JBug.getGitRepository("b");
            try {
                BugFixCommit bugFixCommit = gitAccess.getBugFixCommit(bug_tag + "_buggy", String.valueOf(idx),
                        repository, BIC, defects4JBug.getFixingCommit());
                StringBuilder stringBuilder = new StringBuilder();
                String buginfo = bugFixCommit.getBugId() + "," + "defects4j" +
                        "," + bug_tag + "_buggy" +
                        "," + bugFixCommit.getFixedCommit().getSha() +
                        "," + bugFixCommit.getBuggyCommit().getSha() +
                        "," + bugFixCommit.getInducingCommit().getSha() +
                        "," + bugFixCommit.getOriginalCommit().getSha() + "\n";
                stringBuilder.append(buginfo);
                FileUtils.writeToFile(stringBuilder.toString(), bugFixInfo_total, true);
                idx ++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Test
    public void writeToAnother() {
        String bugFixInfo = "src/test/resources/BugFixInfo.csv";
        String bugFixInfo_total = "src/test/resources/BugFixInfo_total.csv";
        String Another = "src/test/resources/Another.csv";
        List<List<String>> d4jinfos = FileUtils.readCsv(bugFixInfo, true);
        List<String> d4jinfos_Name = d4jinfos.stream().map(bug -> bug.get(2)).collect(Collectors.toList());
        List<List<String>> d4jinfosTotal = FileUtils.readCsv(bugFixInfo_total, true);
        List<String> d4jinfosTotal_Name = d4jinfosTotal.stream().map(bug -> bug.get(2)).collect(Collectors.toList());
        Collection<?> difference = FileUtils.difference(d4jinfosTotal_Name, d4jinfos_Name);

        int idx = 1;
        for (int index = 0; index < d4jinfos.size(); index ++, idx ++) {
            String bugName = d4jinfos.get(index).get(2);
            String proj = bugName.split("_")[0];
            String id = bugName.split("_")[1];
            String bug_tag = proj + "_" + id;
            String workingDir = "../bugs/" + bugName;
            Defects4JBug defects4JBug = new Defects4JBug(proj, id, workingDir);
            Repository repository = defects4JBug.getGitRepository("b");
            String bugInduingCommit = d4jinfos.get(index).get(5);
            if (!d4jinfosTotal_Name.contains(bugName)) {
                continue;
            }

            int i = d4jinfosTotal_Name.indexOf(bugName);
            assert i != -1;
            List<String> d4jinfosTotal_info = d4jinfosTotal.get(i);
            assert d4jinfosTotal_info.get(2).equals(bugName);
            String BIC = d4jinfosTotal_info.get(5);
            if (!BIC.equals(bugInduingCommit)) {
                FileUtils.writeToFile(proj + "," + id + "," + BIC + "\n", Another, true);
            }
        }
        for (Object one :difference) {
            String bugName = (String) one;
            String proj = bugName.split("_")[0];
            String id = bugName.split("_")[1];
            int i = d4jinfosTotal_Name.indexOf(bugName);
            assert i != -1;
            List<String> d4jinfosTotal_info = d4jinfosTotal.get(i);
            assert d4jinfosTotal_info.get(2).equals(bugName);
            String BIC = d4jinfosTotal_info.get(5);
            FileUtils.writeToFile(proj + "," + id + "," + BIC + "\n", Another, true);
        }
    }

    @Test
    public void bugInfoExtractor() {
        String filePath = "src/test/resources/Another.csv";//proj,id,inducing_commit
        List<List<String>> d4jinfos = FileUtils.readCsv(filePath, true);
        String bugInfo = "src/test/resources/BugFixInfo_total.csv";
        List<List<String>> infos = FileUtils.readCsv(bugInfo, true);
        int idx = Integer.parseInt(infos.get(infos.size() - 1).get(0)) + 1;
        List<String> bugNames = infos.stream().map(bug -> bug.get(2)).collect(Collectors.toList());
        for (int i = 0; i < d4jinfos.size(); i ++) {
            List<String> bug = d4jinfos.get(i);
            String proj = bug.get(0);
            String id = bug.get(1);
            String bug_tag = proj + "_" + id;
            String bugName = bug_tag + "_buggy";
            String workingDir = "../bugs/" + bugName;
            Defects4JBug defects4JBug = new Defects4JBug(proj, id,  workingDir);
            Repository repository = defects4JBug.getGitRepository("b");

            String bugFixingCommit = defects4JBug.getFixingCommit();
            String bugInduingCommit = bug.get(2);

            // get diff infos
            String fixingDiff = gitAccess.diff(repository, bugFixingCommit);
            String inducingDiff = gitAccess.diff(repository, bugInduingCommit);
            String fixingDiffDir = "data/changesInfo/" + bug_tag + "/patches/fixing.diff";
            String inducingDiffDir = "data/changesInfo/" + bug_tag + "/patches/inducing.diff";
            String changesInfoDir = "data/changesInfo/" + bug_tag + "/info.txt";
            String bugBuggyCommit = gitAccess.getCommit(repository, bugFixingCommit).getParent(0).getName();
            String bugOriginalCommit = gitAccess.getCommit(repository, bugInduingCommit).getParent(0).getName();
            StringBuilder stringBuilder = new StringBuilder();
            if (fixingDiff != null && inducingDiff != null) {
                BugFixCommit bugFixCommit = gitAccess.getBugFixCommit(bugName, String.valueOf(i),
                        repository, bugInduingCommit, bugFixingCommit);
                FileUtils.writeToFile(bugFixCommit.toString(), changesInfoDir, false);
                FileUtils.writeToFile(fixingDiff, fixingDiffDir, false);
                FileUtils.writeToFile(inducingDiff, inducingDiffDir, false);
                String buginfo = (i + idx) + "," + "defects4j" +
                        "," + bugName +
                        "," + bugFixingCommit +
                        "," + bugBuggyCommit +
                        "," + bugInduingCommit +
                        "," + bugOriginalCommit + "\n";
                stringBuilder.append(buginfo);
            }
            if (bugNames.contains(bugName)) {
                List<List<String>> collect = infos.stream().filter(b -> b.get(2).equals(bugName)).collect(Collectors.toList());
                assert collect.size() == 1;
                String inducing = collect.get(0).get(5);
//                if (inducing.equals(bugInduingCommit)) {
//                    continue;
//                }
            }
//            FileUtils.writeToFile(stringBuilder.toString(), bugInfo, true);

            //get modified changes
            String fileDir = defects4JBug.getDataDir() + bug_tag;
            BugBuilder.getDiffInfo(repository, bugOriginalCommit, bugInduingCommit, "inducing", fileDir);
            BugBuilder.getDiffInfo(repository, bugBuggyCommit, bugFixingCommit, "fixing", fileDir);

            //get mappings: f2i,i2o,f2b
            gitAccess.getFileStatDiffBetweenCommits(defects4JBug.getWorkingDir(), bugFixingCommit, bugInduingCommit
                    , "data/changesInfo/" + proj + "_" + id + "/properties/mappings/f2i");
            gitAccess.getFileStatDiffBetweenCommits(defects4JBug.getWorkingDir(), bugInduingCommit, bugOriginalCommit
                    , "data/changesInfo/" + proj + "_" + id + "/properties/mappings/i2o");
            gitAccess.getFileStatDiffBetweenCommits(defects4JBug.getWorkingDir(), bugFixingCommit, bugBuggyCommit
                    , "data/changesInfo/" + proj + "_" + id + "/properties/mappings/f2b");
            gitAccess.getFileStatDiffBetweenCommits(defects4JBug.getWorkingDir(), bugFixingCommit, bugOriginalCommit
                    , "data/changesInfo/" + proj + "_" + id + "/properties/mappings/f2o");

            //get defects4j patches
            String patches = "data/changesInfo/" + proj + "_" + id + "/patches/";
            String srcPatch = ConfigurationProperties.getProperty("defects4j") + "/framework/projects/" + proj + "/patches/" + id + ".src.patch";
            String testPatch = ConfigurationProperties.getProperty("defects4j") + "/framework/projects/" + proj + "/patches/" + id + ".test.patch";
            FileUtils.copy(new File(srcPatch), new File(patches));
            FileUtils.copy(new File(testPatch), new File(patches));

            //get sources of modified classes
            try {
                String mappingFile = "data/changesInfo/" + proj + "_" + id + "/properties/mappings/";
                getMappingFiles(defects4JBug, mappingFile + "f2b", bugFixingCommit, "fixing");
                getMappingFiles(defects4JBug, mappingFile + "f2b", bugBuggyCommit, "buggy");
                getMappingFiles(defects4JBug, mappingFile + "f2i", bugInduingCommit, "inducing");
                getMappingFiles(defects4JBug, mappingFile + "f2o", bugOriginalCommit, "original");
            } catch (Exception e) {
                System.err.println(bugName);
                e.printStackTrace();
            }
        }
    }

    boolean getMappingFiles(Defects4JBug defects4JBug, String mappingFile, String commitId, String version) {
        String modifiedClasses = "data/changesInfo/" + defects4JBug.getProj() + "_" + defects4JBug.getId() + "/properties/modified_classes/";
        String srcClasses = ConfigurationProperties.getProperty("defects4j") + "/framework/projects/" + defects4JBug.getProj() + "/modified_classes/" + defects4JBug.getId() + ".src";
        String testMethods = "/home/liumengjiao/Desktop/vbaprinfo/d4j_bug_info/failed_tests/" + defects4JBug.getProj().toLowerCase() + "/" + defects4JBug.getId() + ".txt";
        List<List<String>> b2iSrc = gitAccess.getF2i(mappingFile, FileUtils.readEachLine(srcClasses));
        List<List<String>> b2iTest = gitAccess.getF2i(mappingFile, FileUtils.readEachLine(testMethods));
        boolean checkoutf = gitAccess.checkoutf(defects4JBug.getWorkingDir(), commitId);
        if (!checkoutf) {
            return false;
        }
        for (List<String> b2i : b2iSrc) {
            FileUtils.copy(new File(defects4JBug.getWorkingDir() + "/" + b2i.get(2)), new File(modifiedClasses + version + "/" + b2i.get(2)));
        }
        for (List<String> b2i : b2iTest) {
            FileUtils.copy(new File(defects4JBug.getWorkingDir() + "/" + b2i.get(2)), new File(modifiedClasses + version + "/" + b2i.get(2)));
        }
//                FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "diff -r -u original/ buggy/ > " + "../../patches/o2b.diff 2>&1"}, modifiedClasses, 300, null);
        return true;
    }

    @Test
    public void comparison() {
        String bugFixInfo_total = "src/test/resources/BugFixInfo_total.csv";
        List<List<String>> d4jinfos = FileUtils.readCsv(bugFixInfo_total, true);
        List<String> d4jinfos_Name = d4jinfos.stream().map(bug -> bug.get(2)).collect(Collectors.toList());
        String repair_FL = "src/test/resources/bugWithRepairTools_NonFL.csv";
        List<List<String>> repairFLinfos = FileUtils.readCsv(repair_FL, true);
        List<String> repairFL_Name = repairFLinfos.stream().map(bug -> bug.get(0) + "_buggy").collect(Collectors.toList());

        int idx = 1;
        List<String> unfixed = new ArrayList<>();
        List<String> correct = new ArrayList<>();
        List<String> plausible = new ArrayList<>();
        for (int index = 0; index < d4jinfos.size(); index ++, idx ++) {
            String bugName = d4jinfos.get(index).get(2);
            String proj = bugName.split("_")[0];
            String id = bugName.split("_")[1];
            String bug_tag = proj + "_" + id;

            unfixed.add(bug_tag);
            continue;
//            if (!repairFL_Name.contains(bugName)) {
//                unfixed.add(bug_tag);
//                continue;
//            }
//            int i = repairFL_Name.indexOf(bugName);
//            assert i != -1;
//            List<String> bugInRepair = repairFLinfos.get(i);
//            assert bugInRepair.get(0).equals(bug_tag);
//            if (!bugInRepair.contains("correct")) {
//                if (!bugInRepair.contains("plausible")) {
//                    unfixed.add(bug_tag);
//                } else {
//                    plausible.add(bug_tag);
//                }
//            } else {
//                correct.add(bug_tag);
//            }
        }
        List<List<String>> res = new ArrayList<>();
        unfixed.add(0, String.valueOf(unfixed.size()));
        unfixed.add(0, "Unfixed");
        res.add(unfixed);
        correct.add(0, String.valueOf(correct.size()));
        correct.add(0, "Correct");
        res.add(correct);
        plausible.add(0, String.valueOf(plausible.size()));
        plausible.add(0, "Plausible");
        res.add(plausible);
        FileUtils.writeCsv(res, "src/test/resources/RepairInfo_Model.csv", false);
    }
}
