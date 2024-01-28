package root.generation.transformation.visitor;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.modules.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.List;

public class EqualVisitor extends VoidVisitorAdapter<List<Node>> {

    public String label;

    public EqualVisitor(String label) {
        this.label = shortS(label);
    }

    private String shortS(String s) {
        return s.replaceAll("\\s", "").replaceAll(";", "");
    }

    private void compare(Node n, List<Node> arg) {
        if (shortS(n.toString()).equals(label))
            arg.add(n);
    }

    @Override
    public void visit(NodeList n, List<Node> arg) {
        for (Object nn : n) {
            compare((Node) nn, arg);
        }
    }

    @Override
    public void visit(AnnotationDeclaration n, List<Node> arg) {

    }

    @Override
    public void visit(AnnotationMemberDeclaration n, List<Node> arg) {

    }

    @Override
    public void visit(ArrayAccessExpr n, List<Node> arg) {
        compare(n, arg);
        super.visit(n, arg);
    }

    @Override
    public void visit(ArrayCreationExpr n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(ArrayCreationLevel n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(ArrayInitializerExpr n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(ArrayType n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(AssertStmt n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(AssignExpr n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(BinaryExpr n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(BlockComment n, List<Node> arg) {

    }

    @Override
    public void visit(BlockStmt n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(BooleanLiteralExpr n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(BreakStmt n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(CastExpr n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(CatchClause n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(CharLiteralExpr n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(ClassExpr n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(ClassOrInterfaceType n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(CompilationUnit n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(ConditionalExpr n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(ConstructorDeclaration n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(ContinueStmt n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(DoStmt n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(DoubleLiteralExpr n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(EmptyStmt n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(EnclosedExpr n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(EnumConstantDeclaration n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(EnumDeclaration n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(ExplicitConstructorInvocationStmt n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(ExpressionStmt n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(FieldAccessExpr n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(FieldDeclaration n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(ForStmt n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(ForEachStmt n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(IfStmt n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(ImportDeclaration n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(InitializerDeclaration n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(InstanceOfExpr n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(IntegerLiteralExpr n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(IntersectionType n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(JavadocComment n, List<Node> arg) {

    }

    @Override
    public void visit(LabeledStmt n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(LambdaExpr n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(LineComment n, List<Node> arg) {

    }

    @Override
    public void visit(LocalClassDeclarationStmt n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(LocalRecordDeclarationStmt n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(LongLiteralExpr n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(MarkerAnnotationExpr n, List<Node> arg) {

    }

    @Override
    public void visit(MemberValuePair n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(MethodCallExpr n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(MethodDeclaration n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(MethodReferenceExpr n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(NameExpr n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(Name n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(NormalAnnotationExpr n, List<Node> arg) {

    }

    @Override
    public void visit(NullLiteralExpr n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(ObjectCreationExpr n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(PackageDeclaration n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(Parameter n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(PrimitiveType n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(RecordDeclaration n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(CompactConstructorDeclaration n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(ReturnStmt n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(SimpleName n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(SingleMemberAnnotationExpr n, List<Node> arg) {

    }

    @Override
    public void visit(StringLiteralExpr n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(SuperExpr n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(SwitchEntry n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(SwitchStmt n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(SynchronizedStmt n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(ThisExpr n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(ThrowStmt n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(TryStmt n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(TypeExpr n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(TypeParameter n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(UnaryExpr n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(UnionType n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(UnknownType n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(VariableDeclarationExpr n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(VariableDeclarator n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(VoidType n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(WhileStmt n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(WildcardType n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(ModuleDeclaration n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(ModuleRequiresDirective n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(ModuleExportsDirective n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(ModuleProvidesDirective n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(ModuleUsesDirective n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(ModuleOpensDirective n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(UnparsableStmt n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(ReceiverParameter n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(VarType n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(Modifier n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(SwitchExpr n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(TextBlockLiteralExpr n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(YieldStmt n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }

    @Override
    public void visit(PatternExpr n, List<Node> arg) {
        compare(n, arg);super.visit(n, arg);
    }
}
