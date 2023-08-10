package root.benchmarks;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.Tree;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.refactoringminer.astDiff.actions.ASTDiff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.analysis.ASTManipulator;
import root.analysis.RefactoringMiner;
import root.util.*;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Defects4JBug implements GitAccess {
    /*
    d4j check buggy项目时做的事情
    1. 根据本地的project-repo克隆目录到制定文件夹
    2. 重新git init添加用户信息/vcs信息/config信息
    3. 排除flaky/broken测试：根据项目中存放的每个bug对应的commit当中的失败测试文件（failing-tests/dependant-tests/random-failed-tests）
    4. (这里默认有default.properties文件以及build.xml文件)写入defects4j.build.properties文件(在第三步之前，需要直接知道项目的信息，然后直接在这里写入)
    5. 在修复版本应用src补丁->转换到buggy版本的源代码（此时对应的是修复版本的源代码）
    6. 比较fixing commit到before-fixing commit的差异，应用在当前版本（给当前buggy版本增加trigger测试）
     */

    final Logger logger = LoggerFactory.getLogger(Defects4JBug.class);
    final String d4jCmd = ConfigurationProperties.getProperty("defects4j") + "/framework/bin/defects4j";
    final Integer timeoutSecond = ConfigurationProperties.getPropertyInt("d4jtimeoutsecond");
    private String proj;
    private String id;
    private String workingDir;
    private String fixingCommit;
    private String buggyCommit;
    private String inducingCommit;
    private String originalCommit;

    public Defects4JBug(String proj, String id, String workingDir) {
        this.proj = proj;
        this.id = id;
        this.workingDir = workingDir;
    }

    public Defects4JBug(String proj, String id, String workingDir, String fixingCommit, String buggyCommit, String inducingCommit, String originalCommit) {
        this.proj = proj;
        this.id = id;
        this.workingDir = workingDir;
        this.fixingCommit = fixingCommit;
        this.buggyCommit = buggyCommit;
        this.inducingCommit = inducingCommit;
        this.originalCommit = originalCommit;
    }

    public void setProj(String proj) {
        this.proj = proj;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public String getFixingCommit() {
        if (fixingCommit == null) {
            return fixingCommit = getFixedCommit(getGitRepository("fix"), false).getName();
        }
        return fixingCommit;
    }

    public void setFixingCommit(String fixingCommit) {
        this.fixingCommit = fixingCommit;
    }

    public String getBuggyCommit() {
        if (buggyCommit == null) {
            return buggyCommit = getBuggyCommit(getGitRepository("buggy"), false).getName();
        }
        return buggyCommit;
    }

    public void setBuggyCommit(String buggyCommit) {
        this.buggyCommit = buggyCommit;
    }

    public String getInducingCommit() {
        return inducingCommit;
    }

    public void setInducingCommit(String inducingCommit) {
        this.inducingCommit = inducingCommit;
    }

    public String getOriginalCommit() {
        return originalCommit;
    }

    public void setOriginalCommit(String originalCommit) {
        this.originalCommit = originalCommit;
    }

    public String getProj() {
        return proj;
    }

    public String getId() {
        return id;
    }

    public String getWorkingDir() {
        return new File(workingDir).getAbsolutePath();
    }

    private boolean checkout(String version) {
        //defects4j checkout -p $proj -b $id$version -w $workingDir
        CommandSummary cs = new CommandSummary();
        cs.append(d4jCmd, "checkout");
        cs.append("-p", proj);
        version = version.startsWith("f") ? "f" : "b";
        cs.append("-v", id + version);
        cs.append("-w", workingDir);
        String[] cmd = cs.flat();
        int res = FileUtils.executeCommand(cmd, null, timeoutSecond
                , Map.of("JAVA_HOME", ConfigurationProperties.getProperty("jdk8")));
        return res == 0;
    }

    public boolean test() {
        CommandSummary cs = new CommandSummary();
        cs.append("/bin/bash", "-c");
        cs.append("timeout " + timeoutSecond * 1000 + " " + d4jCmd + " test", null);
        String[] cmd = cs.flat();
        int res = FileUtils.executeCommand(cmd, new File(workingDir).getAbsolutePath(), timeoutSecond
                , Map.of("JAVA_HOME", ConfigurationProperties.getProperty("jdk8")));
        if (res == 143) {
            logger.info("test Timeout!");
        }
        return res == 0;
    }

    public boolean singleTest(String testMethod) {
        CommandSummary cs = new CommandSummary();
        cs.append("/bin/bash", "-c");
        cs.append("timeout " + timeoutSecond * 1000 + " " + d4jCmd + " test -t " + testMethod, null);
        String[] cmd = cs.flat();
        int res = FileUtils.executeCommand(cmd, new File(workingDir).getAbsolutePath(), timeoutSecond
                , Map.of("JAVA_HOME", ConfigurationProperties.getProperty("jdk8")));
        if (res == 143) {
            logger.info("test Timeout!");
        }
        return res == 0;
    }

    public boolean commitChangesAndTag(String tagName) {
        //before do:echo "target/" > .gitignore
        // cd "" + "git add -A && git commit -a -m $tageName && git tag &$tagName"
        CommandSummary cs = new CommandSummary();
        cs.append("/bin/bash", "-c");
        cs.append("cd " + workingDir + " && echo 'build/\ntarget/' > .gitignore && git add -A && git commit -a -m " + tagName + " && git tag " + tagName, null);
//        cs.append("git init", "&&");
//        cs.append("git add", "-A 2>&1");
//        cs.append("&&", null);
//        cs.append("git commit", null);
//        cs.append("-a", null);
//        cs.append("-m", tagName);
//        cs.append("2>&1", "&&");
//        cs.append("git tag", tagName);
        String[] cmd = cs.flat();
        int res = FileUtils.executeCommand(cmd, null//new File(workingDir).getAbsolutePath()
                , timeoutSecond, Map.of("JAVA_HOME", ConfigurationProperties.getProperty("jdk8")));
        return res == 0;
    }

    public boolean checkTestResults() {
        return true;
    }

    public boolean rmBrokenTests(String testsLogFile, String testDir) {
        CommandSummary cs = new CommandSummary();
        cs.append(ConfigurationProperties.getProperty("defects4j") + "/framework/util/rm_broken_tests.pl", null);
        cs.append(testsLogFile, testDir);
        String[] cmd = cs.flat();
        int res = FileUtils.executeCommand(cmd, null, timeoutSecond
                , Map.of("JAVA_HOME", ConfigurationProperties.getProperty("jdk8")));
        return res == 0;
    }

    public boolean writeD4JFiles(String version) {
        CommandSummary cs = new CommandSummary();
        cs.append(ConfigurationProperties.getProperty("defects4j") + "/framework/bin/writeFiles.pl", null);
        cs.append("-p", proj);
        version = version.startsWith("fix") ? "f" : version.startsWith("bug") ? "b" : version.startsWith("induc") ? "i" : "o";
        cs.append("-v", id + version);
        cs.append("-w", workingDir);
        String[] cmd = cs.flat();
        int res = FileUtils.executeCommand(cmd, null, timeoutSecond
                , Map.of("JAVA_HOME", ConfigurationProperties.getProperty("jdk8")));
        return res == 0;
    }

    /**
     * use this method when it has been checkout to the target commit!
     * @param commitId
     * @return
     */
    public Map<String, String> getDirs(String version, String commitId) {
        CommandSummary cs = new CommandSummary();
        cs.append(ConfigurationProperties.getProperty("defects4j") + "/framework/bin/get_dirs.pl", null);
        cs.append("-p", proj);
        cs.append("-v", id + version);
        cs.append("-c", commitId);
        cs.append("-w", workingDir);
        String[] cmd = cs.flat();
        int res = FileUtils.executeCommand(cmd, null, timeoutSecond
                , Map.of("JAVA_HOME", ConfigurationProperties.getProperty("jdk8")));
        if(res == 0) {
            return getProperties("/dirs");
        }
        return null;
    }

    /**
     * checkout a specific commit while keeping the project configure.
     * @param repository a valid defects4j project
     * @param commitId target commit to check out
     */
    public boolean switchAndTest(Repository repository, String commitId, String version) {
        boolean res = false;
        try {
            logger.debug("Switch to commit " + commitId);
            gitAccess.checkoutf(workingDir, commitId);
            FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "rm -rf " + workingDir + "/build " + workingDir + "/target"});
            //todo: build.properties need to map to correct package?
            logger.debug("Output defects4j properties and config file...");
            res = writeD4JFiles(version);

            logger.debug("Checking build.xml file...");
            String buildFilePath = ConfigurationProperties.getProperty("defects4j") + "/framework/projects/" + proj + "/build_files/" + commitId;
            File buildFile = new File(buildFilePath);
            if (buildFile.exists()) {
                logger.info("---------- copy build files from defects4j for " + proj + "_" + id + ".");
                FileUtils.copy(buildFile, new File(getWorkingDir()));
            }
            if (FileUtils.notExists(workingDir + "/build.xml") && !FileUtils.notExists(workingDir + "/pom.xml") && !FileUtils.notExists(workingDir + "/maven.xml")) {
                int r = FileUtils.executeCommand(new String[]{"mvn", "ant:ant"}, workingDir, timeoutSecond, null);
                if (r != 0) {
                    return false;
                }
                logger.debug("build.xml file has been generated by `mvn ant:ant`.");
            }

            logger.debug("Execute original tests...");
            boolean testRes = test();
            res &= testRes;

        } catch (Exception e) {
            logger.error("Error occurred when switchAndTest :" + e.getMessage());
            res = false;
        }
        return res;
    }

    public boolean checkAndTest(String commitId, String version, String fixingCommit) {
        boolean res = false;
        try {
            FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "rm -rf " + workingDir + "/build " + workingDir + "/target"});
            //todo: build.properties need to map to correct package?
            logger.debug("Output defects4j properties and config file...");
            res = writeD4JFiles(version);

            logger.debug("Checking build.xml file...");
            String buildFilePath = ConfigurationProperties.getProperty("defects4j") + "/framework/projects/" + proj + "/build_files/" + fixingCommit;
            File buildFile = new File(buildFilePath);
            if (buildFile.exists()) {
                logger.info("---------- copy build files from defects4j from " + buildFilePath);
                FileUtils.copy(buildFile, new File(getWorkingDir()));
            }
            if (FileUtils.notExists(workingDir + "/build.xml") && !FileUtils.notExists(workingDir + "/pom.xml")) {
                FileUtils.executeCommand(new String[]{"mvn", "ant:ant"}, workingDir, timeoutSecond, null);
                logger.debug("build.xml file has been generated by `mvn ant:ant`.");
            }

            logger.debug("Execute original tests...");
            boolean testRes = test();
            res &= testRes;
        } catch (Exception e) {
            logger.error("Error occurred when switchAndTest :" + e.getMessage());
            res = false;
        }
        return res;
    }

    @Deprecated
    public boolean switchAndTag(Repository repository, String commitId, String version, String tagName) {
        boolean res = false;
        try {
            logger.info("Switch to commit " + commitId);
            gitAccess.checkout(repository, commitId);
            //todo: build.properties need to map to correct package!!!
            logger.info("Output defects4j properties and config file...");
            res = writeD4JFiles(version);

            logger.info("Checking build.xml file...");
            if (FileUtils.notExists(workingDir + "/build.xml") && !FileUtils.notExists(workingDir + "/pom.xml")) {
                FileUtils.executeCommand(new String[]{"mvn", "ant:ant"}, workingDir, timeoutSecond, null);
                logger.info("build.xml file has been generated by `mvn ant:ant`.");
            }

            String all_tests = "data/changesInfo/" + proj + "_" + id + "/properties/all_tests/" + version;
            String failing_tests = "data/changesInfo/" + proj + "_" + id + "/properties/failing_tests/" + version;
            if (FileUtils.notExists(failing_tests)) {
                if (FileUtils.notExists(all_tests)) {
                    logger.info("Execute original tests...");
                    logger.info("Writing all_tests info...");
                    res &= test();
                    if (res)
                        FileUtils.copy(new File(workingDir + "/all_tests"), new File(all_tests));
                }
                if (res) {
                    logger.info("Writing failing_tests info...");
                    FileUtils.copy(new File(workingDir + "/failing_tests"), new File(failing_tests));
                }
            }

            logger.info("git int ...");
            res &= gitAccess.init(workingDir, timeoutSecond);

            Map<String, String> properties = getProperties("/defects4j.build.properties");
            logger.info("Read failing tests and Exclude flaky/broken tests...");
            rmBrokenTests(failing_tests, workingDir + File.separator + properties.get("test.dir"));
            logger.info("Add trigger tests...");
            String mappingFile = "data/changesInfo/" + proj + "_" + id + "/properties/mappings/f2i";
            String methods = "/home/liumengjiao/Desktop/vbaprinfo/d4j_bug_info/failed_tests/" + proj.toLowerCase() + File.separator + id + ".txt";
            res &= addTest(repository, mappingFile, FileUtils.readEachLine(methods), fixingCommit, inducingCommit);
            if (res) {
                logger.info("Commit Changes to get a " + version + " version...");
                res = commitChangesAndTag(tagName);
                res &= test();
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            res = false;
        }
        return res;
    }


    public Repository getGitRepository(String version) {
        if (FileUtils.notExists(workingDir) && !checkout(version)) {
            logger.error("Can not get a git repository from " + workingDir + ": working directory does not exist.");
            return null;
        }
        try {
            return gitAccess.getGitRepository(new File(workingDir + "/.git"));
        } catch (Exception e) {
            logger.error("Can not get a git repository from " + workingDir + ": " + e.getMessage());
        }
        return null;
    }

    public String getD4JFix() {
        RevCommit fix = getFixedCommit(getGitRepository("fix"), true);
        return fix.getName();
    }
    
    public String getD4JBuggy() {
        RevCommit buggy = getBuggyCommit(getGitRepository("buggy"), true);
        return buggy.getName();
    }

    private RevCommit getBuggyCommit(Repository repository, boolean isD4JVersion) {
        try {
            //D4J_Closure_30_FIXED_VERSION
            FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "git checkout -f D4J_" + proj + "_" + id + "_BUGGY_VERSION"}, workingDir, 300, null);
            List<RevCommit> walk = gitAccess.createRevsWalkOfAll(repository, false);
            if (isD4JVersion) {
                return walk.get(0);
            }
//            return walk.get(5);
            int i = 0;
            for (; i < walk.size(); i ++) {
                RevCommit commit = walk.get(i);
                String authorName = commit.getAuthorIdent().getName();
                //the latest commit checked by d4j is regarded as bug-fixing commit.
                //well, I don't know how to get its tag name...so I distinguish them by author's name.:-(
                if (!authorName.equals("defects4j")) {
                    break;
                }
            }
            return walk.get(i + 1);
        } catch (Exception e) {
            logger.error("Resolve error or ParseTag error occurred: " + e.getMessage());
        }
        return null;
    }

    private RevCommit getFixedCommit(Repository repository, boolean isD4JVersion) {
        try {
            //D4J_Closure_30_FIXED_VERSION
            FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "git checkout -f D4J_" + proj + "_" + id + "_FIXED_VERSION"}, workingDir, 300, null);
            List<RevCommit> walk = gitAccess.createRevsWalkOfAll(repository, false);
            if (isD4JVersion) {
                return walk.get(0);
            }
