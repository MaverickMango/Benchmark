package root.generation.helper;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.bean.BugRepository;
import root.bean.benchmarks.Defects4JBug;
import root.generation.entity.Skeleton;
import root.generation.parser.AbstractASTParser;
import root.generation.transformation.InputTransformer;
import root.generation.transformation.extractor.InputExtractor;

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

    public static Skeleton createASkeleton(String fileAbsPath, MethodDeclaration methodDeclaration) {
        boolean res = bugRepository.switchToOrg();
        if (!res) {
            logger.error("Error occurred when getting oracle in original commit! May be it cannot be compiled successfully.\n Null will be returned");
            return null;
        }
        CompilationUnit compilationUnit = inputExtractor.getCompilationUnit(fileAbsPath);
        Skeleton skeleton = new Skeleton(fileAbsPath, compilationUnit, methodDeclaration);
        return skeleton;
    }
}
