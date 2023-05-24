package bean;

public class BugFixCommit extends AbstractBeanClazz{
    private String bugName;
    private String bugId;
    private String diff;
    private RepositoryInfo repo;
    private CommitInfo buggyCommit;
    private CommitInfo fixedCommit;
    private String bugReport;

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

    public String getBugReport() {
        return bugReport;
    }

    public void setBugReport(String bugReport) {
        this.bugReport = bugReport;
    }


}
