package root.generation.entity;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import root.generation.helper.MutatorHelper;

import java.util.Objects;

/**
 * test input(method call expression's argument)
 */
public class Input {

    MethodCallExpr methodCallExpr;
    String type;
    Expression inputExpr;
    boolean isPrimitive;

    public Input(MethodCallExpr methodCallExpr, Expression inputExpr) {
        this.methodCallExpr = methodCallExpr;
        this.inputExpr = Objects.requireNonNull(inputExpr);
        //todo: how to get its type if type cannot be resolved?
        this.type = inputExpr.calculateResolvedType().asReferenceType().getQualifiedName();
        setPrimitive(type);
    }

    public Input(MethodCallExpr methodCallExpr, Expression inputExpr, String type) {
        this.methodCallExpr = methodCallExpr;
        this.type = Objects.requireNonNull(type);
        this.inputExpr = Objects.requireNonNull(inputExpr);
        setPrimitive(type);
    }

    public MethodCallExpr getMethodCallExpr() {
        return methodCallExpr;
    }

    public String getType() {
        return type;
    }

    public Expression getInputExpr() {
        return inputExpr;
    }

    public boolean isPrimitive() {
        return isPrimitive;
    }

    private void setPrimitive(String type) {
        isPrimitive = MutatorHelper.isKnownType(type) && !"Object".equals(type) && !"java.lang.Object".equals(type);
    }

    @Override
    public String toString() {
        return "Input{" +
                "type='" + type + '\'' +
                ", inputExpr=" + inputExpr +
                '}';
    }
}
