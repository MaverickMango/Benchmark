package root.analysis.groum.entity;

import com.github.javaparser.ast.Node;

import java.util.ArrayList;
import java.util.List;

public class ControlNode extends AbstractNode {

    Type type;
    List<AbstractNode> scope;//表示该控制节点影响的所有节点

    public ControlNode(Node node, Type type) {
        super(node);
        this.scope = new ArrayList<>();
        this.type = type;
        setLabel(type.getAbbreviation());
    }

    public List<AbstractNode> getScope() {
        return scope;
    }

    public void addScope(AbstractNode scope) {
        this.scope.add(scope);
    }

    public void addScope(List<AbstractNode> scopes) {
        if (scopes == null)
            return;
        this.scope.addAll(scopes);
    }

    public enum Type {
        IF("if"),
        WHILE("while"),
        FOR("for"),
        TRY("try"),
        RETURN("return"),
        Throw("throw");

        private final String abbreviation;

        Type(String abbreviation) {
            this.abbreviation = abbreviation;
        }

        public String getAbbreviation() {
            return abbreviation;
        }
    }
}
