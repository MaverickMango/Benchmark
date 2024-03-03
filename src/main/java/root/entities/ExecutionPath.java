package root.entities;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.generation.transformation.TransformHelper;
import root.generation.transformation.visitor.LineVisitor;

import java.util.ArrayList;
import java.util.List;

public class ExecutionPath {
    private static final Logger logger = LoggerFactory.getLogger(ExecutionPath.class);
    List<CallableDeclaration> mths;
    List<Integer> lineno;
    List<Node> refs;//每个行号依赖的对象

    public ExecutionPath(CompilationUnit unit, List<Integer> line) {
        this.lineno = line;
        LineVisitor visitor = new LineVisitor(lineno);
        refs = new ArrayList<>();
        mths = new ArrayList<>();
        for (Integer l: line) {//Constructor
            CallableDeclaration methodDeclaration = TransformHelper.ASTExtractor.extractMethodByLine(unit, l);
            if (methodDeclaration != null) {
                mths.add(methodDeclaration);
                methodDeclaration.accept(visitor, refs);
            } else {
                unit.accept(visitor, refs);
            }
        }
    }
}
