package root.entities.otherdataset;

import com.google.gson.annotations.SerializedName;

public class Patches {
    @SerializedName("remove-ppa")
    private String removeppa;

    public String getRemoveppa() {
        return removeppa;
    }

    public void setRemoveppa(String removeppa) {
        this.removeppa = removeppa;
    }
}
