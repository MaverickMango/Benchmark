package root.entities.otherdataset;

import com.google.gson.annotations.SerializedName;

public class Null {
    @SerializedName("result")
    private String result;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
