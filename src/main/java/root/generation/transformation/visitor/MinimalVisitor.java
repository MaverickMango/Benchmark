package root.generation.transformation.visitor;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.*;

public class MinimalVisitor extends VoidVisitorAdapter<List<Node>> {

    private List<Node> subNode;//root排除掉目标节点后剩下的节点
    private final Node root;
    private Node target;
    
    boolean terminate = false;

    public MinimalVisitor(Node root) {
        this.root = root;
        this.target = root;
    }
    
    public List<Node> minimalByTarget(Node target) {
        if (shortS(target).contains(root.toString())) {
            List<Node> arg = new ArrayList<>();
            arg.add(target);
            target.accept(this, arg);
            return arg;//todo?如果有多个是什么情况
        }
        return Collections.singletonList(target);
    }

    public List<Node> getSubNode() {
        List<Node> res = subNode;
        subNode = null;
        if (!terminate)
            return Collections.singletonList(root);
        return res;
    }

    private String shortS(Node n) {
        String s = n.toString();
        if (s.startsWith("(") && s.endsWith(")"))
            s = s.substring(1, s.length() - 1);
        return s;
    }
    private boolean terminateFilter(Node currentNode, List<Node> arg) {
        if (shortS(currentNode).equals(shortS(target)))//当前节点等于target节点，不需要遍历其子节点
            return true;
        boolean contains = shortS(currentNode).contains(shortS(target));
        if (!contains && currentNode.getParentNode().isPresent()) {
            //父节点包含target，当前节点不包含target， 其必然有某些子节点组成了target
            Node parent = currentNode.getParentNode().get();
            List<Node> childNodes = new ArrayList<>(parent.getChildNodes());
            for (Node node : parent.getChildNodes()) {
                if (shortS(node).equals(shortS(target))) {
                    //组成target的刚好是当前节点的兄弟节点
                    childNodes.remove(node);
                    break;
                }
            }
            if (childNodes.size() != parent.getChildNodes().size()) {
                terminate = true;
                subNode = null;
                arg.remove(0);
                arg.addAll(childNodes);
            } else {
                //不是兄弟节点组成的target，把current当root，继续minimal
                MinimalVisitor visitor = new MinimalVisitor(currentNode);
                List<Node> left = visitor.minimalByTarget(target);
                this.terminate = visitor.terminate;
                this.subNode = left;
                arg.remove(0);
                arg.addAll(visitor.getSubNode());
            }
        }
        return !contains;
    }

    @Override
    public void visit(ArrayAccessExpr n, List<Node> arg) {
        List<Node> nodes = arg;
        if (terminateFilter(n, nodes) || terminate)
            return;
        super.visit(n, nodes);
    }

    @Override
    public void visit(ArrayCreationExpr n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(ArrayInitializerExpr n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(AssertStmt n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(AssignExpr n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(BinaryExpr n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(BlockStmt n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(BooleanLiteralExpr n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(BreakStmt n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(CastExpr n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(CatchClause n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(CharLiteralExpr n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(ClassExpr n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(ClassOrInterfaceType n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(CompilationUnit n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(ConditionalExpr n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(ConstructorDeclaration n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(ContinueStmt n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(DoStmt n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(DoubleLiteralExpr n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(EmptyStmt n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(EnclosedExpr n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(EnumConstantDeclaration n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(EnumDeclaration n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(ExplicitConstructorInvocationStmt n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(ExpressionStmt n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(FieldAccessExpr n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(FieldDeclaration n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(ForEachStmt n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(ForStmt n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(IfStmt n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(InitializerDeclaration n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(InstanceOfExpr n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(IntegerLiteralExpr n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(JavadocComment n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(LabeledStmt n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(LongLiteralExpr n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(MemberValuePair n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(MethodCallExpr n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(MethodDeclaration n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(NameExpr n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(NullLiteralExpr n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(ObjectCreationExpr n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(PackageDeclaration n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(Parameter n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(PrimitiveType n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(Name n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(SimpleName n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(ArrayType n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(ArrayCreationLevel n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(IntersectionType n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(UnionType n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(ReturnStmt n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(SingleMemberAnnotationExpr n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(StringLiteralExpr n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(SuperExpr n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(SwitchEntry n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(SwitchStmt n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(SynchronizedStmt n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(ThisExpr n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(ThrowStmt n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(TryStmt n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(LocalClassDeclarationStmt n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(LocalRecordDeclarationStmt n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(TypeParameter n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(UnaryExpr n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(UnknownType n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(VariableDeclarationExpr n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(VariableDeclarator n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(WhileStmt n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(LambdaExpr n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(MethodReferenceExpr n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(TypeExpr n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(NodeList n, List<Node> arg) {
        boolean flag = false;
        for (Object node :n) {
            if (terminateFilter((Node) node, arg)) {
                flag = true;
                break;
            }
        }
        if (flag)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(ImportDeclaration n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(UnparsableStmt n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(ReceiverParameter n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(Modifier n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(SwitchExpr n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(TextBlockLiteralExpr n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(PatternExpr n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(RecordDeclaration n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }

    @Override
    public void visit(CompactConstructorDeclaration n, List<Node> arg) {
        if (terminateFilter(n, arg) || terminate)
            return;
        super.visit(n, arg);
    }
}
