package root.analysis.groum.extractor;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import root.analysis.groum.entity.IntraGroum;

import java.util.List;

public class CompilationUnitVisitor extends VoidVisitorAdapter<List<IntraGroum>> {

    PreOrderVisitorInMth preOrderVisitorInMth = new PreOrderVisitorInMth(false);

    @Override
    public void visit(ConstructorDeclaration n, List<IntraGroum> arg) {
//        //当前节点
//        List<Groum> current = new ArrayList<>();
//        n.getBody().accept(preOrderVisitorInMth, current);
//
//        //对函数参数
////        n.getParameters().forEach(p -> {
////            p.accept(this, arg);
////        });//？？？
//
//        List<Groum> before = new ArrayList<>(arg);
//        arg.clear();
//        for (Groum X : before) {
//            for (Groum Y : current) {
//                Groum g = MergeHelper.parallelMerge(X, Y);
//                arg.add(g);
//            }
//        }
//        n.getModifiers().forEach(p -> p.accept(this, arg));
//        n.getName().accept(this, arg);
//        n.getReceiverParameter().ifPresent(l -> l.accept(this, arg));
//        n.getThrownExceptions().forEach(p -> p.accept(this, arg));
//        n.getTypeParameters().forEach(p -> p.accept(this, arg));
    }

    @Override
    public void visit(EnumDeclaration n, List<IntraGroum> arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(InitializerDeclaration n, List<IntraGroum> arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(MethodDeclaration n, List<IntraGroum> arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(PackageDeclaration n, List<IntraGroum> arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(LocalClassDeclarationStmt n, List<IntraGroum> arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(ImportDeclaration n, List<IntraGroum> arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(FieldDeclaration n, List<IntraGroum> arg) {
        super.visit(n, arg);
    }
}
