package root.entities.ci;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Actions {
    /*
    {
"add_classes": false,
"add_functions": false,
"delete_classes": false,
"delete_functions": false,
"line_level_operations": ["UPDATE", "INSERT"],
"operation_types": [{"type": "insert", "from": "statement", "to": "block"}]
}
     */
    @SerializedName("add_classes")
    private NameList addClasses;
    @SerializedName("delete_classes")
    private NameList deleteClasses;
    //函数的比较是在update class的时候计算的
    @SerializedName("add_functions")
    private NameList addFunctions;
    @SerializedName("delete_functions")
    private NameList deleteFunctions;

    public NameList getAddClasses() {
        return addClasses;
    }

    public void setAddClasses(List<String> addClasses) {
        this.addClasses = new NameList(addClasses);
    }

    public NameList getDeleteClasses() {
        return deleteClasses;
    }

    public void setDeleteClasses(List<String> deleteClasses) {
        this.deleteClasses = new NameList(deleteClasses);
    }

    public NameList getAddFunctions() {
        return addFunctions;
    }

    public void setAddFunctions(List<String> addFunctions) {
        this.addFunctions = new NameList(addFunctions);
    }

    public NameList getDeleteFunctions() {
        return deleteFunctions;
    }

    public void setDeleteFunctions(List<String> deleteFunctions) {
        this.deleteFunctions = new NameList(deleteFunctions);
    }
}
