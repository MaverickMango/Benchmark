package root.generation.transformation;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import org.junit.jupiter.api.Test;
import root.generation.entity.Input;
import root.generation.entity.Skeleton;
import root.generation.extractor.InputExtractorTest;
import root.generation.helper.Helper;
import root.generation.helper.MutatorHelper;
import root.generation.helper.PreparationTest;
import root.generation.helper.TransformHelper;
import root.generation.transformation.extractor.InputExtractor;

import javax.tools.JavaFileObject;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        MethodDeclaration methodDeclaration = InputExtractorTest.getMethodDeclaration();
        String absolutePath = new File(InputExtractorTest.filePath).getAbsolutePath();
        Skeleton skeleton = TransformHelper.createASkeleton(absolutePath, methodDeclaration);
        assertNotNull(skeleton);
    }

    @Test
    void buildNewTestByInput() {
        InputTransformer inputTransformer = TransformHelper.inputTransformer;
        InputExtractor inputExtractor = TransformHelper.inputExtractor;
        Input input = InputExtractorTest.getInput();
        Object inputMutant = MutatorHelper.getInputMutant(input);
        Input newInput = inputTransformer.transformInput(input, inputMutant);
        MethodDeclaration methodDeclaration = InputExtractorTest.getMethodDeclaration();
        String absolutePath = new File(InputExtractorTest.filePath).getAbsolutePath();
        Skeleton skeleton = TransformHelper.createASkeleton(absolutePath, methodDeclaration);
        assert skeleton != null;
        Map<CompilationUnit, String[]> compilationUnitMap = inputTransformer.buildNewTestByInput(skeleton, newInput);
        CompilationUnit compilationUnit1 = new ArrayList<>(compilationUnitMap.keySet()).get(0);
        CompilationUnit compilationUnit = inputExtractor.getCompilationUnit(absolutePath);
        assertNotEquals(compilationUnit, compilationUnit1);
    }

    @Test
    void buildNewTestByInputs() {
        InputTransformer inputTransformer = TransformHelper.inputTransformer;
        InputExtractor inputExtractor = TransformHelper.inputExtractor;
        Input input = InputExtractorTest.getInput();
        List<Object> inputMutants = MutatorHelper.getInputMutants(input, 10);
        List<Input> newInputs = inputTransformer.transformInput(input, inputMutants);
        MethodDeclaration methodDeclaration = InputExtractorTest.getMethodDeclaration();
        String absolutePath = new File(InputExtractorTest.filePath).getAbsolutePath();
        Skeleton skeleton = TransformHelper.createASkeleton(absolutePath, methodDeclaration);
        assert skeleton != null;
        Map<CompilationUnit, String[]> compilationUnitMap = inputTransformer.buildNewTestByInputs(skeleton, newInputs);
        CompilationUnit compilationUnit1 = new ArrayList<>(compilationUnitMap.keySet()).get(0);
        CompilationUnit compilationUnit = inputExtractor.getCompilationUnit(absolutePath);
        assertNotEquals(compilationUnit, compilationUnit1);
    }

    @Test
    void getCompiledClassesForTestExecution() {
        InputTransformer inputTransformer = TransformHelper.inputTransformer;
        InputExtractor inputExtractor = TransformHelper.inputExtractor;
        Input input = InputExtractorTest.getInput();
        Object inputMutant = MutatorHelper.getInputMutant(input);
        Input newInput = inputTransformer.transformInput(input, inputMutant);
        MethodDeclaration methodDeclaration = InputExtractorTest.getMethodDeclaration();
        String absolutePath = new File(InputExtractorTest.filePath).getAbsolutePath();
        CompilationUnit compilationUnit = inputExtractor.getCompilationUnit(absolutePath);
        Skeleton skeleton = new Skeleton(absolutePath, compilationUnit, methodDeclaration);
        Map<CompilationUnit, String[]> compilationUnitMap = inputTransformer.buildNewTestByInput(skeleton, newInput);
        CompilationUnit buildNewTestByInput = new ArrayList<>(compilationUnitMap.keySet()).get(0);
        Map<Skeleton, CompilationUnit> map = new HashMap<>();
        map.put(skeleton, buildNewTestByInput);
        Map<String, String> javaSources = Helper.getJavaSources(map);
        Map<String, JavaFileObject> compiledClassesForTestExecution = preparation.getCompiledClassesForTestExecution(javaSources);
        assertNotNull(compiledClassesForTestExecution);
    }
}