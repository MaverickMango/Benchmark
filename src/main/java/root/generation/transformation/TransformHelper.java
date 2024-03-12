package root.generation.transformation;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.analysis.soot.SootUpAnalyzer;
import root.entities.Difference;
import root.entities.benchmarks.Defects4JBug;
import root.entities.ci.BugRepository;
import root.entities.ci.BugWithHistory;
import root.generation.entities.Input;
import root.generation.entities.Skeleton;
import root.analysis.parser.AbstractASTParser;
import root.analysis.ASTExtractor;
import root.util.ConfigurationProperties;
import root.util.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TransformHelper {

    private static final Logger logger = LoggerFactory.getLogger(TransformHelper.class);
    public static final String oracleOutputs = ConfigurationProperties.getProperty("location") + File.separator + "generatedOracles.txt";
    public static ASTExtractor ASTExtractor;
    public static InputTransformer inputTransformer;
    public static BugRepository bugRepository;
    public static BugRepository orgRepository;

    public static void initialize(BugWithHistory buggy, BugWithHistory org, AbstractASTParser parser, SootUpAnalyzer analyzer) {
        if (buggy != null) {
            bugRepository = new BugRepository(buggy, analyzer);
            orgRepository = new BugRepository(org, analyzer);
            orgRepository.switchToOtgAndClean();
        }
        inputTransformer = new InputTransformer();
        ASTExtractor = new ASTExtractor(parser);
    }

    public static Map<String, MethodDeclaration> mutateTest(Skeleton skeleton, List<Input> inputs, List<Difference> differences) {
        List<Input> newInputs = new ArrayList<>();
        logger.info("Mutating test inputs... total inputs num: " + inputs.size());
        for (Input input :inputs) {
            logger.info("getting mutants...");
            List<Pair<Expression, Object>> inputMutants = MutateHelper.getInputMutants(input, differences);
            logger.info("transforming...");
            List<Input> tmp = inputTransformer.transformInput(input, inputMutants);
            newInputs.addAll(tmp);
            skeleton.addInput(input);
        }
        logger.info("Building new tests...");
        Map<String, MethodDeclaration> map = inputTransformer.buildNewTestByInputs(skeleton, newInputs);

        logger.info("Saving all new inputs...");
        //todo
        return map;
    }

    public static Skeleton createASkeleton(String fileAbsPath, String clazzName, Input input) {
        ArrayList<Input> inputs = new ArrayList<>();
        inputs.add(input);
        return createASkeleton(fileAbsPath, clazzName, inputs);
    }

    public static Skeleton createASkeleton(String fileAbsPath, String clazzName, List<Input> inputs) {
        boolean res = bugRepository.switchToOrg();
        if (!res) {
            logger.error("Error occurred when getting oracle in original commit! May be it cannot be compiled successfully.\n Null will be returned");
            return null;
        }
        CompilationUnit compilationUnit = ASTExtractor.getCompilationUnit(fileAbsPath);
        Skeleton skeleton = new Skeleton(fileAbsPath, compilationUnit, clazzName, inputs);
        return skeleton;
    }

    public static List<String> saveAndTest(CompilationUnit unit, String targetPath, String testNames) {
        //todo internal test?
        FileUtils.writeToFile(unit.toString(), targetPath, false);
        List<String> tests = new ArrayList<>();
        boolean flag = false;
        for (String test :testNames.split(" ")) {
            List<String> r = bugRepository.testWithRes(test);
            if (r.isEmpty() || !r.get(0).equals("0")) {
                logger.info("Test execution error! :\n" + FileUtils.getStrOfIterable(r, "\n"));
                continue;
            }
            if (r.size() != 3) {//大于3说明有失败测试
                tests.add(test);
            }
            flag = true;
        }
        if (!flag) {
            return null;
        }
        return tests;
    }

    public static String getIdentifier(Node node) {
        if (node instanceof MethodDeclaration) {
            String nameAsString = ((MethodDeclaration) node).getNameAsString();
            //todo ?
            return nameAsString;
        }
        return node.toString();
    }

}
