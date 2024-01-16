package root.analysis.groum.extractor;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import root.AbstractMain;
import root.analysis.groum.entity.Groum;
import root.generation.ProjectPreparation;
import root.parser.ASTJavaParser;
import root.util.CommandSummary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExtractFromJavaParserTest {
    public static CommandSummary cs = new CommandSummary();
    public static ProjectPreparation projectPreparation;
    public static String filePath;// = "/home/liumengjiao/Desktop/CI/bugs/Closure_10_bug/test/com/google/javascript/jscomp/PeepholeFoldConstantsTest.java";
    public static String methodName;// = "testIssue821";
    public static int lineNumber;// = 582;

    @BeforeAll
    static void beforeAll() {
        cs.append("-location", "./");
        cs.append("-srcJavaDir", "src/main/java");
        cs.append("-srcTestDir", "src/test/java");
        cs.append("-binJavaDir", "build/classes/java/main");
        cs.append("-binTestDir", "build/classes/java/test");
        cs.append("-complianceLevel", "1.8");
        AbstractMain main = new AbstractMain();
        projectPreparation = main.initialize(cs.flat());
    }

    @Test
    void visit() throws IOException {
        ASTJavaParser parser = (ASTJavaParser) projectPreparation.parser;
        parser.parseASTs("src/main/java/root/generation/transformation/extractor/InputExtractor.java");
        Map<String, Object> asts = parser.getASTs();
        PreOrderVisitorInMth visitor = new PreOrderVisitorInMth();
        ArrayList<Groum> p = new ArrayList<>();
        for (String key :asts.keySet()) {
            CompilationUnit o = (CompilationUnit) asts.get(key);
            NodeList<BodyDeclaration<?>> members = o.getType(0).getMembers();
            for (BodyDeclaration b :members) {
                ArrayList<Groum> arg = new ArrayList<>();
                if (b instanceof MethodDeclaration) {
                    visitor.visit((MethodDeclaration) b, arg);
                }
                if (arg.isEmpty())
                    continue;
                p.add(arg.get(0));
            }
        }
        assertFalse(p.isEmpty());
    }
}