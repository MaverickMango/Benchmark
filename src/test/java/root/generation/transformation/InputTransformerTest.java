package root.generation.transformation;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import root.generation.entity.Input;
import root.generation.entity.Skeleton;
import root.generation.extractor.InputExtractorTest;
import root.generation.helper.Helper;
import root.generation.helper.MutatorHelper;
import root.generation.helper.PreparationTest;
import root.generation.transformation.extractor.InputExtractor;

import javax.tools.JavaFileObject;
import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class InputTransformerTest extends PreparationTest{

    @Test
    void transformInput() {
        InputTransformer inputTransformer = TransformHelper.inputTransformer;
        Input input = InputExtractorTest.getInput();
        Object inputMutant = MutatorHelper.getInputMutant(input);
        Input newInput = inputTransformer.transformInput(input, inputMutant);
        assertNotEquals(newInput.getTransformed(), input.getBasicExpr());
    }

    @Test
    void transformInputs() {
        InputTransformer inputTransformer = TransformHelper.inputTransformer;
        Input input = InputExtractorTest.getInput();
        List<Object> inputMutants = MutatorHelper.getInputMutants(input, 10);
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
        Object inputMutant = MutatorHelper.getInputMutant(input);
        Input newInput = inputTransformer.transformInput(input, inputMutant);
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
        List<Object> inputMutants = MutatorHelper.getInputMutants(input, 10);
        List<Input> newInputs = inputTransformer.transformInput(input, inputMutants);
        String absolutePath = new File(InputExtractorTest.filePath).getAbsolutePath();
        Optional<ClassOrInterfaceDeclaration> ancestor = methodDeclaration.findAncestor(ClassOrInterfaceDeclaration.class);
        String clazzName = ancestor.get().getName().toString();
        Skeleton skeleton = TransformHelper.createASkeleton(absolutePath, clazzName, input);
        assert skeleton != null;
        Map<String, MethodDeclaration> compilationUnitMap = inputTransformer.buildNewTestByInputs(skeleton, newInputs);
        assertNotNull(compilationUnitMap);
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
        Object inputMutant = MutatorHelper.getInputMutant(input);
        Input newInput = inputTransformer.transformInput(input, inputMutant);
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