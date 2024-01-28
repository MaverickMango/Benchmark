package root.entities.otherdataset;

import com.google.gson.annotations.SerializedName;

public class ReproducibilityStatus {
    @SerializedName("status")
    private String status;
    @SerializedName("time_stamp")
    private String timeStamp;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
