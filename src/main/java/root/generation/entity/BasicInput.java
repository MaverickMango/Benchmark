package root.generation.entity;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import root.generation.transformation.visitor.PrimitiveVisitor;

import java.util.*;

/**
 * 需要变异的参数就在assert语句当中
 */
public class BasicInput extends Input {

    private VoidVisitorAdapter<List<Expression>> visitor;

    public BasicInput(MethodCallExpr methodCallExpr, Expression inputExpr, int argIdx) {
        super(methodCallExpr, inputExpr, argIdx);
        visitor = new PrimitiveVisitor();
        initBasicExpr(inputExpr);
    }

    public BasicInput(MethodCallExpr methodCallExpr, Expression inputExpr, String type, int argIdx) {
        super(methodCallExpr, inputExpr, type, argIdx);
        visitor = new PrimitiveVisitor();
        initBasicExpr(inputExpr);
    }

    private void initBasicExpr(Expression inputExpr) {
        List<Expression> collector = new ArrayList<>();
        inputExpr.accept(visitor, collector);
        List<Expression> tmp = new ArrayList<>(collector);
        this.basicExpr = tmp;
    }

}
