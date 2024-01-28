package root.entities.otherdataset;

import com.google.gson.annotations.SerializedName;

public class ComponentVersions {
    @SerializedName("analyzer")
    private String analyzer;
    @SerializedName("reproducer")
    private String reproducer;

    public String getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(String analyzer) {
        this.analyzer = analyzer;
    }

    public String getReproducer() {
        return reproducer;
    }

    public void setReproducer(String reproducer) {
        this.reproducer = reproducer;
    }
}
