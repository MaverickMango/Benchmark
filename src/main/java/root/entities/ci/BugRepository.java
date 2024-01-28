package root.entities.ci;

import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.entities.benchmarks.Defects4JBug;
import root.entities.Patch;
import root.util.ConfigurationProperties;
import root.util.FileUtils;
import root.util.GitAccess;
import root.util.GitTool;

import java.io.File;
import java.util.List;

public class BugRepository implements GitAccess {

    private final Logger logger = LoggerFactory.getLogger(BugRepository.class);
    BugWithHistory bug;
    Repository repository;

    public BugRepository(BugWithHistory bug) {
        this.bug = bug;
//        bug.setBugName(bug.getProj() + "_" + bug.getId());
        this.repository = bug.getGitRepository();
        this.bug.compile();
    }

    public GitTool getGitAccess() {
        return gitAccess;
    }

    public Repository getRepository() {
        return repository;
    }

    public BugWithHistory getBug() {
        return bug;
    }

    public boolean test(String testName) {
        boolean res = false;
        if (!(bug instanceof Defects4JBug))
            return res;
        Defects4JBug defects4JBug = (Defects4JBug) bug;
        res = defects4JBug.specifiedTest(testName);
        return res;
    }

    public List<String> testWithRes(String testName) {
        List<String> res = null;
        if (!(bug instanceof Defects4JBug))
            return res;
        Defects4JBug defects4JBug = (Defects4JBug) bug;
        res = defects4JBug.specifiedTestWithRes(testName);
        return res;
    }

    public boolean switchToBug() {
        boolean res = false;
        if (!(bug instanceof Defects4JBug))
            return res;
        Defects4JBug defects4JBug = (Defects4JBug) bug;
        try {
            logger.debug("Switch to commit " + defects4JBug.getD4JBuggy());
            res = gitAccess.checkoutf(defects4JBug.getWorkingDir(), defects4JBug.getD4JBuggy());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return res;
    }

    public boolean switchToOrg() {
        boolean res = false;
        if (!(bug instanceof Defects4JBug))
            return res;
        Defects4JBug defects4JBug = (Defects4JBug) bug;
        res = defects4JBug.switchTo(repository, defects4JBug.getOriginalCommit(), "org", false);
        return res;
    }

    public boolean switchToInducing() {
        boolean res = false;
        if (!(bug instanceof Defects4JBug))
            return res;
        Defects4JBug defects4JBug = (Defects4JBug) bug;
        res = defects4JBug.switchTo(repository, defects4JBug.getInducingCommit(), "inducing", false);
        return res;
    }

    public boolean applyPatch(Patch patch) {
        boolean res = switchToBug();
        if (!res) {
            logger.error("Error occurred when switching " + bug.getBugName() + " to buggy version!");
            return res;
        }
        //todo another better way to apply patch???
        String patchDir = ConfigurationProperties.getProperty("patchDir") + File.separator;
        String[] cmd = new String[]{"/bin/bash", "-c", "cp -r "+ patch.getPathFromRoot() + File.separator + "* " + patchDir};
        int r = FileUtils.executeCommand(cmd, null, 300, null);
        return r == 0;
    }
}
