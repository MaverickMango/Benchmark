package root.generation.parser;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ASTJavaParserTest {

    @Test
    void parseASTs() throws IOException {
        ASTJavaParser parser = new ASTJavaParser("src/main/java", "src/test/java/", null, "1.8");
        parser.parseASTs("src/main/java/root/generation/extractor/InputExtractor.java");
        Map<String, Object> asts = parser.getASTs();
        assertFalse(asts.isEmpty());
    }
}