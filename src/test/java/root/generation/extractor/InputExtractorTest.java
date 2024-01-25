package root.generation.extractor;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import org.junit.jupiter.api.Test;
import root.generation.entity.Input;
import root.generation.helper.PreparationTest;
import root.generation.transformation.TransformHelper;
import root.generation.transformation.extractor.InputExtractor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class InputExtractorTest extends PreparationTest{

    @Test
    void extractMethodByName() {
        MethodDeclaration extractInput = getMethodDeclaration();
        assertNotNull(extractInput);
    }

    public static MethodDeclaration getMethodDeclaration() {
        List<ImportDeclaration> importDeclarations = new ArrayList<>();
        CompilationUnit compilationUnit = TransformHelper.inputExtractor.getCompilationUnit(filePath);
        MethodDeclaration extractInput = TransformHelper.inputExtractor
                .extractMethodByName(
                        compilationUnit,
                        methodName, importDeclarations);
        return extractInput;
    }

    @Test
    void extractMethodCallByLine() {
        List<ImportDeclaration> importDeclarations = new ArrayList<>();
        CompilationUnit compilationUnit = TransformHelper.inputExtractor.getCompilationUnit(filePath);
        MethodDeclaration extractInput = TransformHelper.inputExtractor
                .extractMethodByName(
                        compilationUnit,
                        methodName, importDeclarations);
        InputExtractor inputExtractor = TransformHelper.inputExtractor;
        MethodCallExpr methodCallExpr = inputExtractor.extractMethodCallByLine(extractInput, lineNumber);
        assertEquals(3, methodCallExpr.getArguments().size());
    }

    @Test
    void extractInput() {
        Input input = getInput();
        assertEquals("java.lang.Double", input.getType());
    }

    public static Input getInput() {
        MethodDeclaration extractInput = getMethodDeclaration();
        MethodCallExpr methodCallExpr = TransformHelper.inputExtractor
                .extractMethodCallByLine(extractInput, lineNumber);
        return TransformHelper.inputExtractor.extractInput(methodCallExpr);
    }
}