package root.generation.entity;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.quality.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.bean.BugRepository;
import root.generation.helper.Helper;
import root.generation.helper.TransformHelper;
import root.generation.transformation.visitor.ModifiedVisitor;
import root.util.ConfigurationProperties;
import root.util.FileUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
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
    List<MethodDeclaration> generatedMethods;
    int generatedTestsIdx;
    int generatedClazzIdx;

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
        this.generatedMethods = new ArrayList<>();
        this.generatedTestsIdx = 0;
        this.generatedClazzIdx = 0;
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

    public List<MethodDeclaration> getGeneratedMethods() {
        return generatedMethods;
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

    public MethodDeclaration addStatementAtLast(Statement stmt) {
        String newMethodName = getNewMethodName(this.getOriginalMethod().getNameAsString());
        return addStatementAtLast(newMethodName, this.getOriginalMethod(), stmt);
    }

    public MethodDeclaration addStatementAtLast(String methodNewName, MethodDeclaration methodDeclaration, Statement stmt) {
        MethodDeclaration clone = methodDeclaration.clone();
        clone.setName(methodNewName);
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
        generatedMethods.add(clone);
        return clone;
    }

    public void addStatementsAtLast(List<Input> inputs) {
        for (Input newInput : inputs) {
            MethodCallExpr methodCallExpr = newInput.getMethodCallExpr();
            ExpressionStmt stmt = new ExpressionStmt(methodCallExpr);
            addStatementAtLast(stmt);
        }
    }

    public CompilationUnit getTransformedCompilationUnit(List<MethodDeclaration> methodDeclarations) {
        CompilationUnit clone = clazz.clone();
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = clone.getClassByName(clazzName).get();
        List<String> collect = classOrInterfaceDeclaration.getMethods().stream()
                .map(NodeWithSimpleName::getNameAsString).collect(Collectors.toList());
        for (MethodDeclaration methodDeclaration: methodDeclarations) {
            if (!collect.contains(methodDeclaration.getNameAsString()))
                classOrInterfaceDeclaration.addMember(methodDeclaration);
        }
        return clone;
    }

    public CompilationUnit getTransformedCompilationUnit(MethodDeclaration methodDeclaration) {
        CompilationUnit clone = clazz.clone();
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = clone.getClassByName(clazzName).get();
        List<String> collect = classOrInterfaceDeclaration.getMethods().stream()
                .map(NodeWithSimpleName::getNameAsString).collect(Collectors.toList());
        if (!collect.contains(methodDeclaration.getNameAsString()))
            classOrInterfaceDeclaration.addMember(methodDeclaration);
        //add imports
        /*
            import java.io.*;
            import java.nio.charset.StandardCharsets;
         */
        NodeList<ImportDeclaration> imports = clone.getImports();
        //todo error import name here
        ImportDeclaration importDeclaration = new ImportDeclaration("java.io.*", false, true);
        imports.add(importDeclaration);
        importDeclaration = new ImportDeclaration("java.nio.charset.StandardCharsets", false, false);
        imports.add(importDeclaration);
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
        for (MethodDeclaration method : generatedMethods) {
            addMethodAtCompilationUnit(method);
        }
    }

    private String getNewMethodName(String oldName) {
        return oldName + "_generatedTest" + generatedTestsIdx ++;
    }

    private String getNewClazzName(String oldName) {
        return oldName + "_generatedTest" + generatedClazzIdx ++;
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
            input.setTransformed(true);
        } else if (input instanceof BasicInput) {
            MethodCallExpr clone = input.getMethodCallExpr().clone();
            clone.accept(visitor, collector);
            input.setMethodCallExpr(clone);
            input.setTransformed(true);
        }
    }

    public void applyTransform(List<Input> inputs) {
        for (Input input :inputs) {
            applyTransform(input);
        }
    }

    public CompilationUnit getOracle(BugRepository bugRepository, List<Input> inputs) {
        List<MethodDeclaration> methodintrs = new ArrayList<>();
        for (Input input :inputs) {
            MethodDeclaration methodInstrumented = getMethodInstrumented(input);
            methodintrs.add(methodInstrumented);
        }
        CompilationUnit transformedCompilationUnit = getTransformedCompilationUnit(methodintrs);

        Optional<PackageDeclaration> packageDeclaration = transformedCompilationUnit.getPackageDeclaration();
        AtomicReference<String> pack = new AtomicReference<>("");
        packageDeclaration.ifPresent(p -> pack.set(p.getNameAsString()));
        StringBuilder testName = new StringBuilder();
        for (MethodDeclaration method :methodintrs) {
            testName.append(pack.get() + "." + getClazzName() + "::" + method.getNameAsString() + " ");
        }
        FileUtils.writeToFile(transformedCompilationUnit.toString(), getAbsolutePath(), false);
        bugRepository.test(testName.toString());
        inputs.forEach(input -> input.setCompleted(true));
        return transformedCompilationUnit;
    }

    public String getTestNamePrefix(CompilationUnit compilationUnit, MethodDeclaration methodDeclaration) {
        Optional<PackageDeclaration> packageDeclaration = compilationUnit.getPackageDeclaration();
        AtomicReference<String> pack = new AtomicReference<>("");
        packageDeclaration.ifPresent(p -> pack.set(p.getNameAsString()));
        String testName = pack.get() + "." + getClazzName() + "::" + methodDeclaration.getNameAsString();
        return testName;
    }

    public CompilationUnit getOracle(BugRepository bugRepository, Input input) {
        MethodDeclaration methodInstrumented = getMethodInstrumented(input);
        //这个时候不需要插入断言
//        MethodDeclaration methodDeclaration = addStatementAtLast(input.getMethodCallExpr());
        CompilationUnit transformedCompilationUnit = getTransformedCompilationUnit(methodInstrumented);

        String testName = getTestNamePrefix(transformedCompilationUnit, methodInstrumented);
        FileUtils.writeToFile(transformedCompilationUnit.toString(), getAbsolutePath(), false);
        bugRepository.test(testName);
        input.setCompleted(true);
        return transformedCompilationUnit;
    }

    public Statement getSkeletonStmt(CompilationUnit compilationUnit, MethodDeclaration methodDeclaration, Expression expression) {
        String filePath = ConfigurationProperties.getProperty("location") + File.separator +
                getClazzName() + "_generatedOracles.txt";
        EnclosedExpr expr = new EnclosedExpr();
        BinaryExpr binaryExpr = new BinaryExpr();
        binaryExpr.setOperator(BinaryExpr.Operator.PLUS);
        binaryExpr.setRight(expression);
        StringLiteralExpr stringLiteralExpr = new StringLiteralExpr(getTestNamePrefix(compilationUnit, methodDeclaration) + "::");
        binaryExpr.setLeft(stringLiteralExpr);
        expr.setInner(binaryExpr);
        return Helper.constructFileOutputStmt2Instr(filePath, expr);
    }

    private MethodDeclaration getMethodInstrumented(Input input) {
        //插入一个输出语句用于获取变量在original版本的oracle。
        Expression inputExpr = input.getInputExpr();
        MethodDeclaration methodDeclaration = this.getOriginalMethod();
        if (input instanceof ObjectInput) {
            methodDeclaration = this.getTransformedMethod();
        }
        inputExpr = input.getMethodCallExpr().getArgument(input.getArgIdx());
        Statement stmt = getSkeletonStmt(clazz, methodDeclaration, inputExpr);
        String newMethodName = getNewMethodName(methodDeclaration.getNameAsString());
        MethodDeclaration methodInstrumented = addStatementAtLast(newMethodName, methodDeclaration, stmt);
        return methodInstrumented;
    }
}
