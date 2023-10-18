package root.generation.extractor;

import com.github.javaparser.ast.body.MethodDeclaration;
import root.generation.parser.AbstractASTParser;

import java.util.List;

public class TestExtractor {

    AbstractASTParser parser;

    public TestExtractor(AbstractASTParser parser) {
        this.parser = parser;
    }

    public void extractTestByName(String methodName, List<?> targetImports,
                                  List<MethodDeclaration> dependencies) {

    }
}
