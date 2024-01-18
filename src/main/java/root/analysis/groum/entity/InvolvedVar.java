package root.analysis.groum.entity;

import com.github.javaparser.ast.Node;

public class InvolvedVar {
    String className;
    String target;

    public InvolvedVar(String className, String target) {
        this.className = className;
        this.target = target;
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
     */
    @Override
    public String toString() {
        return className + "#" + target;
    }
}
