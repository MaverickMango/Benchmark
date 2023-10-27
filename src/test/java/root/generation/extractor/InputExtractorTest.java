package root.generation.extractor;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import org.junit.jupiter.api.Test;
import root.generation.entity.Input;
import root.generation.helper.Preparation;
import root.generation.helper.PreparationTest;
import root.generation.transformation.extractor.InputExtractor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class InputExtractorTest extends PreparationTest{

    public static String filePath = "/home/liumengjiao/Desktop/CI/bugs/Closure_10_bug/test/com/google/javascript/jscomp/PeepholeFoldConstantsTest.java";
    static String methodName = "testIssue821";
    static int lineNumber = 582;

    @Test
    void extractMethodByName() {
        MethodDeclaration extractInput = getMethodDeclaration();
        assertNotNull(extractInput);
    }

    public static MethodDeclaration getMethodDeclaration() {
        List<ImportDeclaration> importDeclarations = new ArrayList<>();
        MethodDeclaration extractInput = PreparationTest.preparation.inputExtractor
                .extractMethodByName(
                        new File(filePath).getAbsolutePath(),
                        methodName, importDeclarations);
        return extractInput;
    }

    @Test
    void extractMethodCallByLine() {
        List<ImportDeclaration> importDeclarations = new ArrayList<>();
        MethodDeclaration extractInput = PreparationTest.preparation.inputExtractor
                .extractMethodByName(
                        new File(filePath).getAbsolutePath(),
                        methodName, importDeclarations);
        InputExtractor inputExtractor = PreparationTest.preparation.inputExtractor;
        MethodCallExpr methodCallExpr = inputExtractor.extractMethodCallByLine(extractInput, 67);
        assertEquals(0, methodCallExpr.getArguments().size());
    }

    @Test
    void extractInput() {
        Input input = getInput();
        assertEquals("Object", input.getType());
    }

    public static Input getInput() {
        MethodDeclaration extractInput = getMethodDeclaration();
        MethodCallExpr methodCallExpr = PreparationTest.preparation.inputExtractor
                .extractMethodCallByLine(extractInput, lineNumber);
        return PreparationTest.preparation.inputExtractor.extractInput(methodCallExpr);
    }
}