package root.entities.otherdataset;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FailedJob {
    @SerializedName("base_sha")
    private String baseSha;
    @SerializedName("build_id")
    private Long buildId;
    @SerializedName("build_job")
    private String buildJob;
    @SerializedName("committed_at")
    private String committedAt;
    @SerializedName("component_versions")
    private ComponentVersions componentVersions;
    @SerializedName("config")
    private Config config;
    @SerializedName("failed_tests")
    private String failedTests;
    @SerializedName("is_git_repo")
    private Boolean isGitRepo;
    @SerializedName("job_id")
    private Long jobId;
    @SerializedName("message")
    private String message;
    @SerializedName("mismatch_attrs")
    private List<?> mismatchAttrs;
    @SerializedName("num_tests_failed")
    private Integer numTestsFailed;
    @SerializedName("num_tests_run")
    private Integer numTestsRun;
    @SerializedName("patches")
    private Patches patches;
    @SerializedName("trigger_sha")
    private String triggerSha;

    public String getBaseSha() {
        return baseSha;
    }

    public void setBaseSha(String baseSha) {
        this.baseSha = baseSha;
    }

    public Long getBuildId() {
        return buildId;
    }

    public void setBuildId(Long buildId) {
        this.buildId = buildId;
    }

    public String getBuildJob() {
        return buildJob;
    }

    public void setBuildJob(String buildJob) {
        this.buildJob = buildJob;
    }

    public String getCommittedAt() {
        return committedAt;
    }

    public void setCommittedAt(String committedAt) {
        this.committedAt = committedAt;
    }

    public ComponentVersions getComponentVersions() {
        return componentVersions;
    }

    public void setComponentVersions(ComponentVersions componentVersions) {
        this.componentVersions = componentVersions;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public String getFailedTests() {
        return failedTests;
    }

    public void setFailedTests(String failedTests) {
        this.failedTests = failedTests;
    }

    public Boolean getIsGitRepo() {
        return isGitRepo;
    }

    public void setIsGitRepo(Boolean isGitRepo) {
        this.isGitRepo = isGitRepo;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<?> getMismatchAttrs() {
        return mismatchAttrs;
    }

    public void setMismatchAttrs(List<?> mismatchAttrs) {
        this.mismatchAttrs = mismatchAttrs;
    }

    public Integer getNumTestsFailed() {
        return numTestsFailed;
    }

    public void setNumTestsFailed(Integer numTestsFailed) {
        this.numTestsFailed = numTestsFailed;
    }

    public Integer getNumTestsRun() {
        return numTestsRun;
    }

    public void setNumTestsRun(Integer numTestsRun) {
        this.numTestsRun = numTestsRun;
    }

    public Patches getPatches() {
        return patches;
    }

    public void setPatches(Patches patches) {
        this.patches = patches;
    }

    public String getTriggerSha() {
        return triggerSha;
    }

    public void setTriggerSha(String triggerSha) {
        this.triggerSha = triggerSha;
    }
}
