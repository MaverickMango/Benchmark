package root.generation.transformation;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import org.junit.jupiter.api.Test;
import root.generation.entity.Input;
import root.generation.entity.Skeleton;
import root.generation.extractor.InputExtractorTest;
import root.generation.helper.MutatorHelper;
import root.generation.helper.PreparationTest;
import root.generation.transformation.extractor.InputExtractor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
        CompilationUnit compilationUnit = inputExtractor.getCompilationUnit(new File(InputExtractorTest.filePath).getAbsolutePath());
        Skeleton skeleton = new Skeleton(compilationUnit.getClassByName("StringFilter").get(), methodDeclaration, input);
        inputTransformer.buildNewTestByInput(skeleton, newInput);
    }
}