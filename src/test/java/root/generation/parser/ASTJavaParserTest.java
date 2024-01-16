package root.generation.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.Statement;
import org.junit.jupiter.api.Test;
import root.parser.ASTJavaParser;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ASTJavaParserTest {

    @Test
    void parseASTs() throws IOException {
        ASTJavaParser parser = new ASTJavaParser("src/main/java", "src/test/java/", null, "1.8");
        parser.parseASTs("src/main/java/root/generation/transformation/extractor/InputExtractor.java");
        Map<String, Object> asts = parser.getASTs();
        assertFalse(asts.isEmpty());
    }

}