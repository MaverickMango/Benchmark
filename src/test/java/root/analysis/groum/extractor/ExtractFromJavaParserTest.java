package root.analysis.groum.extractor;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import root.AbstractMain;
import root.analysis.groum.entity.IntraGroum;
import root.analysis.groum.Graphvizer;
import root.ProjectPreparation;
import root.analysis.parser.ASTJavaParser;
import root.util.CommandSummary;
import root.util.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
//        setInputs();
//        AbstractMain main = new AbstractMain();
//        projectPreparation = main.initialize(cs.flat());
//
//        String testInfos = ConfigurationProperties.getProperty("testInfos");
//        String[] split = testInfos.split("#");
//        if (split.length >= 1) {
//            String triggerTest1 = split[0];
//            String[] split1 = triggerTest1.split(":");
//            filePath = projectPreparation.srcTestDir + File.separator +
//                    split1[0].replaceAll("\\.", File.separator) + ".java";
//            methodName = split1[1];
//            if (split1.length == 3) {
//                lineNumber = Integer.parseInt(split1[2]);
//            }
//        }
    }

    private static void setInputs() {
        String info = "/home/liumengjiao/Desktop/CI/Benchmark_py/generation/info/patches_inputs.csv";
        List<List<String>> lists = FileUtils.readCsv(info, true);
        String location = "/home/liumengjiao/Desktop/CI/bugs/";
        String bugName, srcJavaDir, srcTestDir, binJavaDir, binTestDir, testInfos, projectCP, originalCommit, cleaned, complianceLevel;
        List<String> strings = lists.get(46);
        bugName = strings.get(0);
        srcJavaDir = strings.get(1);
        srcTestDir = strings.get(2);
        binJavaDir = strings.get(3);
        binTestDir = strings.get(4);
        testInfos = strings.get(5);
        projectCP = strings.get(6);
        originalCommit = strings.get(7);
        cleaned = strings.get(8);
        cs.append("-proj", bugName.split("_")[0]);
        cs.append("-id", bugName.split("_")[1]);
        cs.append("-location", location + bugName + "_buggy");
        cs.append("-srcJavaDir", srcJavaDir);
        cs.append("-srcTestDir", srcTestDir);
        cs.append("-binJavaDir", binJavaDir);
        cs.append("-binTestDir", binTestDir);
        cs.append("-testInfos", testInfos);
        cs.append("-dependencies", projectCP);
        cs.append("-originalCommit", originalCommit);
//        cs.append("-complianceLevel", "1.6");
//        cs.append("-patchesDir", "/home/liumengjiao/Desktop/CI/patches/patches_plausible/Closure/Closure_10/");
    }

    @Test
    void visit() throws IOException {
        ASTJavaParser parser = (ASTJavaParser) projectPreparation.bugParser;
        parser.parseASTs("src/test/java/FileRead.java");
//        parser.parseASTs("src/main/java/root/generation/transformation/extractor/InputExtractor.java");
//        parser.parseASTs("/home/liumengjiao/Desktop/CI/bugs/Math_50_buggy/src/main/java/org/apache/commons/math/analysis/solvers/BaseSecantSolver.java");
        Map<String, Object> asts = parser.getASTs();
        PreOrderVisitorInMth visitor = new PreOrderVisitorInMth(false);
        ArrayList<IntraGroum> p = new ArrayList<>();
        for (String key :asts.keySet()) {
            CompilationUnit o = (CompilationUnit) asts.get(key);
            NodeList<BodyDeclaration<?>> members = o.getType(0).getMembers();
            for (BodyDeclaration b :members) {
                ArrayList<IntraGroum> arg = new ArrayList<>();
                if (b instanceof MethodDeclaration) {
//                    if (!((MethodDeclaration) b).getNameAsString().equals("doSolve")) {
//                        continue;
//                    }
                    visitor.visit((MethodDeclaration) b, arg);
                    if (arg.isEmpty())
                        continue;
                    assert arg.size() == 1;
                    p.add(arg.get(0));
                    if (arg.get(0).getNodes().size() > 3) {
                        String filePath = "example/example.svg";
                        Graphvizer er = new Graphvizer();
                        er.outputGraph(arg.get(0), filePath);
                        assert true;
                    }
                }
            }
        }
        assertFalse(p.isEmpty());
    }
}