package root.entities.ci;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PatchDiff {
    @SerializedName("changed_type")
    String changedType;//UPDATE和ADD以及DELETE
    @SerializedName("changed_class")
    List<String> changedClass;//始终是两个，前一个是修改前，后一个是修改后
    @SerializedName("changed_functions")
    List<NameList> changedFunctions;//始终是两个，前一个是修改前，后一个是修改后
    @SerializedName("changed_lines")
    List<NameList> changedLines;//除UPDATE外的两种不考虑该属性
    @SerializedName("diff")
    String diff;
    @SerializedName("operations")
    private List<Operation> operations;

    public PatchDiff(String changedType, List<String> changedClass) {
        this.changedType = changedType;
        this.changedClass = changedClass;
    }

    public String getChangedType() {
        return changedType;
    }

    public void setChangedType(String changedType) {
        this.changedType = changedType;
    }

    public List<String> getChangedClass() {
        return changedClass;
    }

    public void setChangedClass(List<String> changedClass) {
        this.changedClass = changedClass;
    }

    public List<Operation> getOperations() {
        return operations;
    }

    public void setOperations(List<Operation> operations) {
        this.operations = operations;
    }

    public List<NameList> getChangedFunctions() {
        return changedFunctions;
    }

    public void setChangedFunctions(List<NameList> changedFunctions) {
        this.changedFunctions = changedFunctions;
    }

    public List<NameList> getChangedLines() {
        return changedLines;
    }

    public void setChangedLines(List<NameList> changedLines) {
        this.changedLines = changedLines;
    }

    public String getDiff() {
        return diff;
    }

    public void setDiff(String diff) {
        this.diff = diff;
    }
}
