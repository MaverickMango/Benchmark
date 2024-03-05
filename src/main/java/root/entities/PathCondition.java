package root.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PathCondition {
    List<String> conditions;
    List<String> dataFlows;
    Set<String> variables;

    public PathCondition() {
        conditions = new ArrayList<>();
        dataFlows = new ArrayList<>();
        variables = new HashSet<>();
    }

    public List<String> getConditions() {
        return conditions;
    }

    public Set<String> getVariables() {
        return variables;
    }

    public List<String> getDataFlows() {
        return dataFlows;
    }

    public void addCondition(String condition) {
        this.conditions.add(condition);
    }

    public void addVariable(String variable) {
        this.variables.add(variable);
    }

    public void addDataFlow(String dataFlow) {this.dataFlows.add(dataFlow);}
}
