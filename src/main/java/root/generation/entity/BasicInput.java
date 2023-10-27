package root.generation.entity;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import javassist.expr.Expr;
import root.generation.transformation.visitor.PrimitiveVisitor;

import java.util.*;

/**
 * 需要变异的参数就在assert语句当中
 */
public class BasicInput extends Input {

    private VoidVisitorAdapter<Set<Expression>> visitor;

    public BasicInput(MethodCallExpr methodCallExpr, Expression inputExpr, int argIdx) {
        super(methodCallExpr, inputExpr, argIdx);
        visitor = new PrimitiveVisitor();
        setBasicExpr(inputExpr);
    }

    public BasicInput(MethodCallExpr methodCallExpr, Expression inputExpr, String type, int argIdx) {
        super(methodCallExpr, inputExpr, type, argIdx);
        visitor = new PrimitiveVisitor();
        setBasicExpr(inputExpr);
    }

    public void setBasicExpr(Expression inputExpr) {
        Set<Expression> collector = new HashSet<>();
        inputExpr.accept(visitor, collector);
        Random random = new Random();
        List<Expression> tmp = new ArrayList<>(collector);
        this.basicExpr = tmp.get(random.nextInt(tmp.size()));
    }

}
