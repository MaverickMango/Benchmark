package root.generation.entity;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;

/**
 * 需要变异的参数就在assert语句当中
 */
public class BasicInput extends Input {

    private Expression basicExpr;//inputExpr中的基本类型参数

    public BasicInput(MethodCallExpr methodCallExpr, Expression inputExpr, int argIdx) {
        super(methodCallExpr, inputExpr, argIdx);
    }

    public BasicInput(MethodCallExpr methodCallExpr, Expression inputExpr, String type, int argIdx) {
        super(methodCallExpr, inputExpr, type, argIdx);
    }

    public Expression getBasicExpr() {
        return basicExpr;
    }

    public void setBasicExpr(Expression inputExpr) {
        this.basicExpr = basicExpr;
    }
}
