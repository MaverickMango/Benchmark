package root.generation.transformation.visitor;

import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.List;
import java.util.Set;

/**
 * 对应mutator的数据类型
 */
public class PrimitiveVisitor extends VoidVisitorAdapter<List<Expression>> {

    @Override
    public void visit(BooleanLiteralExpr n, List<Expression> arg) {
        super.visit(n, arg);
        arg.add(n);
    }

    @Override
    public void visit(DoubleLiteralExpr n, List<Expression> arg) {
        super.visit(n, arg);
        arg.add(n);
    }

    @Override
    public void visit(IntegerLiteralExpr n, List<Expression> arg) {
        super.visit(n, arg);
        arg.add(n);
    }

    @Override
    public void visit(LongLiteralExpr n, List<Expression> arg) {
        super.visit(n, arg);
        arg.add(n);
    }

    @Override
    public void visit(StringLiteralExpr n, List<Expression> arg) {
        super.visit(n, arg);
        arg.add(n);
    }

    @Override
    public void visit(CharLiteralExpr n, List<Expression> arg) {
        super.visit(n, arg);
        arg.add(n);
    }

    @Override
    public void visit(UnaryExpr n, List<Expression> arg) {
        super.visit(n, arg);
        arg.add(n);
    }
}
