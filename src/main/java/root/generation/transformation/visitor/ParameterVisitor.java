package root.generation.transformation.visitor;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import root.entities.PathFlow;
import root.generation.helper.Helper;

import java.util.Optional;

public class ParameterVisitor extends VoidVisitorAdapter<PathFlow> {

    @Override
    public void visit(ConstructorDeclaration n, PathFlow arg) {
//        n.getBody().accept(this, arg);
//        n.getModifiers().forEach(p -> p.accept(this, arg));
//        n.getName().accept(this, arg);
        build(n.getNameAsString(), n.getParameters(), arg);
//        n.getReceiverParameter().ifPresent(l -> l.accept(this, arg));
//        n.getThrownExceptions().forEach(p -> p.accept(this, arg));
//        n.getTypeParameters().forEach(p -> p.accept(this, arg));
//        n.getAnnotations().forEach(p -> p.accept(this, arg));
//        n.getComment().ifPresent(l -> l.accept(this, arg));
    }

    @Override
    public void visit(MethodDeclaration n, PathFlow arg) {
//        n.getType().accept(this, arg);
        String name = n.getNameAsString();
        //return 的映射在PathVistor中完成
//        String descriptor = n.getType().toDescriptor();
//        if (!"V".equals(descriptor)) {;
//            n.getBody().ifPresent(l -> {
//                l.getStatements().forEach(statement -> {
//                    statement.accept(this, arg);
//                });
//            });
//        }

//        n.getModifiers().forEach(p -> p.accept(this, arg));
//        n.getName().accept(this, arg);
        build(name, n.getParameters(), arg);
//        n.getReceiverParameter().ifPresent(l -> l.accept(this, arg));
//        n.getThrownExceptions().forEach(p -> p.accept(this, arg));
//        n.getTypeParameters().forEach(p -> p.accept(this, arg));
//        n.getAnnotations().forEach(p -> p.accept(this, arg));
//        n.getComment().ifPresent(l -> l.accept(this, arg));
    }

    private void build(String mth, NodeList<Parameter> pars, PathFlow arg) {
        for (int i = 0; i < pars.size(); i ++) {
            Parameter p = pars.get(i);
            if (p.getType().isReferenceType()) {
                String name = p.getNameAsString();
                if (arg.getVariables().contains(name)) {
                    arg.addMappingVars(mth, "PAR#" + i + "#" + name);
                }
            }
        }
    }
}
