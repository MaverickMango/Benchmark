package root.bean;

public class BugFixCommit extends AbstractBeanClazz{
    private String bugName;
    private String bugId;
    private String diff;
    private RepositoryInfo repo;
    private CommitInfo buggyCommit;
    private CommitInfo commitBeforeBuggy;
    private CommitInfo fixedCommit;
    private CommitInfo commitBeforeFixing;
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

    public CommitInfo getBuggyCommit() {
        return buggyCommit;
    }

    public void setBuggyCommit(CommitInfo buggyCommit) {
        this.buggyCommit = buggyCommit;
    }

    public CommitInfo getFixedCommit() {
        return fixedCommit;
    }

    public void setFixedCommit(CommitInfo fixedCommit) {
        this.fixedCommit = fixedCommit;
    }

    public CommitInfo getCommitBeforeBuggy() {
        return commitBeforeBuggy;
    }

    public void setCommitBeforeBuggy(CommitInfo commitBeforeBuggy) {
        this.commitBeforeBuggy = commitBeforeBuggy;
    }

    public CommitInfo getCommitBeforeFixing() {
        return commitBeforeFixing;
    }

    public void setCommitBeforeFixing(CommitInfo commitBeforeFixing) {
        this.commitBeforeFixing = commitBeforeFixing;
    }

    public String getBugReport() {
        return bugReport;
    }

    public void setBugReport(String bugReport) {
        this.bugReport = bugReport;
    }


}
