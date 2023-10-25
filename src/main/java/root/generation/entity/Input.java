package root.generation.entity;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.quality.NotNull;
import root.generation.helper.MutatorHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * <s>test input(method call expression's argument)</s>
 */
public abstract class Input {

    private MethodCallExpr methodCallExpr;
    private final String type;
    private Expression inputExpr;//assert语句的actual参数
    private int argIdx;
    private boolean isPrimitive;
    private boolean isCompleted;//是否需要到original版本获取断言，获取完或者不需要的为completed

    public Input(@NotNull MethodCallExpr methodCallExpr,
                 @NotNull Expression inputExpr, int argIdx) {
        //todo: how to get its type if type cannot be resolved?
        this.type = inputExpr.calculateResolvedType().asReferenceType().getQualifiedName();
        setAttributes(methodCallExpr, inputExpr, argIdx);
        setPrimitive(type);
    }

    public Input(@NotNull MethodCallExpr methodCallExpr,
                 @NotNull Expression inputExpr, @NotNull String type, int argIdx) {
        this.type = type;
        setAttributes(methodCallExpr, inputExpr, argIdx);
        setPrimitive(type);
    }

    private void setAttributes(MethodCallExpr methodCallExpr,
                               Expression inputExpr, int argIdx) {
        Expression expression = methodCallExpr.getArguments().get(argIdx);
        if (!inputExpr.equals(expression)) {
            MethodCallExpr newMethodCallExpr = methodCallExpr.clone();
            newMethodCallExpr.setArgument(argIdx, inputExpr);
            this.methodCallExpr = newMethodCallExpr;
        } else {
            this.methodCallExpr = methodCallExpr;
        }
        this.inputExpr = inputExpr;
        this.argIdx = argIdx;
        this.isCompleted = methodCallExpr.getArguments().size() == 1;
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

    public int getArgIdx() {
        return argIdx;
    }

    public boolean isPrimitive() {
        return isPrimitive;
    }

    private void setPrimitive(String type) {
        isPrimitive = MutatorHelper.isKnownType(type) && !"Object".equals(type) && !"java.lang.Object".equals(type);
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void getOracle() {
        //todo:把需要获取oracle的函数放回到original版本运行
        this.isCompleted = true;
    }

    @Override
    public String toString() {
        return "Input{" +
                "type='" + type + '\'' +
                ", inputExpr=" + inputExpr +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Input) {
            Input that = (Input) obj;
            if (this.isPrimitive() != that.isPrimitive())
                return false;
            if (!this.getInputExpr().equals(that.getInputExpr()))
                return false;
            if (this.getArgIdx() != that.getArgIdx())
                return false;
            return this.getMethodCallExpr().equals(that.getMethodCallExpr());
        } else {
            return super.equals(obj);
        }
    }
}
