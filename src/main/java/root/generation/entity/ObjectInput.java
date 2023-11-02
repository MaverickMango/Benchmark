package root.generation.entity;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.quality.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.generation.transformation.visitor.DependencyVisitor;
import root.generation.transformation.visitor.PrimitiveVisitor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * inputExpr是Object类型，需要去寻找其涉及的变量的声明或赋值中存在的基本表达式
 */
public class ObjectInput extends Input{

    private final Logger logger = LoggerFactory.getLogger(ObjectInput.class);
    private VoidVisitorAdapter<List<Expression>> visitor;

    public ObjectInput(MethodCallExpr methodCallExpr, Expression inputExpr, int argIdx) {
        super(methodCallExpr, inputExpr, argIdx);
        setBasicExpr(inputExpr);
    }

    public ObjectInput(MethodCallExpr methodCallExpr, Expression inputExpr, String type, int argIdx) {
        super(methodCallExpr, inputExpr, type, argIdx);
        setBasicExpr(inputExpr);
    }

    public void setBasicExpr(@NotNull MethodDeclaration methodDeclaration) {
        List<String> all = getInputExpr().findAll(NameExpr.class).stream().
                map(nameExpr -> nameExpr.getName().toString()).collect(Collectors.toList());
        List<Expression> collector = new ArrayList<>();
        visitor = new DependencyVisitor(all);
        visitor.visit(methodDeclaration, collector);
        Random random = new Random();
        List<Expression> tmp = new ArrayList<>(collector);
        this.basicExpr = tmp.get(random.nextInt(tmp.size()));
    }

    private void setBasicExpr(Expression inputExpr) {
        List<String> all = inputExpr.findAll(NameExpr.class).stream().
                map(nameExpr -> nameExpr.getName().toString()).collect(Collectors.toList());
        List<Expression> collector = new ArrayList<>();
        visitor = new DependencyVisitor(all);
        Optional<MethodDeclaration> ancestor = inputExpr.findAncestor(MethodDeclaration.class);
        if (ancestor.isPresent()) {
            MethodDeclaration methodDeclaration = ancestor.get();
            visitor.visit(methodDeclaration, collector);
        } else {
            logger.error("Input Expression lost its ancestor node while creating objectInput! ");
            throw new IllegalArgumentException("Illegal Argument: " + inputExpr);
        }
        Random random = new Random();
        List<Expression> tmp = new ArrayList<>(collector);
        int idx = random.nextInt(tmp.size());
        this.basicExpr = tmp.get(idx);
    }
}
