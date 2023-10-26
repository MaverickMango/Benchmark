package root.generation.entity;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.quality.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.generation.helper.Helper;
import root.generation.transformation.visitor.ModifiedVisitor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * a method declaration of test, the last statement is the one before trigger assert.
 * if all statements before are $ASSERT, Prefix will have an empty body.
 */
public class Skeleton {

    private static final Logger logger = LoggerFactory.getLogger(Skeleton.class);
    String absolutePath;
    CompilationUnit clazz;
    String clazzName;
    MethodDeclaration originalMethod;
    MethodDeclaration transformedMethod;
    boolean isSplit;//是否完成对多余断言的删除
    List<Input> inputs;
    List<MethodDeclaration> methodDeclarations;
    int generatedTestsIdx;

    public Skeleton(@NotNull String absolutePath, @NotNull CompilationUnit clazz, @NotNull MethodDeclaration originalMethod) {
        this.absolutePath = absolutePath;
        this.clazz = clazz;
        this.originalMethod = originalMethod;
        if (originalMethod.getParentNode().isEmpty()) {
            throw new IllegalArgumentException("originalMethod " + originalMethod.getNameAsString() + " doesn't have parent node!");
        }
        if (!(originalMethod.getParentNode().get() instanceof ClassOrInterfaceDeclaration)) {
            throw new IllegalArgumentException("Unsupported method type. " +
                    "May be originalMethod " + originalMethod.getNameAsString() + " is anonymous!");
        }
        this.clazzName = ((ClassOrInterfaceDeclaration) originalMethod.getParentNode().get()).getNameAsString();
        this.isSplit = false;
        this.inputs = new ArrayList<>();
        this.methodDeclarations = new ArrayList<>();
        this.generatedTestsIdx = 0;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public CompilationUnit getClazz() {
        return clazz;
    }

    public String getClazzName() {
        return clazzName;
    }

    public MethodDeclaration getOriginalMethod() {
        return originalMethod;
    }

    public MethodDeclaration getTransformedMethod() {
        return transformedMethod;
    }

    public void addInput(Input input) {
        this.inputs.add(input);
    }

    public void setInputs(List<Input> inputs) {
        this.inputs.addAll(inputs);
    }

    public List<Input> getInputs() {
        return inputs;
    }

    public boolean isSplit() {
        return isSplit;
    }

    public void splitAssert() {
        MethodDeclaration clone = this.getOriginalMethod().clone();
        (clone.getBody().get()).findAll(ExpressionStmt.class).forEach((stmt) -> {
            if (Helper.isAssertion(stmt)) {
                stmt.remove();
            }
        });
        this.originalMethod = clone;
        this.transformedMethod = clone;
        this.isSplit = true;
    }

    public MethodDeclaration addStatementAtLast(Expression expression) {
        ExpressionStmt stmt = new ExpressionStmt();
        stmt.setExpression(expression);
        MethodDeclaration clone = this.getOriginalMethod().clone();
        clone.setName(getNewMethodName(clone.getNameAsString()));
        Optional<BlockStmt> body = clone.getBody();
        if (body.isPresent()) {
            BlockStmt blockStmt = body.get();
            NodeList<Statement> statements = blockStmt.getStatements();
            statements.addLast(stmt);
        } else {
            logger.error("methodDeclaration in skeleton has been initialized with error. Empty skeleton will be create.");
            BlockStmt blockStmt = new BlockStmt(new NodeList<>());
            blockStmt.getStatements().addLast(stmt);
        }
        methodDeclarations.add(clone);
        return clone;
    }

    public void addStatementsAtLast(List<Input> inputs) {
        for (Input newInput : inputs) {
            addStatementAtLast(newInput.getMethodCallExpr());
        }
    }

    public CompilationUnit getTransformedCompilationUnit(MethodDeclaration methodDeclaration) {
        CompilationUnit clone = clazz.clone();
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = clone.getClassByName(clazzName).get();
        List<String> collect = classOrInterfaceDeclaration.getMethods().stream()
                .map(NodeWithSimpleName::getNameAsString).collect(Collectors.toList());
        if (!collect.contains(methodDeclaration.getNameAsString()))
            classOrInterfaceDeclaration.addMember(methodDeclaration);
        return clone;
    }

    public void addMethodAtCompilationUnit(MethodDeclaration methodDeclaration) {
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = clazz.getClassByName(clazzName).get();
        List<String> collect = classOrInterfaceDeclaration.getMethods().stream()
                .map(NodeWithSimpleName::getNameAsString).collect(Collectors.toList());
        if (!collect.contains(methodDeclaration.getNameAsString()))
            classOrInterfaceDeclaration.addMember(methodDeclaration);
    }

    public void addMethodsAtCompilationUnit() {
        for (MethodDeclaration method :methodDeclarations) {
            addMethodAtCompilationUnit(method);
        }
    }

    private String getNewMethodName(String oldName) {
        return oldName + "_generatedTest" + generatedTestsIdx ++;
    }

    public void applyTransform(Input input) {
        Expression inputExpr = input.getInputExpr().clone();
        Expression basicExpr = input.getBasicExpr();
        Expression transformed = input.getTransformed();
        List<Expression> collector = new ArrayList<>();
        ModifiedVisitor visitor = new ModifiedVisitor(inputExpr, basicExpr, transformed);
        if (input instanceof ObjectInput) {
            MethodDeclaration methodDeclaration = this.getTransformedMethod().clone();
            methodDeclaration.accept(visitor, collector);
            this.transformedMethod = methodDeclaration;
        } else if (input instanceof BasicInput) {
            inputExpr.accept(visitor, collector);
        }
    }

    public void applyTransform(List<Input> inputs) {
        for (Input input :inputs) {
            applyTransform(input);
        }
    }

    public CompilationUnit getOracle(Input input) {
        //插入一个输出语句用于获取变量在original版本的oracle。
        Expression expression = Helper.constructPrintStmt2Instr(input.inputExpr);
        MethodDeclaration methodInstrumented = addStatementAtLast(expression);
        //这个时候不需要插入断言
//        MethodDeclaration methodDeclaration = addStatementAtLast(input.getMethodCallExpr());
        CompilationUnit transformedCompilationUnit = getTransformedCompilationUnit(methodInstrumented);

        //todo 执行

        input.setCompleted(true);
        return transformedCompilationUnit;
    }
}
