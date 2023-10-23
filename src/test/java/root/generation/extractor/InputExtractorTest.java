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

class InputExtractorTest extends PreparationTest {

    String filePath = "src/main/java/root/generation/transformation/extractor/InputExtractor.java";
    String methodName = "extractMethodByName";
    int lineNumber = 40;

    @Test
    void extractMethodByName() {
        List<ImportDeclaration> importDeclarations = new ArrayList<>();
        MethodDeclaration extractInput = PreparationTest.preparation.inputExtractor
                .extractMethodByName(
                        new File(filePath).getAbsolutePath(),
                        methodName, importDeclarations);
        assertNotNull(extractInput);
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
        List<ImportDeclaration> importDeclarations = new ArrayList<>();
        MethodDeclaration extractInput = PreparationTest.preparation.inputExtractor
                .extractMethodByName(
                        new File(filePath).getAbsolutePath(),
                        methodName, importDeclarations);
        MethodCallExpr methodCallExpr = PreparationTest.preparation.inputExtractor
                .extractMethodCallByLine(extractInput, lineNumber);
        Input input = PreparationTest.preparation.inputExtractor.extractInput(methodCallExpr);
        assertEquals("Object", input.getType());
    }
}