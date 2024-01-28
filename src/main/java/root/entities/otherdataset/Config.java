package root.entities.otherdataset;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Config {

    @SerializedName("id-in-workflow")
    private String idinworkflow;
    @SerializedName("runs-on")
    private String runson;
    @SerializedName("steps")
    private List<Steps> steps;
    @SerializedName("strategy")
    private Strategy strategy;
    @SerializedName("")
    private Null _$Null65;// FIXME check this code
    @SerializedName("jdk")
    private String jdk;
    @SerializedName("language")
    private String language;
    @SerializedName("os")
    private String os;
    @SerializedName("sudo")
    private Boolean sudo;

    public String getIdinworkflow() {
        return idinworkflow;
    }

    public void setIdinworkflow(String idinworkflow) {
        this.idinworkflow = idinworkflow;
    }

    public String getRunson() {
        return runson;
    }

    public void setRunson(String runson) {
        this.runson = runson;
    }

    public List<Steps> getSteps() {
        return steps;
    }

    public void setSteps(List<Steps> steps) {
        this.steps = steps;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public Null get_$Null65() {
        return _$Null65;
    }

    public void set_$Null65(Null _$Null65) {
        this._$Null65 = _$Null65;
    }

    public String getJdk() {
        return jdk;
    }

    public void setJdk(String jdk) {
        this.jdk = jdk;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public Boolean getSudo() {
        return sudo;
    }

    public void setSudo(Boolean sudo) {
        this.sudo = sudo;
    }
}
