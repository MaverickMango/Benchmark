package root.generation.transformation.visitor;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.List;

public class LineVisitor extends VoidVisitorAdapter<List<Node>> {
    public List<Integer> lineno;

    public LineVisitor(List<Integer> lineno) {
        this.lineno = lineno;
    }
    
    private boolean contains(int start, int end) {
        for (Integer no: lineno) {
            if (no >= start && no <= end) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void visit(DoStmt n, List<Node> arg) {
        if (n.getBegin().isPresent() && n.getEnd().isPresent()) {
            if (contains(n.getBegin().get().line, n.getEnd().get().line)) {
                arg.add(n);
            }
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(ExpressionStmt n, List<Node> arg) {
        if (n.getBegin().isPresent() && n.getEnd().isPresent()) {
            if (contains(n.getBegin().get().line, n.getEnd().get().line)) {
                arg.add(n);
            }
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(ExplicitConstructorInvocationStmt n, List<Node> arg) {
        if (n.getBegin().isPresent() && n.getEnd().isPresent()) {
            if (contains(n.getBegin().get().line, n.getEnd().get().line)) {
                arg.add(n);
            }
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(ForEachStmt n, List<Node> arg) {
        if (n.getBegin().isPresent() && n.getEnd().isPresent()) {
            if (contains(n.getBegin().get().line, n.getEnd().get().line)) {
                arg.add(n);
            }
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(ForStmt n, List<Node> arg) {
        if (n.getBegin().isPresent() && n.getEnd().isPresent()) {
            if (contains(n.getBegin().get().line, n.getEnd().get().line)) {
                arg.add(n);
            }
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(IfStmt n, List<Node> arg) {
        if (n.getBegin().isPresent() && n.getEnd().isPresent()) {
            if (contains(n.getBegin().get().line, n.getEnd().get().line)) {
                arg.add(n);
            }
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(ReturnStmt n, List<Node> arg) {
        if (n.getBegin().isPresent() && n.getEnd().isPresent()) {
            if (contains(n.getBegin().get().line, n.getEnd().get().line)) {
                arg.add(n);
            }
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(SwitchStmt n, List<Node> arg) {
        if (n.getBegin().isPresent() && n.getEnd().isPresent()) {
            if (contains(n.getBegin().get().line, n.getEnd().get().line)) {
                arg.add(n);
            }
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(WhileStmt n, List<Node> arg) {
        if (n.getBegin().isPresent() && n.getEnd().isPresent()) {
            if (contains(n.getBegin().get().line, n.getEnd().get().line)) {
                arg.add(n);
            }
        }
        super.visit(n, arg);
    }
}
