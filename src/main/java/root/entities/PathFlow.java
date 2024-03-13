package root.entities;

import com.google.gson.annotations.SerializedName;
import root.util.FileUtils;

import java.util.*;

public class PathFlow {
    @SerializedName("pathConditions")
    List<String> pathConditions;
    @SerializedName("dataFlow")
    List<String> dataFlow;
    @SerializedName("declarators")
    Set<String> declarators;
    @SerializedName("variables")
    Set<String> variables;
    @SerializedName("mappingVars")
    Map<String, Set<String>> mappingVars;//key should be the invocation name, value should be like: [RETURN#|PAR#{0/1/2...}]#${variable}, ${varibale} is in ${variables}

    public PathFlow() {
        pathConditions = new ArrayList<>();
        dataFlow = new ArrayList<>();
        variables = new HashSet<>();
        declarators = new HashSet<>();
        mappingVars = new HashMap<>();
    }

    public void reset() {
        pathConditions = new ArrayList<>();
        dataFlow = new ArrayList<>();
        variables = new HashSet<>();
        declarators = new HashSet<>();
        mappingVars = new HashMap<>();
    }

    public void setPathConditions(List<String> pathConditions) {
        this.pathConditions = pathConditions;
    }

    public void setDataFlow(List<String> dataFlow) {
        this.dataFlow = dataFlow;
    }

    public void setVariables(Set<String> variables) {
        this.variables = variables;
    }

    public void setDeclarators(Set<String> declarators) {
        this.declarators = declarators;
    }

    public void setMappingVars(Map<String, Set<String>> mappingVars) {
        this.mappingVars = mappingVars;
    }

    public Map<String, Set<String>> getMappingVars() {
        return mappingVars;
    }

    public void addMappingVars(String key, String value) {
        if (!mappingVars.containsKey(key)) {
            mappingVars.put(key, new HashSet<>());
        }
        this.mappingVars.get(key).add(value);
    }

    public Set<String> getMappingVar(String key) {
        return this.mappingVars.get(key);
    }

    public void removeMappingVar(String key, String value) {
        Set<String> strings = this.mappingVars.get(key);
        strings.remove(value);
        if (strings.isEmpty()) {
            this.mappingVars.remove(key);
        }
    }

    public List<String> getPathConditions() {
        return pathConditions;
    }

    public Set<String> getVariables() {
        return variables;
    }

    public Set<String> getDeclarators() {
        return declarators;
    }

    public List<String> getDataFlow() {
        return dataFlow;
    }

    public void addCondition(String condition) {
        this.pathConditions.add(0, condition);
    }

    public void addVariable(String variable) {
        this.variables.add(variable);
    }

    public void addDeclarator(String declarator) {
        this.declarators.add(declarator);
    }

    public void addDataFlow(String dataFlow) {
        this.dataFlow.add(0, dataFlow);
    }

    @Override
    public String toString() {
        return FileUtils.bean2Json(this);
    }
}
