package root.entities.otherdataset;

import com.google.gson.annotations.SerializedName;

public class Collection {
    @SerializedName("href")
    private String href;
    @SerializedName("title")
    private String title;

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
