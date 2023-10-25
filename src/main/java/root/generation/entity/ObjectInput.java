package root.generation.entity;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;

/**
 * inputExpr是Object类型，需要去寻找其涉及的变量的声明或赋值中存在的基本表达式
 */
public class ObjectInput extends Input{

    private Expression basicExpr;//实际进行变异的内容

    public ObjectInput(MethodCallExpr methodCallExpr, Expression inputExpr, int argIdx) {
        super(methodCallExpr, inputExpr, argIdx);
    }

    public ObjectInput(MethodCallExpr methodCallExpr, Expression inputExpr, String type, int argIdx) {
        super(methodCallExpr, inputExpr, type, argIdx);
    }
}
