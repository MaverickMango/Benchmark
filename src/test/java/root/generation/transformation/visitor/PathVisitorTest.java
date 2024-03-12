package root.generation.transformation.visitor;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.Test;
import root.entities.PathFlow;
import root.util.FileUtils;

import java.util.ArrayList;
import java.util.List;

public class PathVisitorTest {

    @Test
    public void testSwitch() {
        PathVisitor pathVisitor = new PathVisitor();
        ArrayList<Integer> lineno = new ArrayList<>(List.of(1));
        pathVisitor.setLineno(lineno);
        PathFlow pathFlow = new PathFlow();
        pathFlow.addVariable("band");
        JavaParser javaParser = new JavaParser();
        ParseResult<CompilationUnit> parse =
                javaParser.parse(FileUtils.readFileByLines("src/test/resources/examples/src/TestClass.java"));
        parse.getResult().ifPresent(c -> {
            c.accept(pathVisitor, pathFlow);
        });
    }

}