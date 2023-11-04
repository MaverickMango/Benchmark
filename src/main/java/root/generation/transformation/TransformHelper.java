package root.generation.transformation;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.bean.BugRepository;
import root.bean.benchmarks.Defects4JBug;
import root.generation.entity.Input;
import root.generation.entity.Skeleton;
import root.generation.helper.MutatorHelper;
import root.generation.parser.AbstractASTParser;
import root.generation.transformation.extractor.InputExtractor;
import root.util.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TransformHelper {

    private static final Logger logger = LoggerFactory.getLogger(TransformHelper.class);
    public static InputExtractor inputExtractor;
    public static InputTransformer inputTransformer;
    public static BugRepository bugRepository;

    public static void initialize(String proj, String id, String workingDir, String originalCommit, AbstractASTParser parser) {
        Defects4JBug defects4JBug = new Defects4JBug(proj, id, workingDir, originalCommit);
        bugRepository = new BugRepository(defects4JBug);
        inputTransformer = new InputTransformer();
        inputExtractor = new InputExtractor(parser);
    }

    public static Map<String, MethodDeclaration> mutateTest(Skeleton skeleton, List<Input> inputs, int mutantsNum) {
        List<Input> newInputs = new ArrayList<>();
        for (Input input :inputs) {//todo test inputs build
            List<Object> inputMutants = MutatorHelper.getInputMutants(input, mutantsNum);
            List<Input> tmp = inputTransformer.transformInput(input, inputMutants);
            newInputs.addAll(tmp);
            skeleton.addInput(input);
        }
        Map<String, MethodDeclaration> map = inputTransformer.buildNewTestByInputs(skeleton, newInputs);
        return map;
    }

    public static Map<String, MethodDeclaration> mutateTest(String fileAbsPath, String methodName, int lineNumber, int mutantsNum) {
        Input input = inputExtractor.extractInput(fileAbsPath, methodName, lineNumber);
        List<Object> inputMutants = MutatorHelper.getInputMutants(input, mutantsNum);
        List<Input> newInputs = inputTransformer.transformInput(input, inputMutants);
        Skeleton skeleton = createASkeleton(fileAbsPath, methodName, input);
        if (skeleton == null) {
            return null;
        }
        Map<String, MethodDeclaration> map = inputTransformer.buildNewTestByInputs(skeleton, newInputs);
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

    public static boolean applyPatch(Skeleton skeleton, Map<String, MethodDeclaration> map) {
        boolean res = bugRepository.switchToBug();
        if (!res) {
            logger.error("Error occurred when switching to buggy version!");
            return res;
        }
        String path = skeleton.getAbsolutePath();
        try {
            inputExtractor.getParser().parseASTs(path);
            CompilationUnit unit = (CompilationUnit) inputExtractor.getParser().getASTs().get(path);
            skeleton.setClazz(unit);
        } catch (Exception ignored) {}
        List<String> list = skeleton.applyPatch(bugRepository, map);
        //todo check patch execution result
        res = !list.isEmpty() && list.get(0).equals("0");
        return res;
    }
}
