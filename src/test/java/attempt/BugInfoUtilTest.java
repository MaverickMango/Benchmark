package attempt;

import org.eclipse.jgit.lib.Repository;
import org.junit.Test;
import root.bean.BugFixCommit;
import root.bean.CommitInfo;
import root.benchmarks.Defects4JBug;
import root.util.ConfigurationProperties;
import root.util.FileUtils;
import root.util.GitAccess;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class BugInfoUtilTest implements GitAccess {

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
                    if (!note.equals("√")) {
                        CommitInfo commitInfo = gitAccess.getCommitInfo(repository, BIC);
                        bugFixCommit.setInducingCommit(commitInfo);
                    }
                }
            } else if (!note.equals("√")) {
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
            defects4JBug.getDiffInfo(repository, bugOriginalCommit, bugInduingCommit, "inducing");
            defects4JBug.getDiffInfo(repository, bugBuggyCommit, bugFixingCommit, "fixing");

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
}
