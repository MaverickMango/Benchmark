package root.diff;

import com.github.javaparser.ast.Node;
import com.github.javaparser.utils.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import root.generation.transformation.TransformHelper;
import root.parser.ASTJavaParser;

import java.io.IOException;
import java.util.List;

class DiffExtractorTest {

    @Test
    void getDiffMths() throws IOException {
        TransformHelper.initialize(null,
                new ASTJavaParser("src/main/java", "src/test/java", null, "1.8"));
        String srcPath = "/home/liumengjiao/Desktop/CI/Benchmark/src/test/resources/examples/src/FileRead.java";
        String dstPath = "/home/liumengjiao/Desktop/CI/Benchmark/src/test/resources/examples/dst/FileRead.java";
        DiffExtractor extractor = new DiffExtractor();
        List<Pair<Node, Node>> diffMths = extractor.getDifferentPairs(srcPath, dstPath, null, null,"buggy", "buggy");
        Assertions.assertFalse(diffMths.isEmpty());
    }

    @Test
    void getInducingRelevantDiffNodes() {
    }

}