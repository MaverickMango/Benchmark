package root.generation.transformation.visitor;

import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import root.entities.PathFlow;

public class VariableVisitor  extends VoidVisitorAdapter<PathFlow> {

    public VariableVisitor() {
    }

    @Override
    public void visit(NameExpr n, PathFlow arg) {
        arg.addVariable(n.toString());
    }
}
