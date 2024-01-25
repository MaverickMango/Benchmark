package root.generation.entity;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.generation.helper.Helper;
import root.generation.transformation.TransformHelper;
import root.generation.transformation.visitor.ModifiedVisitor;
import root.util.FileUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * a method declaration of test, the last statement is the one before trigger assert.
 * if all statements before are $ASSERT, Prefix will have an empty body.
 */
public class Skeleton implements Cloneable {

    private static final Logger logger = LoggerFactory.getLogger(Skeleton.class);

    String oracleFilePath;
    String absolutePath;
    CompilationUnit clazz;
    String clazzName;
    Map<String, MethodDeclaration> originalMethod;//key: input's identifier
    Map<Input, MethodDeclaration> transformedMethod;//key: input's identifier
    boolean isSplit;//是否完成对多余断言的删除
    List<Input> inputs;
    Map<String, MethodDeclaration> generatedMethods;
    int generatedTestsIdx;
    int generatedClazzIdx;

    public Skeleton(String absolutePath, CompilationUnit clazz, String clazzName) {
        this.absolutePath = absolutePath;
        this.clazz = clazz;
        this.originalMethod = new HashMap<>();
        this.clazzName = clazzName;
        this.isSplit = false;
        this.inputs = new ArrayList<>();
        this.generatedMethods = new HashMap<>();
        this.generatedTestsIdx = 0;
        this.generatedClazzIdx = 0;
        this.oracleFilePath = TransformHelper.oracleOutputs;
        File file = new File(oracleFilePath);
        if (file.exists()) {
            file.delete();
        }
    }

    public Skeleton(String absolutePath, CompilationUnit clazz, String clazzName, List<Input> inputs) {
//        if (originalMethod.keySet().stream().anyMatch(n -> n.getParentNode().isEmpty())) {
//            throw new IllegalArgumentException("one of the originalMethod doesn't have parent node!");
//        }
//        if (originalMethod.keySet().stream().anyMatch(n -> !(n.getParentNode().get() instanceof ClassOrInterfaceDeclaration))) {
//            throw new IllegalArgumentException("Unsupported method type. May be one of the originalMethod is anonymous!");
//        }
        this.absolutePath = absolutePath;
        this.clazz = clazz;
        this.originalMethod = new HashMap<>();
        this.inputs = new ArrayList<>();
        setInputs(inputs);
        this.clazzName = clazzName;
        this.isSplit = false;
        this.generatedMethods = new HashMap<>();
        this.generatedTestsIdx = 0;
        this.generatedClazzIdx = 0;
        File file = new File(oracleFilePath);
        if (file.exists()) {
            file.delete();
        }
    }

