package root.entities.otherdataset;

import com.google.gson.annotations.SerializedName;

public class Metrics {
    @SerializedName("additions")
    private Integer additions;
    @SerializedName("changes")
    private Integer changes;
    @SerializedName("deletions")
    private Integer deletions;
    @SerializedName("num_of_changed_files")
    private Integer numOfChangedFiles;

    public Integer getAdditions() {
        return additions;
    }

    public void setAdditions(Integer additions) {
        this.additions = additions;
    }

    public Integer getChanges() {
        return changes;
    }

    public void setChanges(Integer changes) {
        this.changes = changes;
    }

    public Integer getDeletions() {
        return deletions;
    }

    public void setDeletions(Integer deletions) {
        this.deletions = deletions;
    }

    public Integer getNumOfChangedFiles() {
        return numOfChangedFiles;
    }

    public void setNumOfChangedFiles(Integer numOfChangedFiles) {
        this.numOfChangedFiles = numOfChangedFiles;
    }
}