//            return walk.get(3);
            int i = 0;
            for (; i < walk.size(); i ++) {
                RevCommit commit = walk.get(i);
                String authorName = commit.getAuthorIdent().getName();
                //the latest commit checked by d4j is regarded as bug-fixing commit.
                //well, I don't know how to get its tag name...so I distinguish them by author's name.:-(
                if (!authorName.equals("defects4j")) {
                    break;
                }
            }
            return walk.get(i);
        } catch (Exception e) {
            logger.error("Resolve error or ParseTag error occurred: " + e.getMessage());
        }
        return null;
    }

    private String getDir(String postFix) {
        String dir = "";
        String path = FileUtils.findOneFilePath(workingDir, postFix);
        List<String> contents = FileUtils.readEachLine(path);
        List<String> aPackage = contents.stream().filter(line -> line.startsWith("package")).collect(Collectors.toList());
        path = path.substring(path.indexOf(workingDir) + workingDir.length() + 1
                        , path.lastIndexOf("."))
                .replaceAll(File.separator, ".");
        String qualifiedName = path.substring(path.lastIndexOf(".") + 1);
        if (!aPackage.isEmpty()) {
            qualifiedName = aPackage.get(0).substring(aPackage.get(0).lastIndexOf(" ") + 1, aPackage.get(0).length() - 1);
        }
        dir = path.substring(0, path.indexOf(qualifiedName) - 1);
        return dir;
    }

    public Map<String, String> getProperties(String properitesFile) {
        List<String> lines = FileUtils.readEachLine(workingDir + properitesFile);
        Map<String, String> pros = new HashMap<>();
        for (String line : lines) {
            if (line.startsWith("d4j.dir.src.classes")) {
                pros.put("src.dir", line.split("=")[1]);
            }
            if (line.startsWith("d4j.dir.src.tests")) {
                pros.put("test.dir", line.split("=")[1]);
            }
            if (line.startsWith("d4j.classes.modified")) {
                pros.put("clz.modified", line.split("=")[1]);
            }
            if (line.startsWith("d4j.tests.trigger")) {
                pros.put("trigger.tests", line.split("=")[1]);
            }
        }
        return pros;
    }


    public void getDiffInfo(Repository repository, String srcCommit, String dstCommit, String version) {
        List<String> modifiedClasses = new ArrayList<>();
        String filePath = "data/changesInfo/" + proj + "_" + id;
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

    public boolean addTest(Repository repository, String mappingFile, List<String> methods, String fixingCommit, String inducingCommit){
        try {
            List<List<String>> f2is = gitAccess.getF2i(mappingFile, methods);
            gitAccess.checkout(repository, fixingCommit);
            ASTManipulator astManipulator = new ASTManipulator(8);
            Map<List<String>, ASTNode> triggerTests = new HashMap<>();
            Map<List<String>, List<?>> imports = new HashMap<>();
            Map<List<String>, List<MethodDeclaration>> dependencies = new HashMap<>();
            for (List<String> f2i :f2is) {
                List<?> importDeclarations = new ArrayList<>();
                List<MethodDeclaration> methodDeclarations = new ArrayList<>();
                MethodDeclaration triggerTest = astManipulator.extractTest(FileUtils.readFileByChars(workingDir + File.separator + f2i.get(1)), f2i.get(3)
                        , importDeclarations, methodDeclarations);
                triggerTests.put(f2i, triggerTest);
                imports.put(f2i, importDeclarations);
                dependencies.put(f2i, methodDeclarations);
            }
            gitAccess.checkout(repository, inducingCommit);
            for (Map.Entry<List<String>, ASTNode> entry: triggerTests.entrySet()) {
                String s = astManipulator.insertTest(FileUtils.readFileByChars(workingDir + File.separator + entry.getKey().get(2)), entry.getValue()
                        , mappingFile, imports.get(entry.getKey()), dependencies.get(entry.getKey()));
                FileUtils.writeToFile(s, workingDir + File.separator + entry.getKey().get(2), false);
            }
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return false;
    }

    public List<String> getTriggerTests(String triggeTestsPath) {
        List<String> lines = FileUtils.readEachLine(triggeTestsPath);
        if (lines.isEmpty()) {
            return lines;
        }
        List<String> failing_tests = lines.stream().filter(line -> line.startsWith("--- ")).collect(Collectors.toList());
        failing_tests = failing_tests.stream().map(f -> f.split(" ")[1]).collect(Collectors.toList());
        return failing_tests;
    }

    public List<String> getFailingTests(String failingTestsPath) {
        List<String> lines = FileUtils.readEachLine(workingDir + File.separator + failingTestsPath);
        if (lines.isEmpty()) {
            return lines;
        }
        List<String> failing_tests = lines.stream().filter(line -> line.startsWith("--- ")).collect(Collectors.toList());
        failing_tests = failing_tests.stream().map(f -> f.split(" ")[1]).collect(Collectors.toList());
        return failing_tests;
    }

    public boolean isAllTest1NotInTests2(List<String> tests1, List<String> tests2) {
        tests1 = tests1.stream().map(m -> m.split("::")[1]).collect(Collectors.toList());
        tests2 = tests2.stream().map(m -> m.split("::")[1]).collect(Collectors.toList());
        for (String test :tests1) {
            if (tests2.contains(test)) {
                return false;
            }
        }
        return true;
    }

    public String bisectTest(Repository repository, String fixingCommit, String startCommit, boolean findOlderVersion){
        boolean res = false;
        String fakeInducing = "", fakeOriginal = "";
        String bugName = proj + "_" + id + "_buggy";
        FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "git add . && git stash && git stash drop"}, workingDir, 300, null);
        int r = -1;
        if (findOlderVersion) {
            List<RevCommit> revsWalkOfAll = gitAccess.createRevsWalkOfAll(repository, true);
            r = FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "git bisect start " + startCommit + " " + revsWalkOfAll.get(0).getName()}, workingDir, 300, null);
        } else {
            r = FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "git bisect start " + fixingCommit + " " + startCommit}, workingDir, 300, null);
        }

        while (r == 0) {
            fakeInducing = gitAccess.getCurrentHeadCommit(repository);
            res = checkAndTest(gitAccess.getCurrentHeadCommit(repository), "buggy", fixingCommit);
            if (!res) {
                logger.info("---------- " + bugName + " failed in continuous test.");
                FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "git add . && git stash && git stash drop"}, workingDir, 300, null);
                int i = FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "git bisect skip"}, workingDir, 300, null);
                if (i != 0)
                    return startCommit;
                continue;
            }
            //get mapping file
            String mappingFile = workingDir + "/../findInducing/" + bugName + "/properties/mappings/f2i";
            gitAccess.getFileStatDiffBetweenCommits(workingDir, fixingCommit, fakeInducing, mappingFile);
            // extract trigger test and modified classes
            Map<String, String> properties = getProperties("/defects4j.build.properties");//todo: package name from wrong version after version change.
            List<String> triggerTests = List.of(properties.get("trigger.tests").split(","));
            addTest(repository, mappingFile, triggerTests, fixingCommit, fakeInducing);

            // test for each single trigger test
            int count = 0;
            for (String triggerTest : triggerTests) {
                res = singleTest(triggerTest);
                if (!res) {
                    logger.info("---------- " + bugName + " went wrong after changing tests.");
                    FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "git add . && git stash && git stash drop"}, workingDir, 300, null);
                    FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "git bisect reset"}, workingDir, 300, null);
                    return fakeInducing;
                }
                List<String> failingTests_new = getFailingTests("failing_tests");
                count += failingTests_new.size();
                if (count != 0)
                    break;
            }
            if (count > 0) {
                FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "git add . && git stash && git stash drop"}, workingDir, 300, null);
                List<String> message = FileUtils.execute(new String[]{"/bin/bash", "-c", "git bisect bad"}, workingDir, 300, null);
                logger.info("Command result:\n" + FileUtils.getStrOfIterable(message, "\n"));
                if (Integer.parseInt(message.get(0)) == 0) {
                    String s = message.get(1);
                    if (s.contains("is the first bad commit")) {
                        logger.info("----------" + bugName + " successfully found inducing commit");
                        bugName += "*";
                        fakeInducing = s.split(" ")[0];
                        FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "git bisect reset"}, workingDir, 300, null);
                        break;
                    }
                } else {
                    logger.info("----------" + bugName + " error occurred.");
                    return fakeInducing;
                }
            } else {
                FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "git add . && git stash && git stash drop"}, workingDir, 300, null);
                List<String> message = FileUtils.execute(new String[]{"/bin/bash", "-c", "git bisect good"}, workingDir, 300, null);
                logger.info("Command result:\n" + FileUtils.getStrOfIterable(message, "\n"));
                if (Integer.parseInt(message.get(0)) == 0) {
                    String s = message.get(1);
                    if (s.contains("is the first bad commit")) {
                        logger.info("----------" + bugName + " successfully found inducing commit");
                        bugName += "*";
                        fakeInducing = s.split(" ")[0];
                        FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "git bisect reset"}, workingDir, 300, null);
                        break;
                    }
                } else {
                    logger.info("----------" + bugName + " error occurred.");
                    return fakeInducing;
                }
            }
        }
        if (findOlderVersion) {
            FileUtils.writeToFile(proj + "," + id + "," + fakeInducing + "\n", "data/bug_original_commits", true);
        } else {
            FileUtils.writeToFile(proj + "," + id + "," + fakeInducing + "\n", "data/bug_inducing_commits", true);
        }
        return null;
    }

    public String findInducingCommit(String fixedCommit, String startCommit, boolean findOlderVersion) {
        String bugName = proj + "_" + id + "_buggy";
        logger.info("Starting find inducing commit process for " + bugName + "...");
        String res = startCommit;
        try {
            Repository repository = getGitRepository("b");
            //initial checkout & test : at fixed commit
            boolean r = switchAndTest(repository, fixedCommit, "fixing");
            if (!r) {
                logger.info("---------- " + bugName + " failed in initial test.");
                return res;
            }
//            Set<String> testsClz = triggerTests.stream().map(t -> t.split("::")[0]).collect(Collectors.toSet());
//            for (String testCls :testsClz) {
//                String srcFilePath = workingDir + "/" + properties.get("test.dir") + "/" + testCls.replaceAll("[.]", "/") + ".java";
//                String dstFilePath = workingDir + "/../findInducing/" + bugName + "/fixing/" + properties.get("test.dir") + "/" + testCls.replaceAll("[.]", "/") + ".java";
//                FileUtils.copy(new File(srcFilePath), new File(dstFilePath));
//            }
//            List<String> modifiedClz = List.of(properties.get("clz.modified").split(","));
//            for (String modifiedCls: modifiedClz) {
//                String srcFilePath = workingDir + "/" + properties.get("src.dir") + "/" + modifiedCls.replaceAll("[.]", "/") + ".java";
//                String dstFilePath = workingDir + "/../findInducing/" + bugName + "/fixing/" + properties.get("src.dir") + "/" + modifiedCls.replaceAll("[.]", "/") + ".java";
//                FileUtils.copy(new File(srcFilePath), new File(dstFilePath));
//            }

            // find inducing commit with binary search
            res = bisectTest(repository, fixedCommit, startCommit, findOlderVersion);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("error occurred when processing " + bugName + ": " + e.getMessage());
        }
        return res;
    }

    public boolean findInducingCommitSequencely(String fixedCommit, String startCommit) {
        String bugName = proj + "_" + id + "_buggy";
        logger.info("Starting find inducing commit process for " + bugName + "...");
        String fakeInducing = "", fakeOriginal = "";
        boolean res = false;
        try {
            Repository repository = getGitRepository("b");
            //initial checkout & test : at fixed commit
            res = switchAndTest(repository, fixedCommit, "fixing");
            if (!res) {
                String buildFilePath = ConfigurationProperties.getProperty("defects4j") + "/framework/projects/" + proj + "/build_files/" + fixedCommit;
                File buildFile = new File(buildFilePath);
                if (buildFile.exists()) {
                    FileUtils.copy(buildFile, new File(getWorkingDir()));
                    res = test();
                }
                if (!res) {
                    logger.info("---------- " + bugName + " failed in initial test.");
                    return false;
                }
            }
            // extract trigger test and modified classes
            Map<String, String> properties = getProperties("/defects4j.build.properties");
            List<String> triggerTests = List.of(properties.get("trigger.tests").split(","));
            Set<String> testsClz = triggerTests.stream().map(t -> t.split("::")[0]).collect(Collectors.toSet());
            for (String testCls :testsClz) {
                String srcFilePath = workingDir + "/" + properties.get("test.dir") + "/" + testCls.replaceAll("[.]", "/") + ".java";
                String dstFilePath = workingDir + "/findInducing/fixing/" + properties.get("test.dir") + "/" + testCls.replaceAll("[.]", "/") + ".java";
                FileUtils.copy(new File(srcFilePath), new File(dstFilePath));
            }
            List<String> modifiedClz = List.of(properties.get("clz.modified").split(","));
            for (String modifiedCls: modifiedClz) {
                String srcFilePath = workingDir + "/" + properties.get("src.dir") + "/" + modifiedCls.replaceAll("[.]", "/") + ".java";
                String dstFilePath = workingDir + "/findInducing/fixing/" + properties.get("src.dir") + "/" + modifiedCls.replaceAll("[.]", "/") + ".java";
                FileUtils.copy(new File(srcFilePath), new File(dstFilePath));
            }

            gitAccess.checkoutf(workingDir, startCommit);
            // loop to find inducing commit
            List<RevCommit> revsWalkOfAll = gitAccess.createRevsWalkOfAll(repository, false);
            for (int i = 2; i < revsWalkOfAll.size(); i ++) {
                String commit = revsWalkOfAll.get(i).getName();
                fakeInducing = revsWalkOfAll.get(i - 1).getName();
                fakeOriginal = commit;
                res = switchAndTest(repository, commit, "inducing");
                if (!res) {
                    logger.info("---------- " + bugName + " failed in continuous test.");
                    return false;
                }
//                List<String> failingTests = getFailingTests("failing_tests");

                //todo: add trigger tests accurately
                String srcFilePath = "findInducing/fixing/" + properties.get("test.dir") + "/*";
                String dstFilePath = properties.get("test.dir") + "/";
                FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "cp -r " + srcFilePath + " " + dstFilePath}, workingDir, 300, null);

                // test for each single trigger test
                int count = 0;
                for (String triggerTest: triggerTests) {
                    res = singleTest(triggerTest);
                    if (!res) {
                        logger.info("---------- " + bugName + " failed after changing tests.");
                        return false;
                    }
                    List<String> failingTests_new = getFailingTests("failing_tests");
                    count += failingTests_new.size();
                }
                if (count >= 0 && count < triggerTests.size()) {
                    logger.info("----------" + bugName + " successfully found original commit");
                    FileUtils.writeToFile(bugName + "," + fakeInducing + "," + fakeOriginal + "\n", "/bug_original_commits", true);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("error occurred when processing " + bugName + ": " + e.getMessage());
        }
        return false;
    }
}
