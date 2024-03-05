package root.entities;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.CallableDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.generation.transformation.visitor.LineVisitor;

import java.util.ArrayList;
import java.util.List;

public class ExecutionPathInMth {
    private static final Logger logger = LoggerFactory.getLogger(ExecutionPathInMth.class);
    CallableDeclaration mth;
    List<Integer> lineno;
    List<Node> refs;//每个行号依赖的对象

    public ExecutionPathInMth(CallableDeclaration mth) {
        this.mth = mth;
        lineno = new ArrayList<>();
    }

    public ExecutionPathInMth(CallableDeclaration mth, List<Integer> line) {
        this.lineno = line;
        LineVisitor visitor = new LineVisitor(lineno);
        refs = new ArrayList<>();
        this.mth = mth;
        mth.accept(visitor, refs);
    }

    public CallableDeclaration getMth() {
        return mth;
    }

    public void addLine(Integer line) {
        lineno.add(line);
    }

    public List<Integer> getLineno() {
        return lineno;
    }

    public List<Node> getNodes() {
        if (refs == null) {
            refs = new ArrayList<>();
            LineVisitor visitor = new LineVisitor(lineno);
            mth.accept(visitor, refs);
        }
        return refs;
    }
}
