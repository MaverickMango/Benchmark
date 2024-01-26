package root.generation.transformation;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.entity.BugRepository;
import root.entity.benchmarks.Defects4JBug;
import root.generation.entity.Input;
import root.generation.entity.Skeleton;
import root.parser.AbstractASTParser;
import root.generation.transformation.extractor.InputExtractor;
import root.util.ConfigurationProperties;
import root.util.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TransformHelper {

    private static final Logger logger = LoggerFactory.getLogger(TransformHelper.class);
    public static final String oracleOutputs = ConfigurationProperties.getProperty("location") + File.separator + "generatedOracles.txt";
    public static InputExtractor inputExtractor;
    public static InputTransformer inputTransformer;
    public static BugRepository bugRepository;

    public static void initialize(String proj, String id, String workingDir, String originalCommit, AbstractASTParser parser) {
        if (proj != null) {
            Defects4JBug defects4JBug = new Defects4JBug(proj, id, workingDir, originalCommit);
            bugRepository = new BugRepository(defects4JBug);
            defects4JBug.setInducingCommit(bugRepository);
        }
        inputTransformer = new InputTransformer();
        inputExtractor = new InputExtractor(parser);
    }

    public static Map<String, MethodDeclaration> mutateTest(Skeleton skeleton, List<Input> inputs, int mutantsNum) {
        List<Input> newInputs = new ArrayList<>();
        logger.info("Mutating test inputs... Expected num for each: " + mutantsNum + ", total inputs num: " + inputs.size());
        for (Input input :inputs) {
            logger.info("getting mutants...");
            List<Pair<Expression, Object>> inputMutants = MutateHelper.getInputMutants(input, mutantsNum);
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
        CompilationUnit compilationUnit = inputExtractor.getCompilationUnit(fileAbsPath);
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

}
