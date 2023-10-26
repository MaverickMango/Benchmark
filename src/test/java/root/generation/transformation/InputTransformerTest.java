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
import root.generation.transformation.extractor.InputExtractor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InputTransformerTest extends PreparationTest{

    @Test
    void transformInput() {
        InputTransformer inputTransformer = PreparationTest.preparation.inputTransformer;
        Input input = InputExtractorTest.getInput();
        Object inputMutant = MutatorHelper.getInputMutant(input);
        Input newInput = inputTransformer.transformInput(input, inputMutant);
        assertNotEquals(newInput, input);
    }

    @Test
    void buildNewTestByInput() {
        InputTransformer inputTransformer = PreparationTest.preparation.inputTransformer;
        Input input = InputExtractorTest.getInput();
        Object inputMutant = MutatorHelper.getInputMutant(input);
        Input newInput = inputTransformer.transformInput(input, inputMutant);
        MethodDeclaration methodDeclaration = InputExtractorTest.getMethodDeclaration();
        InputExtractor inputExtractor = PreparationTest.preparation.inputExtractor;
        String absolutePath = new File(InputExtractorTest.filePath).getAbsolutePath();
        CompilationUnit compilationUnit = inputExtractor.getCompilationUnit(absolutePath);
        Skeleton skeleton = new Skeleton(absolutePath, compilationUnit, methodDeclaration);
        CompilationUnit compilationUnit1 = inputTransformer.buildNewTestByInput(skeleton, newInput);
        assertNotNull(compilationUnit1);
    }

    @Test
    void getCompiledClassesForTestExecution() {
        InputTransformer inputTransformer = PreparationTest.preparation.inputTransformer;
        Input input = InputExtractorTest.getInput();
        Object inputMutant = MutatorHelper.getInputMutant(input);
        Input newInput = inputTransformer.transformInput(input, inputMutant);
        MethodDeclaration methodDeclaration = InputExtractorTest.getMethodDeclaration();
        InputExtractor inputExtractor = PreparationTest.preparation.inputExtractor;
        String absolutePath = new File(InputExtractorTest.filePath).getAbsolutePath();
        CompilationUnit compilationUnit = inputExtractor.getCompilationUnit(absolutePath);
        Skeleton skeleton = new Skeleton(absolutePath, compilationUnit, methodDeclaration);
        CompilationUnit buildNewTestByInput = inputTransformer.buildNewTestByInput(skeleton, newInput);
        Map<Skeleton, CompilationUnit> map = new HashMap<>();
        map.put(skeleton, buildNewTestByInput);
        Map<String, String> javaSources = Helper.getJavaSources(map);
        preparation.getCompiledClassesForTestExecution(javaSources);
    }
}