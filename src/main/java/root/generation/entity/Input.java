package root.generation.entity;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.quality.NotNull;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.generation.helper.Helper;
import root.generation.helper.MutatorHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * <s>test input(method call expression's argument)</s>
 */
public abstract class Input {

    private final Logger logger = LoggerFactory.getLogger(Input.class);
    MethodCallExpr methodCallExpr;
    Expression inputExpr;//assert语句的actual参数
    String type;
    int argIdx;
    boolean isPrimitive;
    boolean isCompleted;//是否需要到original版本获取断言，获取完或者不需要的为completed
    Expression basicExpr;//实际进行变异的内容,如果是basicInput则和inputExpr一致
    Expression transformed;
    boolean isTransformed;

    public Input(@NotNull MethodCallExpr methodCallExpr,
                 @NotNull Expression inputExpr, int argIdx) {
        this.type = Helper.getType(inputExpr);
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
        this.isTransformed = false;
    }

    public void setMethodCallExpr(MethodCallExpr methodCallExpr) {
        this.methodCallExpr = methodCallExpr;
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

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public Expression getBasicExpr() {
        return basicExpr;
    }

    public Expression getTransformed() {
        return transformed;
    }

    public void setBasicExprTransformed(Expression newInputExpr) {
        this.transformed = newInputExpr;
    }

    public boolean isTransformed() {
        return isTransformed;
    }

    public void setTransformed(boolean transformed) {
        isTransformed = transformed;
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
