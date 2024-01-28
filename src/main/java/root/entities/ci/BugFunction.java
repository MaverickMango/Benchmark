package root.entities.ci;

import com.google.gson.annotations.SerializedName;

public class BugFunction {
    @SerializedName("buggy")
    private String buggy;
    @SerializedName("start")
    private Integer start;
    @SerializedName("end")
    private Integer end;
    @SerializedName("fix")
    private String fix;
    @SerializedName("original")
    private String original;

    public String getOriginal() {
        return original;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

    public String getBuggy() {
        return buggy;
    }

    public void setBuggy(String buggy) {
        this.buggy = buggy;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    public String getFix() {
        return fix;
    }

    public void setFix(String fix) {
        this.fix = fix;
    }

}
