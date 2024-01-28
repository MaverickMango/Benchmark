package root.entities.otherdataset;

import root.entities.AbstractBeanClazz;

public class BugFixCommit extends AbstractBeanClazz {
    private String bugName;
    private String bugId;
    private String diff;
    private RepositoryInfo repo;
    private CommitInfo inducingCommit;
    private CommitInfo originalCommit;
    private CommitInfo fixedCommit;
    private CommitInfo buggyCommit;
    private String bugReport;

    public BugFixCommit(String bugName, String bugId) {
        this.bugName = bugName;
        this.bugId = bugId;
    }

    public BugFixCommit() {}

    public String getBugName() {
        return bugName;
    }

    public void setBugName(String bugName) {
        this.bugName = bugName;
    }

    public String getBugId() {
        return bugId;
    }

    public void setBugId(String bugId) {
        this.bugId = bugId;
    }

    public String getDiff() {
        return diff;
    }

    public void setDiff(String diff) {
        this.diff = diff;
    }

    public RepositoryInfo getRepo() {
        return repo;
    }

    public void setRepo(RepositoryInfo repo) {
        this.repo = repo;
    }

    public CommitInfo getInducingCommit() {
        return inducingCommit;
    }

    public void setInducingCommit(CommitInfo inducingCommit) {
        this.inducingCommit = inducingCommit;
    }

    public CommitInfo getFixedCommit() {
        return fixedCommit;
    }

    public void setFixedCommit(CommitInfo fixedCommit) {
        this.fixedCommit = fixedCommit;
    }

    public CommitInfo getOriginalCommit() {
        return originalCommit;
    }

    public void setOriginalCommit(CommitInfo originalCommit) {
        this.originalCommit = originalCommit;
    }

    public CommitInfo getBuggyCommit() {
        return buggyCommit;
    }

    public void setBuggyCommit(CommitInfo buggyCommit) {
        this.buggyCommit = buggyCommit;
    }

    public String getBugReport() {
        return bugReport;
    }

    public void setBugReport(String bugReport) {
        this.bugReport = bugReport;
    }


}
