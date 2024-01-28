package root.entities.otherdataset;

import com.google.gson.annotations.SerializedName;

public class Steps {
    @SerializedName("uses")
    private String uses;
    @SerializedName("name")
    private String name;
    @SerializedName("with")
    private With with;
    @SerializedName("run")
    private String run;

    public String getUses() {
        return uses;
    }

    public void setUses(String uses) {
        this.uses = uses;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public With getWith() {
        return with;
    }

    public void setWith(With with) {
        this.with = with;
    }

    public String getRun() {
        return run;
    }

    public void setRun(String run) {
        this.run = run;
    }
}
