package root.generation.transformation;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.utils.Pair;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import root.generation.entities.Input;
import root.generation.entities.Skeleton;
import root.extraction.ASTExtractorTest;
import root.generation.helper.PreparationTest;
import root.analysis.ASTExtractor;
import root.util.ConfigurationProperties;

import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class InputTransformerTest extends PreparationTest{

    @Test
    void transformInput() {
        InputTransformer inputTransformer = TransformHelper.inputTransformer;
        Input input = ASTExtractorTest.getInput();
        Pair<Expression, Object> inputMutant = MutateHelper.getInputMutant(input);
        Input newInput = inputTransformer.transformInput(input, inputMutant.a, inputMutant.b);
        assertNotEquals(newInput.getTransformed(), input.getBasicExpr());
    }

    @Test
    void transformInputs() {
        InputTransformer inputTransformer = TransformHelper.inputTransformer;
        Input input = ASTExtractorTest.getInput();
        List<Pair<Expression, Object>> inputMutants = MutateHelper.getInputMutants(input, null);
        assertEquals(10, inputMutants.size());
        List<Input> newInputs = inputTransformer.transformInput(input, inputMutants);
        for (Input newInput :newInputs) {
            assertNotEquals(newInput.getTransformed(), input.getBasicExpr());
        }
    }

    @Test
    void createASkeleton() {
        Skeleton skeleton = getASkeleton();
        assertNotNull(skeleton);
    }

    public Skeleton getASkeleton() {
        CallableDeclaration methodDeclaration = ASTExtractorTest.getMethodDeclaration();
        MethodCallExpr methodCallExpr = TransformHelper.ASTExtractor
                .extractAssertByLine(methodDeclaration, lineNumber);
        Input input =  TransformHelper.ASTExtractor.extractInput(methodCallExpr);
        String absolutePath = new File(ASTExtractorTest.filePath).getAbsolutePath();
        Optional<ClassOrInterfaceDeclaration> ancestor = methodDeclaration.findAncestor(ClassOrInterfaceDeclaration.class);
        String clazzName = ancestor.get().getName().toString();
        Skeleton skeleton = TransformHelper.createASkeleton(absolutePath, clazzName, input);
        return skeleton;
    }

    @Test
    void buildNewTestByInput() {
        InputTransformer inputTransformer = TransformHelper.inputTransformer;
        CallableDeclaration methodDeclaration = ASTExtractorTest.getMethodDeclaration();
        MethodCallExpr methodCallExpr = TransformHelper.ASTExtractor
                .extractAssertByLine(methodDeclaration, lineNumber);
        Input input =  TransformHelper.ASTExtractor.extractInput(methodCallExpr);
        String absolutePath = new File(ASTExtractorTest.filePath).getAbsolutePath();
        Pair<Expression, Object> inputMutant = MutateHelper.getInputMutant(input);
        Input newInput = inputTransformer.transformInput(input, inputMutant.a, inputMutant.b);
        Optional<ClassOrInterfaceDeclaration> ancestor = methodDeclaration.findAncestor(ClassOrInterfaceDeclaration.class);
        String clazzName = ancestor.get().getName().toString();
        Skeleton skeleton = TransformHelper.createASkeleton(absolutePath, clazzName, input);
        assert skeleton != null;
        Map<String, MethodDeclaration> compilationUnitMap = inputTransformer.buildNewTestByInput(skeleton, newInput);
        assertNotNull(compilationUnitMap);
    }

    @Test
    void buildNewTestByInputs() {
        InputTransformer inputTransformer = TransformHelper.inputTransformer;
        CallableDeclaration methodDeclaration = ASTExtractorTest.getMethodDeclaration();
        MethodCallExpr methodCallExpr = TransformHelper.ASTExtractor
                .extractAssertByLine(methodDeclaration, lineNumber);
        Input input =  TransformHelper.ASTExtractor.extractInput(methodCallExpr);
        List<Pair<Expression, Object>> inputMutants = MutateHelper.getInputMutants(input, null);
        List<Input> newInputs = inputTransformer.transformInput(input, inputMutants);
        String absolutePath = new File(ASTExtractorTest.filePath).getAbsolutePath();
        Optional<ClassOrInterfaceDeclaration> ancestor = methodDeclaration.findAncestor(ClassOrInterfaceDeclaration.class);
        String clazzName = ancestor.get().getName().toString();
        Skeleton skeleton = TransformHelper.createASkeleton(absolutePath, clazzName, input);
        assert skeleton != null;
        Map<String, MethodDeclaration> compilationUnitMap = inputTransformer.buildNewTestByInputs(skeleton, newInputs);
        assertEquals(10, compilationUnitMap.size());
    }

    @Test
    void mutateTest() {
        String absolutePath = new File(ASTExtractorTest.filePath).getAbsolutePath();
        CompilationUnit compilationUnit = TransformHelper.ASTExtractor.getCompilationUnit(absolutePath);
        String testInfos = ConfigurationProperties.getProperty("testInfos");
        String[] split = testInfos.split("#")[0].split(":")[0].split("\\.");
        Skeleton skeleton = new Skeleton(absolutePath, compilationUnit, split[split.length - 1]);
        Input input = ASTExtractorTest.getInput();
        Map<String, MethodDeclaration> stringMethodDeclarationMap = TransformHelper.mutateTest(skeleton, Collections.singletonList(input), null);
        assertEquals(10, stringMethodDeclarationMap.size());
    }

    @Test
    void applyPatch() {
        String absolutePath = new File(ASTExtractorTest.filePath).getAbsolutePath();
        CompilationUnit compilationUnit = TransformHelper.ASTExtractor.getCompilationUnit(absolutePath);
        String testInfos = ConfigurationProperties.getProperty("testInfos");
        String[] split = testInfos.split("#")[0].split(":")[0].split("\\.");
        Skeleton skeleton = new Skeleton(absolutePath, compilationUnit, split[split.length - 1]);
        Input input = ASTExtractorTest.getInput();
        Map<String, MethodDeclaration> stringMethodDeclarationMap = TransformHelper.mutateTest(skeleton, Collections.singletonList(input), null);
        assertNotNull(stringMethodDeclarationMap);
        //        PatchValidator validator = new PatchValidator();
//        boolean res = validator.validate(projectPreparation.patches, Collections.singletonList(skeleton));
//        assertTrue(res);
    }

    @Ignore
    void getCompiledClassesForTestExecution() {
        InputTransformer inputTransformer = TransformHelper.inputTransformer;
        ASTExtractor ASTExtractor = TransformHelper.ASTExtractor;
        CallableDeclaration methodDeclaration = ASTExtractorTest.getMethodDeclaration();
        MethodCallExpr methodCallExpr = TransformHelper.ASTExtractor
                .extractAssertByLine(methodDeclaration, lineNumber);
        Input input =  TransformHelper.ASTExtractor.extractInput(methodCallExpr);
        String absolutePath = new File(ASTExtractorTest.filePath).getAbsolutePath();
        Pair<Expression, Object> inputMutant = MutateHelper.getInputMutant(input);
        Input newInput = inputTransformer.transformInput(input, inputMutant.a, inputMutant.b);
        Optional<ClassOrInterfaceDeclaration> ancestor = methodDeclaration.findAncestor(ClassOrInterfaceDeclaration.class);
        String clazzName = ancestor.get().getName().toString();
        Skeleton skeleton = TransformHelper.createASkeleton(absolutePath, clazzName, input);
        Map<String, MethodDeclaration> compilationUnitMap = inputTransformer.buildNewTestByInput(skeleton, newInput);
//        CompilationUnit buildNewTestByInput = new ArrayList<>(compilationUnitMap.keySet()).get(0);
//        Map<Skeleton, CompilationUnit> map = new HashMap<>();
//        map.put(skeleton, buildNewTestByInput);
//        Map<String, String> javaSources = Helper.getJavaSources(map);
//        Map<String, JavaFileObject> compiledClassesForTestExecution = projectPreparation.getCompiledClassesForTestExecution(javaSources);
//        assertNotNull(compiledClassesForTestExecution);
    }
}