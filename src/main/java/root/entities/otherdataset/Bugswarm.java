package root.entities.otherdataset;

import com.google.gson.annotations.SerializedName;

import java.math.BigInteger;
import java.util.List;

public class Bugswarm {

    @SerializedName("image_tag")
    private String imageTag;
    @SerializedName("lang")
    private String lang;
    @SerializedName("metrics")
    private Metrics metrics;
    @SerializedName("reproducibility_status")
    private ReproducibilityStatus reproducibilityStatus;
    @SerializedName("failed_job")
    private FailedJob failedJob;
    @SerializedName("passed_job")
    private PassedJob passedJob;
    @SerializedName("classification")
    private Classification classification;
    @SerializedName("diff_url")
    private String diffUrl;

    public String getImageTag() {
        return imageTag;
    }

    public String getLang() {
        return lang;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public ReproducibilityStatus getReproducibilityStatus() {
        return reproducibilityStatus;
    }

    public FailedJob getFailedJob() {
        return failedJob;
    }

    public PassedJob getPassedJob() {
        return passedJob;
    }

    public Classification getClassification() {
        return classification;
    }

    public String getDiffUrl() {
        return diffUrl;
    }

    public static class Metrics {
        @SerializedName("changes")
        private Integer changes;

        public Integer getChanges() {
            return changes;
        }
    }

    public static class ReproducibilityStatus {
        @SerializedName("status")
        private String status;

        public String getStatus() {
            return status;
        }
    }

    public static class FailedJob {
        @SerializedName("job_id")
        private BigInteger jobId;
        @SerializedName("build_id")
        private BigInteger buildId;
        @SerializedName("num_tests_run")
        private Integer numTestsRun;
        @SerializedName("num_tests_failed")
        private Integer numTestsFailed;
        @SerializedName("trigger_sha")
        private String triggerSha;

        public BigInteger getJobId() {
            return jobId;
        }

        public BigInteger getBuildId() {
            return buildId;
        }

        public Integer getNumTestsRun() {
            return numTestsRun;
        }

        public Integer getNumTestsFailed() {
            return numTestsFailed;
        }

        public String getTriggerSha() {
            return triggerSha;
        }
    }

    public static class PassedJob {
        @SerializedName("trigger_sha")
        private String triggerSha;

        public String getTriggerSha() {
            return triggerSha;
        }
    }

    public static class Classification {
        @SerializedName("build")
        private String build;
        @SerializedName("code")
        private String code;
        @SerializedName("exceptions")
        private List<?> exceptions;
        @SerializedName("test")
        private String test;

        public String getBuild() {
            return build;
        }

        public String getCode() {
            return code;
        }

        public List<?> getExceptions() {
            return exceptions;
        }

        public String getTest() {
            return test;
        }
    }
}
