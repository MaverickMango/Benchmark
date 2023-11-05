package root.generation.entity;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.quality.NotNull;
import com.github.javaparser.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.generation.helper.Helper;
import root.generation.helper.MutatorHelper;

import java.util.List;
import java.util.Map;

/**
 * <s>test input(method call expression's argument)</s>
 */
public abstract class Input implements Cloneable{

    private final Logger logger = LoggerFactory.getLogger(Input.class);
    String identifier;
    MethodCallExpr methodCallExpr;
    Expression inputExpr;//assert语句的actual参数
    String type;
    int argIdx;
    boolean isPrimitive;
    boolean isCompleted;//是否需要到original版本获取断言，获取完或者不需要的为completed
    List<Expression> basicExpr;//实际进行变异的内容,如果是basicInput则和inputExpr一致
    Pair<Expression, Expression> mutatedExpr;//<key, value>: basicExpr, mutated
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
        this.identifier = methodCallExpr.getRange().toString() + "_" + argIdx;
    }

    public String getIdentifier() {
        return identifier;
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

    public void setInputExpr(Expression inputExpr) {
        this.inputExpr = inputExpr;
    }

    public void setBasicExpr(List<Expression> basicExpr) {
        this.basicExpr = basicExpr;
    }

    public void setMutatedExpr(Pair<Expression, Expression> mutatedExpr) {
        this.mutatedExpr = mutatedExpr;
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

    public List<Expression> getBasicExpr() {
        return basicExpr;
    }

    public Pair<Expression, Expression> getTransformed() {
        return mutatedExpr;
    }

    public void setBasicExprTransformed(Expression baicExpr, Expression newInputExpr) {
        this.mutatedExpr = new Pair<>(baicExpr, newInputExpr);
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

    @Override
    public Input clone() {
        try {
            Input clone = (Input) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            clone.setInputExpr(inputExpr.clone());
            clone.setMethodCallExpr(methodCallExpr.clone());
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
