package root.entities.ci;

import com.google.gson.annotations.SerializedName;

public class Operation {
    /*
    {"type": "insert", "from": "statement", "to": "block"}
     */
    @SerializedName("type")
    private String type;
    @SerializedName("from")
    private String from = "";
    @SerializedName("to")
    private String to = "";

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}