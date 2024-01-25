package root.generation.transformation;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.utils.Pair;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import root.PatchValidator;
import root.generation.entity.Input;
import root.generation.entity.Skeleton;
import root.generation.extractor.InputExtractorTest;
import root.generation.helper.PreparationTest;
import root.generation.transformation.extractor.InputExtractor;
import root.util.ConfigurationProperties;

import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class InputTransformerTest extends PreparationTest{

    @Test
    void transformInput() {
        InputTransformer inputTransformer = TransformHelper.inputTransformer;
        Input input = InputExtractorTest.getInput();
        Pair<Expression, Object> inputMutant = MutateHelper.getInputMutant(input);
        Input newInput = inputTransformer.transformInput(input, inputMutant.a, inputMutant.b);
        assertNotEquals(newInput.getTransformed(), input.getBasicExpr());
    }

    @Test
    void transformInputs() {
        InputTransformer inputTransformer = TransformHelper.inputTransformer;
        Input input = InputExtractorTest.getInput();
        List<Pair<Expression, Object>> inputMutants = MutateHelper.getInputMutants(input, 10);
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
        MethodDeclaration methodDeclaration = InputExtractorTest.getMethodDeclaration();
        MethodCallExpr methodCallExpr = TransformHelper.inputExtractor
                .extractMethodCallByLine(methodDeclaration, lineNumber);
        Input input =  TransformHelper.inputExtractor.extractInput(methodCallExpr);
        String absolutePath = new File(InputExtractorTest.filePath).getAbsolutePath();
        Optional<ClassOrInterfaceDeclaration> ancestor = methodDeclaration.findAncestor(ClassOrInterfaceDeclaration.class);
        String clazzName = ancestor.get().getName().toString();
        Skeleton skeleton = TransformHelper.createASkeleton(absolutePath, clazzName, input);
        return skeleton;
    }

    @Test
    void buildNewTestByInput() {
        InputTransformer inputTransformer = TransformHelper.inputTransformer;
        MethodDeclaration methodDeclaration = InputExtractorTest.getMethodDeclaration();
        MethodCallExpr methodCallExpr = TransformHelper.inputExtractor
                .extractMethodCallByLine(methodDeclaration, lineNumber);
        Input input =  TransformHelper.inputExtractor.extractInput(methodCallExpr);
        String absolutePath = new File(InputExtractorTest.filePath).getAbsolutePath();
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
        MethodDeclaration methodDeclaration = InputExtractorTest.getMethodDeclaration();
        MethodCallExpr methodCallExpr = TransformHelper.inputExtractor
                .extractMethodCallByLine(methodDeclaration, lineNumber);
        Input input =  TransformHelper.inputExtractor.extractInput(methodCallExpr);
        List<Pair<Expression, Object>> inputMutants = MutateHelper.getInputMutants(input, 10);
        List<Input> newInputs = inputTransformer.transformInput(input, inputMutants);
        String absolutePath = new File(InputExtractorTest.filePath).getAbsolutePath();
        Optional<ClassOrInterfaceDeclaration> ancestor = methodDeclaration.findAncestor(ClassOrInterfaceDeclaration.class);
        String clazzName = ancestor.get().getName().toString();
        Skeleton skeleton = TransformHelper.createASkeleton(absolutePath, clazzName, input);
        assert skeleton != null;
        Map<String, MethodDeclaration> compilationUnitMap = inputTransformer.buildNewTestByInputs(skeleton, newInputs);
        assertEquals(10, compilationUnitMap.size());
    }

    @Test
    void mutateTest() {
        String absolutePath = new File(InputExtractorTest.filePath).getAbsolutePath();
        CompilationUnit compilationUnit = TransformHelper.inputExtractor.getCompilationUnit(absolutePath);
        String testInfos = ConfigurationProperties.getProperty("testInfos");
        String[] split = testInfos.split("#")[0].split(":")[0].split("\\.");
        Skeleton skeleton = new Skeleton(absolutePath, compilationUnit, split[split.length - 1]);
        Input input = InputExtractorTest.getInput();
        Map<String, MethodDeclaration> stringMethodDeclarationMap = TransformHelper.mutateTest(skeleton, Collections.singletonList(input), 10);
        assertEquals(10, stringMethodDeclarationMap.size());
    }

    @Test
    void applyPatch() {
        String absolutePath = new File(InputExtractorTest.filePath).getAbsolutePath();
        CompilationUnit compilationUnit = TransformHelper.inputExtractor.getCompilationUnit(absolutePath);
        String testInfos = ConfigurationProperties.getProperty("testInfos");
        String[] split = testInfos.split("#")[0].split(":")[0].split("\\.");
        Skeleton skeleton = new Skeleton(absolutePath, compilationUnit, split[split.length - 1]);
        Input input = InputExtractorTest.getInput();
        Map<String, MethodDeclaration> stringMethodDeclarationMap = TransformHelper.mutateTest(skeleton, Collections.singletonList(input), 10);
//        PatchValidator validator = new PatchValidator();
//        boolean res = validator.validate(projectPreparation.patches, Collections.singletonList(skeleton));
//        assertTrue(res);
    }

    @Ignore
    void getCompiledClassesForTestExecution() {
        InputTransformer inputTransformer = TransformHelper.inputTransformer;
        InputExtractor inputExtractor = TransformHelper.inputExtractor;
        MethodDeclaration methodDeclaration = InputExtractorTest.getMethodDeclaration();
        MethodCallExpr methodCallExpr = TransformHelper.inputExtractor
                .extractMethodCallByLine(methodDeclaration, lineNumber);
        Input input =  TransformHelper.inputExtractor.extractInput(methodCallExpr);
        String absolutePath = new File(InputExtractorTest.filePath).getAbsolutePath();
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