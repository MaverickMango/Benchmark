package root.entities.otherdataset;

import root.entities.AbstractBeanClazz;

public class CommitInfo extends AbstractBeanClazz {
    /*
        "url": "http://github.com/FasterXML/jackson-databind/commit/d44600d3750e5dba9fac68aee7248ed2a80a2225",
        "sha": "d44600d3750e5dba9fac68aee7248ed2a80a2225",
        "branchName": "2.7",
        "date": "Jan 9, 2017 10:42:27 PM",
        "repoName": "FasterXML/jackson-databind"
     */
    private String url;
    private String sha;
    private String branchName;
    private String date;
    private String fullMessage;
    private String repoName;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFullMessage() {
        return fullMessage;
    }

    public void setFullMessage(String fullMessage) {
        this.fullMessage = fullMessage;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }
}
