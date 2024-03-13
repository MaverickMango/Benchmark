package root.generation.transformation.visitor;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import root.entities.PathFlow;

import java.util.Optional;

public class VariableVisitor  extends VoidVisitorAdapter<PathFlow> {

    public VariableVisitor() {
    }

    @Override
    public void visit(NameExpr n, PathFlow arg) {
        try {
            ResolvedValueDeclaration resolve = n.resolve();//能解析类型就说明是变量而不是类名？
            arg.addVariable(n.toString());
        } catch (Exception ignore) {}
    }
}
