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
import root.bean.BugRepository;
import root.generation.helper.Helper;
import root.generation.transformation.TransformHelper;
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
    Map<String, MethodDeclaration> originalMethod;//key: input's identifier
    Map<String, MethodDeclaration> transformedMethod;//key: input's identifier
    boolean isSplit;//是否完成对多余断言的删除
    List<Input> inputs;
    Map<Input, MethodDeclaration> generatedMethods;
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
        File file = new File(filePath);
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
        setInputs(inputs);
        this.clazzName = clazzName;
        this.isSplit = false;
        this.inputs = new ArrayList<>();
        this.generatedMethods = new HashMap<>();
        this.generatedTestsIdx = 0;
        this.generatedClazzIdx = 0;
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
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

    public Map<String, MethodDeclaration> getTransformedMethod() {
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

    public MethodDeclaration getGeneratedMethod(Input input) {
        return generatedMethods.get(input);
    }
    public Map<Input, MethodDeclaration> getGeneratedMethods() {
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
            map.put(identifier, originalMethod.get(identifier));
        }
        this.originalMethod = map;
        this.transformedMethod = map;
        this.isSplit = true;
    }

    public MethodDeclaration addStatementAtLast(Input input, Statement stmt) {
        MethodDeclaration methodDeclaration = originalMethod.get(input);
        return addStatementAtLast(input, methodDeclaration, stmt);
    }

    public MethodDeclaration addStatementAtLast(Input input, MethodDeclaration methodDeclaration, Statement stmt) {
        MethodDeclaration clone = methodDeclaration.clone();
        String methodNewName = getNewMethodName(methodDeclaration.getNameAsString());
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
        generatedMethods.putIfAbsent(input, clone);
        return clone;
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
        Expression basicExpr = transformedInput.getBasicExpr();
        Expression transformed = transformedInput.getTransformed();
        List<Expression> collector = new ArrayList<>();
        ModifiedVisitor visitor = new ModifiedVisitor(inputExpr, basicExpr, transformed);
        if (transformedInput instanceof ObjectInput) {
            MethodDeclaration preTransformedMethod = transformedMethod.get(transformedInput.getIdentifier());
            MethodDeclaration methodDeclaration = preTransformedMethod.clone();
            methodDeclaration.accept(visitor, collector);
            this.transformedMethod.put(transformedInput.getIdentifier(), methodDeclaration);
            transformedInput.setTransformed(true);
        } else if (transformedInput instanceof BasicInput) {
            MethodCallExpr clone = transformedInput.getMethodCallExpr().clone();
            clone.accept(visitor, collector);
            transformedInput.setMethodCallExpr(clone);
            transformedInput.setTransformed(true);
        }
    }

    public void applyTransform(List<Input> inputs) {
        for (Input input :inputs) {
            applyTransform(input);
        }
    }

    String filePath = ConfigurationProperties.getProperty("location") + File.separator + "generatedOracles.txt";

    public Statement getSkeletonStmt(CompilationUnit compilationUnit, String methodName, Expression expression) {
        EnclosedExpr expr = new EnclosedExpr();
        BinaryExpr binaryExpr = new BinaryExpr();
        binaryExpr.setOperator(BinaryExpr.Operator.PLUS);
        binaryExpr.setRight(expression);
        StringLiteralExpr stringLiteralExpr = new StringLiteralExpr("\n" + getTestNamePrefix(compilationUnit, methodName) + "::");
        binaryExpr.setLeft(stringLiteralExpr);
        expr.setInner(binaryExpr);
        return Helper.constructFileOutputStmt2Instr(filePath, expr);
    }

    private MethodDeclaration getMethodInstrumented(Input input) {
        //插入一个输出语句用于获取变量在original版本的oracle。
        Expression inputExpr = input.getInputExpr();
        MethodDeclaration methodDeclaration = this.getOriginalMethod().get(input.getIdentifier());
        if (input instanceof ObjectInput) {
            methodDeclaration = this.getTransformedMethod().get(input.getIdentifier());
        }
        inputExpr = input.getMethodCallExpr().getArgument(input.getArgIdx());
        String newMethodName = getNewMethodName(methodDeclaration.getNameAsString());
        Statement stmt = getSkeletonStmt(clazz, newMethodName, inputExpr);
        MethodDeclaration methodInstrumented = addStatementAtLast(input, methodDeclaration, stmt);
        return methodInstrumented;
    }

    public Map<String, MethodDeclaration> getOracle(BugRepository bugRepository, List<Input> inputs) {
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
        //todo internal test?
        FileUtils.writeToFile(transformedCompilationUnit.toString(), getAbsolutePath(), false);
        bugRepository.test(testName.toString());
        inputs.forEach(input -> input.setCompleted(true));

        Map<String, MethodDeclaration> oracleWithAssert = getOracleWithAssert(transformedCompilationUnit, methodintrs);
        return oracleWithAssert;
    }

    public Map<String, MethodDeclaration> getOracle(BugRepository bugRepository, Input input) {
        MethodDeclaration methodInstrumented = getMethodInstrumented(input);
        //这个时候不需要插入断言
//        MethodDeclaration methodDeclaration = addStatementAtLast(input.getMethodCallExpr());
        CompilationUnit transformedCompilationUnit = addMethod2CompilationUnit(clazz, methodInstrumented);

        String testName = getTestNamePrefix(transformedCompilationUnit, methodInstrumented.getNameAsString());
        //todo internal test?
        FileUtils.writeToFile(transformedCompilationUnit.toString(), getAbsolutePath(), false);
        bugRepository.test(testName);
        input.setCompleted(true);
        Map<Input, MethodDeclaration> map = new HashMap<>();
        map.put(input, methodInstrumented);
        Map<String, MethodDeclaration> oracleWithAssert = getOracleWithAssert(transformedCompilationUnit, map);
        return oracleWithAssert;
    }

    public List<String> applyPatch(BugRepository bugRepository, Map<String, MethodDeclaration> map) {
        CompilationUnit transformedCompilationUnit = addMethods2CompilationUnit(clazz, map.values());

        StringBuilder testNames = new StringBuilder();
        for (String testName :map.keySet()) {
            testNames.append(testName).append(" ");
        }
        //todo internal test?
        FileUtils.writeToFile(transformedCompilationUnit.toString(), getAbsolutePath(), false);
        List<String> res = bugRepository.testWithRes(testNames.toString());
        return res;
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
        List<String> oracles = FileUtils.readEachLine(filePath);
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
}
