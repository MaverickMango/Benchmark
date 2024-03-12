package root.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.analysis.ASTExtractor;
import root.analysis.parser.AbstractASTParser;
import root.analysis.soot.SootUpAnalyzer;
import root.entities.ci.BugRepository;
import root.entities.benchmarks.Defects4JBug;
import root.entities.Patch;
import root.entities.ci.BugWithHistory;
import root.generation.entities.Skeleton;
import root.generation.transformation.TransformHelper;

import java.io.File;
import java.util.List;

public class PatchHelper {

    private final static Logger logger = LoggerFactory.getLogger(PatchHelper.class);
    private static String target;
    private static String resOutput;
    private static String testOutput;
    public static root.analysis.ASTExtractor ASTExtractor;
    public static BugRepository patchRepository;

    public static void initialization(BugWithHistory pat, AbstractASTParser parser, SootUpAnalyzer analyzer) {
        String bugName = TransformHelper.bugRepository.getBug().getBugName();
        //save the generatedOracle file.
        target = ConfigurationProperties.getProperty("resultOutput") + File.separator
                + bugName + File.separator + "generatedOracles" + File.separator;
        resOutput = ConfigurationProperties.getProperty("resultOutput") + File.separator
                + bugName + File.separator + "result" + File.separator;
        testOutput = ConfigurationProperties.getProperty("resultOutput") + File.separator
                + bugName + File.separator + "generatedTests";
        if (FileUtils.notExists(testOutput)) {
            FileUtils.mkdirs(testOutput);
        }
        ASTExtractor = new ASTExtractor(parser);
        patchRepository = new BugRepository(pat, analyzer);
        patchRepository.switchToBug();
        patchRepository.compile();
    }

    public static boolean validate(Patch patch, List<Skeleton> skeletons) {
        logger.info("Applying patch: " + patch);
        boolean res = patchRepository.applyPatch(patch);
        if (!res) {
            logger.error("Error occurred when applying patch " + patch.getName());
            return res;
        }
        for (Skeleton skeleton :skeletons) {
            //copy oracle file
            FileUtils.copy(new File(skeleton.getOracleFilePath()), new File(target));
            List<String> failed = skeleton.runGeneratedTests(skeleton.getGeneratedMethods(), testOutput);
            if (failed == null) {
                logger.info("Test execution error in patched version!");
                continue;
            }
            res &= failed.isEmpty();//failed为空贼说明没有失败测试，是一个正确的补丁。
        }
        String correctness = res ? "correct" : "incorrect";
        logger.info("Patch correctness : " + correctness + " for " + patch.getName());
        String content = patch.getPatchAbsPath() + "#" + correctness + "\n";
        FileUtils.writeToFile(content, resOutput, true);
        return res;
    }
}
