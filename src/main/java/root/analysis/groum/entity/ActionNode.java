package root.analysis.groum.entity;

import com.github.javaparser.ast.Node;

import java.util.ArrayList;
import java.util.List;

public class ActionNode extends AbstractNode {
    String className;
    String target;

    public ActionNode(Node node, String className, String target) {
        super(node);
        this.className = className;
        this.target = target;
        setLabel(null);
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }


    /**
     * C.m(label), where C means the scope, m means the target(a method or field name)
     * @param label <- NULL, never be used
     */
    @Override
    public void setLabel(String label) {
        this.label = className + "#" + target;
    }

}
