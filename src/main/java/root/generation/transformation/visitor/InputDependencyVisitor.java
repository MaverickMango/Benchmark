package root.generation.transformation.visitor;

import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.quality.NotNull;

import java.util.List;
import java.util.Optional;

public class InputDependencyVisitor extends VoidVisitorAdapter<List<Expression>> {

    private final List<String> nameExprs;
    private final VoidVisitorAdapter<List<Expression>> visitor;

    public InputDependencyVisitor(@NotNull List<String> nameExprs) {
        this.nameExprs = nameExprs;
        this.visitor = new PrimitiveVisitor();
    }

    @Override
    public void visit(AssignExpr n, List<Expression> arg) {
        super.visit(n, arg);
        Expression target = n.getTarget();
        if (nameExprs.contains(target.toString())) {
            n.getValue().accept(visitor, arg);
        }
    }

    @Override
    public void visit(MethodCallExpr n, List<Expression> arg) {
        super.visit(n, arg);
        Optional<Expression> scope = n.getScope();
        if (scope.isPresent() && nameExprs.contains(scope.get().toString())) {
            n.accept(visitor, arg);
        }
    }

    @Override
    public void visit(VariableDeclarator n, List<Expression> arg) {
        super.visit(n, arg);
        SimpleName name = n.getName();
        if (nameExprs.contains(name.toString())) {
            Optional<Expression> initializer = n.getInitializer();
            initializer.ifPresent(expression -> expression.accept(visitor, arg));
        }
    }

}
