package root.analysis.groum.extractor;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.github.javaparser.ast.stmt.LocalRecordDeclarationStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import root.analysis.groum.entity.Groum;

import java.util.ArrayList;
import java.util.List;

public class CompilationUnitVisitor extends VoidVisitorAdapter<List<Groum>> {

    PreOrderVisitorInMth preOrderVisitorInMth = new PreOrderVisitorInMth();

    @Override
    public void visit(ConstructorDeclaration n, List<Groum> arg) {
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
    public void visit(EnumDeclaration n, List<Groum> arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(InitializerDeclaration n, List<Groum> arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(MethodDeclaration n, List<Groum> arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(PackageDeclaration n, List<Groum> arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(LocalClassDeclarationStmt n, List<Groum> arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(ImportDeclaration n, List<Groum> arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(FieldDeclaration n, List<Groum> arg) {
        super.visit(n, arg);
    }
}
