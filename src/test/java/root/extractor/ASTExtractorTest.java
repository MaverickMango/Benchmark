package root.extractor;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import org.junit.jupiter.api.Test;
import root.generation.entity.Input;
import root.generation.helper.PreparationTest;
import root.generation.transformation.TransformHelper;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ASTExtractorTest extends PreparationTest{

    @Test
    void extractMethodByName() {
        MethodDeclaration extractInput = getMethodDeclaration();
        assertNotNull(extractInput);
    }

    public static MethodDeclaration getMethodDeclaration() {
        CompilationUnit compilationUnit = TransformHelper.ASTExtractor.getCompilationUnit(filePath);
        MethodDeclaration extractInput = TransformHelper.ASTExtractor
                .extractMethodByName(
                        compilationUnit,
                        methodName);
        return extractInput;
    }

    @Test
    void extractMethodCallByLine() {
        CompilationUnit compilationUnit = TransformHelper.ASTExtractor.getCompilationUnit(filePath);
        MethodDeclaration extractInput = TransformHelper.ASTExtractor
                .extractMethodByName(
                        compilationUnit,
                        methodName);
        ASTExtractor ASTExtractor = TransformHelper.ASTExtractor;
        MethodCallExpr methodCallExpr = ASTExtractor.extractMethodCallByLine(extractInput, lineNumber);
        assertEquals(3, methodCallExpr.getArguments().size());
    }

    @Test
    void extractInput() {
        Input input = getInput();
        assertEquals("java.lang.Double", input.getType());
    }

    public static Input getInput() {
        MethodDeclaration extractInput = getMethodDeclaration();
        MethodCallExpr methodCallExpr = TransformHelper.ASTExtractor
                .extractMethodCallByLine(extractInput, lineNumber);
        return TransformHelper.ASTExtractor.extractInput(methodCallExpr);
    }
}