package root.entities.ci;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NameList {
    @SerializedName("qualified_names")
    private List<String> qualifiedNames;
    @SerializedName("num")
    private Integer num;

    public NameList(List<String> qualifiedNames) {
        this.qualifiedNames = qualifiedNames;
        this.num = qualifiedNames.size();
    }

    public List<String> getQualifiedNames() {
        return qualifiedNames;
    }

    public void setQualifiedNames(List<String> qualifiedNames) {
        this.qualifiedNames = qualifiedNames;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }
}