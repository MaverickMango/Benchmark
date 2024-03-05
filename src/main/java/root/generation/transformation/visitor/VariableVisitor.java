package root.generation.transformation.visitor;

import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import root.entities.PathCondition;

public class VariableVisitor  extends VoidVisitorAdapter<PathCondition> {

    public VariableVisitor() {
    }

    @Override
    public void visit(NameExpr n, PathCondition arg) {
        arg.addVariable(n.toString());
    }
}
