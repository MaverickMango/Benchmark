package root.entities.otherdataset;

import com.google.gson.annotations.SerializedName;

public class With {
    @SerializedName("distribution")
    private String distribution;
    @SerializedName("java-version")
    private String javaversion;

    public String getDistribution() {
        return distribution;
    }

    public void setDistribution(String distribution) {
        this.distribution = distribution;
    }

    public String getJavaversion() {
        return javaversion;
    }

    public void setJavaversion(String javaversion) {
        this.javaversion = javaversion;
    }
}
