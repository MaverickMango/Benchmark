package root.generation.transformation.visitor;

import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.List;
import java.util.Set;

public class PrimitiveVisitor extends VoidVisitorAdapter<Set<Expression>> {

    @Override
    public void visit(BooleanLiteralExpr n, Set<Expression> arg) {
        super.visit(n, arg);
        arg.add(n);
    }

    @Override
    public void visit(DoubleLiteralExpr n, Set<Expression> arg) {
        super.visit(n, arg);
        arg.add(n);
    }

    @Override
    public void visit(IntegerLiteralExpr n, Set<Expression> arg) {
        super.visit(n, arg);
        arg.add(n);
    }

    @Override
    public void visit(LongLiteralExpr n, Set<Expression> arg) {
        super.visit(n, arg);
        arg.add(n);
    }

    @Override
    public void visit(StringLiteralExpr n, Set<Expression> arg) {
        super.visit(n, arg);
        arg.add(n);
    }

    @Override
    public void visit(CharLiteralExpr n, Set<Expression> arg) {
        super.visit(n, arg);
        arg.add(n);
    }

}
