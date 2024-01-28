package root.entities.otherdataset;

import com.google.gson.annotations.SerializedName;

public class Matrix {
    @SerializedName("platform")
    private String platform;

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
