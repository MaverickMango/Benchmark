package root.generation.transformation.visitor;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.quality.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class DependencyVisitor extends VoidVisitorAdapter<Set<Expression>> {

    private final List<String> nameExprs;
    private final VoidVisitorAdapter<Set<Expression>> visitor;

    public DependencyVisitor(@NotNull List<String> nameExprs) {
        this.nameExprs = nameExprs;
        this.visitor = new PrimitiveVisitor();
    }

    @Override
    public void visit(AssignExpr n, Set<Expression> arg) {
        super.visit(n, arg);
        Expression target = n.getTarget();
        if (nameExprs.contains(target.toString())) {
            n.getValue().accept(visitor, arg);
        }
    }

    @Override
    public void visit(MethodCallExpr n, Set<Expression> arg) {
        super.visit(n, arg);
        Optional<Expression> scope = n.getScope();
        if (scope.isPresent() && nameExprs.contains(scope.get().toString())) {
            n.accept(visitor, arg);
        }
    }

    @Override
    public void visit(VariableDeclarator n, Set<Expression> arg) {
        super.visit(n, arg);
        SimpleName name = n.getName();
        if (nameExprs.contains(name.toString())) {
            Optional<Expression> initializer = n.getInitializer();
            initializer.ifPresent(expression -> expression.accept(visitor, arg));
        }
    }

}
