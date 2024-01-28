package root.entities.otherdataset;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Classification {
    @SerializedName("build")
    private String build;
    @SerializedName("code")
    private String code;
    @SerializedName("exceptions")
    private List<String> exceptions;
    @SerializedName("test")
    private String test;

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<String> getExceptions() {
        return exceptions;
    }

    public void setExceptions(List<String> exceptions) {
        this.exceptions = exceptions;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }
}
