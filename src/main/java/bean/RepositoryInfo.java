package bean;

public class RepositoryInfo extends AbstractBeanClazz{
    /*
      "name": "FasterXML-jackson-databind",
      "url": "https://github.com/FasterXML/jackson-databind",
      "isFork": false,
      "pullRequestId": 0,
      "isPullRequest": false,
      "original": {
        "url": "",
        "name": "",
        "githubId": 0
      },
      "githubId": 3038937
     */
    private String name;
    private String url;
    private Boolean isFork;
    private int pullRequestId;
    private Boolean isPullRequest;
    private String Original;
    private long githubId;

    public RepositoryInfo(String name, String url, Boolean isFork, int pullRequestId, Boolean isPullRequest, long githubId) {
        this.name = name;
        this.url = url;
        this.isFork = isFork;
        this.pullRequestId = pullRequestId;
        this.isPullRequest = isPullRequest;
        this.githubId = githubId;
    }

//    public Repository(String name, String url, Boolean isFork, int pullRequestId, Boolean isPullRequest, String original, long githubId) {
//        this.name = name;
//        this.url = url;
//        this.isFork = isFork;
//        this.pullRequestId = pullRequestId;
//        this.isPullRequest = isPullRequest;
//        Original = original;
//        this.githubId = githubId;
//    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getFork() {
        return isFork;
    }

    public void setFork(Boolean fork) {
        isFork = fork;
    }

    public int getPullRequestId() {
        return pullRequestId;
    }

    public void setPullRequestId(int pullRequestId) {
        this.pullRequestId = pullRequestId;
    }

    public Boolean getPullRequest() {
        return isPullRequest;
    }

    public void setPullRequest(Boolean pullRequest) {
        isPullRequest = pullRequest;
    }

    public String getOriginal() {
        return Original;
    }

    public void setOriginal(String original) {
        Original = original;
    }

    public long getGithubId() {
        return githubId;
    }

    public void setGithubId(long githubId) {
        this.githubId = githubId;
    }
}
