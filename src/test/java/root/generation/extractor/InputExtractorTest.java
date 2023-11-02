package root.generation.extractor;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import root.generation.entity.Input;
import root.generation.helper.Preparation;
import root.generation.helper.PreparationTest;
import root.generation.helper.TransformHelper;
import root.generation.transformation.extractor.InputExtractor;
import root.util.ConfigurationProperties;

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
        MethodDeclaration extractInput = TransformHelper.inputExtractor
                .extractMethodByName(
                        new File(filePath).getAbsolutePath(),
                        methodName, importDeclarations);
        return extractInput;
    }

    @Test
    void extractMethodCallByLine() {
        List<ImportDeclaration> importDeclarations = new ArrayList<>();
        MethodDeclaration extractInput = TransformHelper.inputExtractor
                .extractMethodByName(
                        new File(filePath).getAbsolutePath(),
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