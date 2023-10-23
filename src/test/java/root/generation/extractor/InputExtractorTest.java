package root.generation.extractor;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import org.junit.jupiter.api.Test;
import root.generation.entity.Input;
import root.generation.helper.Preparation;
import root.generation.helper.PreparationTest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class InputExtractorTest extends PreparationTest{

    public static String filePath = "src/main/java/root/analysis/StringFilter.java";
    static String methodName = "addPattern";
    static int lineNumber = 32;

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
        MethodCallExpr methodCallExpr = PreparationTest.preparation.inputExtractor
                .extractMethodCallByLine(extractInput, 67);
        assertNotNull(methodCallExpr);
    }

    @Test
    void extractInput() {
        Input input = getInput();
        assertEquals("java.lang.String", input.getType());
    }

    public static Input getInput() {
        MethodDeclaration extractInput = getMethodDeclaration();
        MethodCallExpr methodCallExpr = PreparationTest.preparation.inputExtractor
                .extractMethodCallByLine(extractInput, lineNumber);
        return PreparationTest.preparation.inputExtractor.extractInput(methodCallExpr);
    }
}