    public String getOracleFilePath() {
        return oracleFilePath;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setClazz(CompilationUnit clazz) {
        this.clazz = clazz;
    }

    public CompilationUnit getClazz() {
        return clazz;
    }

    public String getClazzName() {
        return clazzName;
    }

    public Map<String, MethodDeclaration> getOriginalMethod() {
        return originalMethod;
    }

    public Map<Input, MethodDeclaration> getTransformedMethod() {
        return transformedMethod;
    }

    public void addInput(Input input) {
        Optional<MethodDeclaration> ancestor = input.getMethodCallExpr().findAncestor(MethodDeclaration.class);
        if (ancestor.isPresent()) {
            MethodDeclaration methodDeclaration = ancestor.get();
            this.originalMethod.putIfAbsent(input.getIdentifier(), methodDeclaration);
        }
        this.inputs.add(input);
    }

    public void setInputs(List<Input> inputs) {
        for (Input input :inputs) {
            addInput(input);
        }
    }

    public List<Input> getInputs() {
        return this.inputs;
    }

    public boolean isSplit() {
        return isSplit;
    }

    public void addGeneratedMethod(String testName, MethodDeclaration methodDeclaration) {
        generatedMethods.put(testName, methodDeclaration);
    }

    public void addGeneratedMethods(Map<String, MethodDeclaration> map) {
        generatedMethods.putAll(map);
    }

    public Map<String, MethodDeclaration> getGeneratedMethods() {
        return generatedMethods;
    }

    public void splitAssert() {
        Map<String, MethodDeclaration> map = new HashMap<>();
        for (String identifier :originalMethod.keySet()) {
            MethodDeclaration clone = originalMethod.get(identifier).clone();
            (clone.getBody().get()).findAll(ExpressionStmt.class).forEach((stmt) -> {
                if (Helper.isAssertion(stmt)) {
                    stmt.remove();
                }
            });
            map.put(identifier, clone);
        }
        this.originalMethod = map;
        this.transformedMethod = new HashMap<>();
        this.isSplit = true;
    }

    public MethodDeclaration addStatementAtLast(Input input, Statement stmt) {
        MethodDeclaration methodDeclaration = transformedMethod.get(input);
        MethodDeclaration clone = methodDeclaration.clone();
        String newMethodName = getNewMethodName(clone.getNameAsString());
        clone.setName(newMethodName);
        addStatementAtLast(input, clone, stmt);
        return clone;
    }

    public void addStatementAtLast(Input input, MethodDeclaration clone, Statement stmt) {
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
    }

    public Map<Input, MethodDeclaration> addStatementsAtLast(List<Input> inputs) {
        Map<Input, MethodDeclaration> methodDeclarations = new HashMap<>();
        for (Input newInput : inputs) {
            MethodCallExpr methodCallExpr = newInput.getMethodCallExpr();
            ExpressionStmt stmt = new ExpressionStmt(methodCallExpr);
            MethodDeclaration methodDeclaration = addStatementAtLast(newInput, stmt);
            methodDeclarations.put(newInput, methodDeclaration);
        }
        return methodDeclarations;
    }

    public CompilationUnit addMethods2CompilationUnit(CompilationUnit unit, Collection<MethodDeclaration> methodDeclarations) {
        CompilationUnit clone = unit.clone();
        if (clone.getClassByName(clazzName).isEmpty()) {
            logger.error("unit: \n" + unit.toString());
            logger.error("clazzName: " + clazzName);
        }
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = clone.getClassByName(clazzName).get();
        List<String> collect = classOrInterfaceDeclaration.getMethods().stream()
                .map(NodeWithSimpleName::getNameAsString).collect(Collectors.toList());
        NodeList<ImportDeclaration> imports = clone.getImports();
        ImportDeclaration importDeclaration = new ImportDeclaration("java.io", false, true);
        imports.add(importDeclaration);
        importDeclaration = new ImportDeclaration("java.nio.charset.StandardCharsets", false, false);
        imports.add(importDeclaration);
        for (MethodDeclaration methodDeclaration: methodDeclarations) {
            if (!collect.contains(methodDeclaration.getNameAsString()))
                classOrInterfaceDeclaration.addMember(methodDeclaration);
        }
        return clone;
    }

    public CompilationUnit addMethod2CompilationUnit(CompilationUnit unit, MethodDeclaration methodDeclaration) {
        CompilationUnit clone = unit.clone();
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
        ImportDeclaration importDeclaration = new ImportDeclaration("java.io", false, true);
        imports.add(importDeclaration);
        importDeclaration = new ImportDeclaration("java.nio.charset.StandardCharsets", false, false);
        imports.add(importDeclaration);
        return clone;
    }

    private String getNewMethodName(String oldName) {
        return oldName + "_generatedTest" + generatedTestsIdx ++;
    }

    private String getNewClazzName(String oldName) {
        return oldName + "_generatedTest" + generatedClazzIdx ++;
    }

    public void applyTransform(Input transformedInput) {
        Expression inputExpr = transformedInput.getInputExpr().clone();
        Expression basicExpr = transformedInput.getTransformed().a;
        Expression transformed = transformedInput.getTransformed().b;
        List<Expression> collector = new ArrayList<>();
        ModifiedVisitor visitor = new ModifiedVisitor(inputExpr, basicExpr, transformed);
        if (transformedInput instanceof ObjectInput) {
            MethodDeclaration preTransformedMethod = originalMethod.get(transformedInput.getIdentifier());
            MethodDeclaration methodDeclaration = preTransformedMethod.clone();
            methodDeclaration.accept(visitor, collector);
            this.transformedMethod.put(transformedInput, methodDeclaration);
            transformedInput.setTransformed(true);
        } else if (transformedInput instanceof BasicInput) {
            MethodCallExpr clone = transformedInput.getMethodCallExpr().clone();
            clone.accept(visitor, collector);
            transformedInput.setMethodCallExpr(clone);
            transformedInput.setTransformed(true);
            this.transformedMethod.put(transformedInput, originalMethod.get(transformedInput.getIdentifier()));
        }
    }

    public void applyTransform(List<Input> inputs) {
        for (Input input :inputs) {
            applyTransform(input);
        }
    }

    public Statement getSkeletonStmt(CompilationUnit compilationUnit, String methodName, Expression expression) {
        EnclosedExpr expr = new EnclosedExpr();
        BinaryExpr binaryExpr = new BinaryExpr();
        binaryExpr.setOperator(BinaryExpr.Operator.PLUS);
        binaryExpr.setRight(expression);
        StringLiteralExpr stringLiteralExpr = new StringLiteralExpr("\n" + getTestNamePrefix(compilationUnit, methodName) + "::");
        binaryExpr.setLeft(stringLiteralExpr);
        expr.setInner(binaryExpr);
        return Helper.constructFileOutputStmt2Instr(oracleFilePath, expr);
    }

    private MethodDeclaration getMethodInstrumented(Input input) {
        //插入一个输出语句用于获取变量在original版本的oracle。
        Expression inputExpr = input.getInputExpr();
        MethodDeclaration methodDeclaration = transformedMethod.get(input);
        inputExpr = input.getMethodCallExpr().getArgument(input.getArgIdx());
        String newMethodName = getNewMethodName(methodDeclaration.getNameAsString());
        MethodDeclaration clone = methodDeclaration.clone();
        clone.setName(newMethodName);
        Statement stmt = getSkeletonStmt(clazz, newMethodName, inputExpr);
        addStatementAtLast(input, clone, stmt);
        return clone;
    }

    public Map<String, MethodDeclaration> getOracle(List<Input> inputs) {
        Map<Input, MethodDeclaration> methodintrs = new HashMap<>();
        for (Input input :inputs) {
            MethodDeclaration methodInstrumented = getMethodInstrumented(input);
            methodintrs.put(input, methodInstrumented);
        }
        CompilationUnit transformedCompilationUnit = addMethods2CompilationUnit(clazz, methodintrs.values());

        StringBuilder testName = new StringBuilder();
        for (MethodDeclaration method :methodintrs.values()) {
            testName.append(getTestNamePrefix(transformedCompilationUnit, method.getNameAsString()) + " ");
        }
        logger.info("Executing tests in original commit...");
        List<String> failed = TransformHelper.saveAndTest(transformedCompilationUnit, getAbsolutePath(), testName.toString());
        //失败的测试就不需要了
        Map<Input, MethodDeclaration> withOracle = new HashMap<>();
        for (Map.Entry<Input, MethodDeclaration> entry : methodintrs.entrySet()) {
            if (!failed.contains(entry.getValue().getName().toString())) {
                withOracle.put(entry.getKey(), entry.getValue());
            }
        }
        inputs.forEach(input -> input.setCompleted(true));
        logger.info("Constructing methods with oracles....");
        Map<String, MethodDeclaration> oracleWithAssert = getOracleWithAssert(transformedCompilationUnit, withOracle);
        return oracleWithAssert;
    }

    public Map<String, MethodDeclaration> getOracle(Input input) {
        MethodDeclaration methodInstrumented = getMethodInstrumented(input);
        //这个时候不需要插入断言
//        MethodDeclaration methodDeclaration = addStatementAtLast(input.getMethodCallExpr());
        CompilationUnit transformedCompilationUnit = addMethod2CompilationUnit(clazz, methodInstrumented);

        input.setCompleted(true);
        String testName = getTestNamePrefix(transformedCompilationUnit, methodInstrumented.getNameAsString());
        List<String> failed = TransformHelper.saveAndTest(transformedCompilationUnit, getAbsolutePath(), testName);
        //失败的测试就不需要了
        if (!failed.contains(methodInstrumented.getNameAsString()))
            return null;
        Map<Input, MethodDeclaration> map = new HashMap<>();
        map.put(input, methodInstrumented);
        Map<String, MethodDeclaration> oracleWithAssert = getOracleWithAssert(transformedCompilationUnit, map);
        return oracleWithAssert;
    }

    public List<String> runGeneratedTests(Map<String, MethodDeclaration> map) {
        logger.info("Transforming test for buggy version, add generated methods.");
        CompilationUnit transformedCompilationUnit = addMethods2CompilationUnit(clazz, map.values());

        StringBuilder testNames = new StringBuilder();
        for (String testName :map.keySet()) {
            testNames.append(testName).append(" ");
        }
        logger.info("Running test to predict patch correctness...");
        List<String> failed = TransformHelper.saveAndTest(transformedCompilationUnit, getAbsolutePath(), testNames.toString());
        return failed;
    }

    public String getTestNamePrefix(CompilationUnit compilationUnit, String methodName) {
        Optional<PackageDeclaration> packageDeclaration = compilationUnit.getPackageDeclaration();
        AtomicReference<String> pack = new AtomicReference<>("");
        packageDeclaration.ifPresent(p -> pack.set(p.getNameAsString()));
        String testName = pack.get() + "." + getClazzName() + "::" + methodName;
        return testName;
    }

    private Map<String, MethodDeclaration> getOracleWithAssert(CompilationUnit instr,
                                               Map<Input, MethodDeclaration> methodDeclarations) {
        List<String> oracles = FileUtils.readEachLine(oracleFilePath);
        Map<String, MethodDeclaration> map  = new HashMap<>();
        for (Map.Entry<Input, MethodDeclaration> entry :methodDeclarations.entrySet()) {
            MethodDeclaration methodDeclaration = entry.getValue();
            Input input = entry.getKey();
            List<String> collect = oracles.stream().filter(line -> !line.equals("") &&
                    line.substring(0, line.lastIndexOf("::")).equals(methodDeclaration.getNameAsString())).collect(Collectors.toList());
            if (collect.isEmpty()) {
                ExpressionStmt stmt = new ExpressionStmt(input.getMethodCallExpr());
                LineComment comment = new LineComment("execution failed, original oracle is set.");
                stmt.setComment(comment);
                replaceLastStmt(methodDeclaration, input, stmt);
                map.put(getTestNamePrefix(instr, methodDeclaration.getNameAsString()), methodDeclaration);
                continue;
            }
            String line = collect.get(0);
            String oracle = line.substring(line.lastIndexOf("::") + 2);
            MethodCallExpr methodCallExpr = input.getMethodCallExpr();
            NodeList<Expression> arguments = methodCallExpr.getArguments();
            Expression oldOracle = arguments.get(0);
            Expression newOracle = TransformHelper.inputTransformer.transform(oldOracle, oracle);
            arguments.replace(oldOracle, newOracle);
            ExpressionStmt stmt = new ExpressionStmt(methodCallExpr);
            replaceLastStmt(methodDeclaration, input, stmt);
            map.put(getTestNamePrefix(instr, methodDeclaration.getNameAsString()), methodDeclaration);
        }
        return map;
    }

    private void replaceLastStmt(MethodDeclaration methodDeclaration, Input input, ExpressionStmt stmt) {
        Optional<BlockStmt> body = methodDeclaration.getBody();
        body.ifPresent(blockStmt -> {
            NodeList<Statement> statements = blockStmt.getStatements();
            Optional<Statement> last = statements.getLast();
            last.ifPresent(b -> {
                if (b.isBlockStmt()) {
                    statements.replace(b, stmt);
                }
            });
        });
    }

    @Override
    public Skeleton clone() {
        try {
            Skeleton clone = (Skeleton) super.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
