package root.diff;

import com.github.javaparser.ast.Node;
import com.github.javaparser.utils.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import root.generation.transformation.TransformHelper;
import root.analysis.parser.ASTJavaParser;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class DiffExtractorTest {

    @Test
    void getDiffMths() throws IOException {
        TransformHelper.initialize(null,null,
                new ASTJavaParser("src/main/java", "src/test/java", null, "1.8"),
                null);
        String srcPath = "/home/liumengjiao/Desktop/CI/Benchmark/src/test/resources/examples/src/FileRead.java";
        String dstPath = "/home/liumengjiao/Desktop/CI/Benchmark/src/test/resources/examples/dst/FileRead.java";
        DiffExtractor extractor = new DiffExtractor();
        List<Pair<Node, Node>> diffMths = extractor.getDifferentPairs(srcPath, dstPath, null, null,"buggy", "buggy");
        Assertions.assertFalse(diffMths.isEmpty());
    }

    @Test
    void minimal() throws IOException {
        TransformHelper.initialize(null,null,
                new ASTJavaParser("src/main/java", "src/test/java", null, "1.8"),
                null);
        String srcPath = "/home/liumengjiao/Desktop/CI/Benchmark/src/test/resources/examples/src/FileRead.java";
        String dstPath = "/home/liumengjiao/Desktop/CI/Benchmark/src/test/resources/examples/dst/FileRead.java";
        DiffExtractor extractor = new DiffExtractor();
        List<Pair<Node, Node>> diffMths = extractor.getDifferentPairs(srcPath, dstPath, null, null,"buggy", "buggy");
        Set<Node> A = diffMths.stream().map(n -> n.a).collect(Collectors.toSet());
        DiffExtractor.filterChildNode(A);
        Set<Node> B = diffMths.stream().map(n -> n.b).collect(Collectors.toSet());
        DiffExtractor.filterChildNode(B);
        Pair<Set<Node>, Set<Node>> minimalDiffNodes = DiffExtractor.getMinimalDiffNodes(A, B);
        Assertions.assertEquals(2, minimalDiffNodes.a.size());
        Assertions.assertEquals(2, minimalDiffNodes.b.size());
    }

}