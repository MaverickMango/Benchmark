package root.entities.otherdataset;

import com.google.gson.annotations.SerializedName;

public class BugswarmShow {

    @SerializedName("_created")
    private String created;
    @SerializedName("_deleted")
    private Boolean deleted;
    @SerializedName("_etag")
    private String etag;
    @SerializedName("_id")
    private String id;
    @SerializedName("_links")
    private Links links;
    @SerializedName("_updated")
    private String updated;
    @SerializedName("added_version")
    private String addedVersion;
    @SerializedName("base_branch")
    private String baseBranch;
    @SerializedName("branch")
    private String branch;
    @SerializedName("build_system")
    private String buildSystem;
    @SerializedName("cached")
    private Boolean cached;
    @SerializedName("ci_service")
    private String ciService;
    @SerializedName("classification")
    private Classification classification;
    @SerializedName("creation_time")
    private Integer creationTime;
    @SerializedName("current_image_tag")
    private String currentImageTag;
    @SerializedName("deprecated_version")
    private Object deprecatedVersion;
    @SerializedName("failed_job")
    private FailedJob failedJob;
    @SerializedName("filtered_reason")
    private Object filteredReason;
    @SerializedName("image_tag")
    private String imageTag;
    @SerializedName("is_error_pass")
    private Boolean isErrorPass;
    @SerializedName("lang")
    private String lang;
    @SerializedName("match")
    private String match;
    @SerializedName("merged_at")
    private Object mergedAt;
    @SerializedName("metrics")
    private Metrics metrics;
    @SerializedName("passed_job")
    private PassedJob passedJob;
    @SerializedName("pr_num")
    private Integer prNum;
    @SerializedName("repo")
    private String repo;
    @SerializedName("repo_mined_version")
    private String repoMinedVersion;
    @SerializedName("reproduce_attempts")
    private Integer reproduceAttempts;
    @SerializedName("reproduce_successes")
    private Integer reproduceSuccesses;
    @SerializedName("reproduced")
    private Boolean reproduced;
    @SerializedName("reproducibility_status")
    private ReproducibilityStatus reproducibilityStatus;
    @SerializedName("stability")
    private String stability;
    @SerializedName("status")
    private String status;
    @SerializedName("test_framework")
    private String testFramework;

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getAddedVersion() {
        return addedVersion;
    }

    public void setAddedVersion(String addedVersion) {
        this.addedVersion = addedVersion;
    }

    public String getBaseBranch() {
        return baseBranch;
    }

    public void setBaseBranch(String baseBranch) {
        this.baseBranch = baseBranch;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getBuildSystem() {
        return buildSystem;
    }

    public void setBuildSystem(String buildSystem) {
        this.buildSystem = buildSystem;
    }

    public Boolean getCached() {
        return cached;
    }

    public void setCached(Boolean cached) {
        this.cached = cached;
    }

    public String getCiService() {
        return ciService;
    }

    public void setCiService(String ciService) {
        this.ciService = ciService;
    }

    public Classification getClassification() {
        return classification;
    }

    public void setClassification(Classification classification) {
        this.classification = classification;
    }

    public Integer getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Integer creationTime) {
        this.creationTime = creationTime;
    }

    public String getCurrentImageTag() {
        return currentImageTag;
    }

    public void setCurrentImageTag(String currentImageTag) {
        this.currentImageTag = currentImageTag;
    }

    public Object getDeprecatedVersion() {
        return deprecatedVersion;
    }

    public void setDeprecatedVersion(Object deprecatedVersion) {
        this.deprecatedVersion = deprecatedVersion;
    }

    public FailedJob getFailedJob() {
        return failedJob;
    }

    public void setFailedJob(FailedJob failedJob) {
        this.failedJob = failedJob;
    }

    public Object getFilteredReason() {
        return filteredReason;
    }

    public void setFilteredReason(Object filteredReason) {
        this.filteredReason = filteredReason;
    }

    public String getImageTag() {
        return imageTag;
    }

    public void setImageTag(String imageTag) {
        this.imageTag = imageTag;
    }

    public Boolean getIsErrorPass() {
        return isErrorPass;
    }

    public void setIsErrorPass(Boolean isErrorPass) {
        this.isErrorPass = isErrorPass;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getMatch() {
        return match;
    }

    public void setMatch(String match) {
        this.match = match;
    }

    public Object getMergedAt() {
        return mergedAt;
    }

    public void setMergedAt(Object mergedAt) {
        this.mergedAt = mergedAt;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public void setMetrics(Metrics metrics) {
        this.metrics = metrics;
    }

    public PassedJob getPassedJob() {
        return passedJob;
    }

    public void setPassedJob(PassedJob passedJob) {
        this.passedJob = passedJob;
    }

    public Integer getPrNum() {
        return prNum;
    }

    public void setPrNum(Integer prNum) {
        this.prNum = prNum;
    }

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public String getRepoMinedVersion() {
        return repoMinedVersion;
    }

    public void setRepoMinedVersion(String repoMinedVersion) {
        this.repoMinedVersion = repoMinedVersion;
    }

    public Integer getReproduceAttempts() {
        return reproduceAttempts;
    }

    public void setReproduceAttempts(Integer reproduceAttempts) {
        this.reproduceAttempts = reproduceAttempts;
    }

    public Integer getReproduceSuccesses() {
        return reproduceSuccesses;
    }

    public void setReproduceSuccesses(Integer reproduceSuccesses) {
        this.reproduceSuccesses = reproduceSuccesses;
    }

    public Boolean getReproduced() {
        return reproduced;
    }

    public void setReproduced(Boolean reproduced) {
        this.reproduced = reproduced;
    }

    public ReproducibilityStatus getReproducibilityStatus() {
        return reproducibilityStatus;
    }

    public void setReproducibilityStatus(ReproducibilityStatus reproducibilityStatus) {
        this.reproducibilityStatus = reproducibilityStatus;
    }

    public String getStability() {
        return stability;
    }

    public void setStability(String stability) {
        this.stability = stability;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTestFramework() {
        return testFramework;
    }

    public void setTestFramework(String testFramework) {
        this.testFramework = testFramework;
    }
}
