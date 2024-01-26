package root.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.entity.BugRepository;
import root.entity.benchmarks.Defects4JBug;
import root.generation.entity.Patch;
import root.generation.entity.Skeleton;
import root.generation.transformation.TransformHelper;

import java.io.File;
import java.util.List;

public class PatchHelper {

    private final static Logger logger = LoggerFactory.getLogger(PatchHelper.class);
    private static String target;
    private static String resOutput;
    private static BugRepository bugRepository;

    public static void initialization() {
        //copy patch directory
        String workingDir = ConfigurationProperties.getProperty("location");
        String patchDir = workingDir.replace("_buggy", "_pat");
        if (!new File(patchDir).exists()) {
            String[] cmd = new String[]{"/bin/bash", "-c", "cp -r " + workingDir + " " + patchDir};
            FileUtils.executeCommand(cmd);
        }
        ConfigurationProperties.setProperty("patchDir", patchDir);

        String bugName = TransformHelper.bugRepository.getBug().getBugName();
        //save the generatedOracle file.
        target = ConfigurationProperties.getProperty("resultOutput") + File.separator
                + bugName + File.separator + "generatedOracles" + File.separator;
        resOutput = ConfigurationProperties.getProperty("resultOutput") + File.separator
                + bugName + File.separator + "result" + File.separator;
        Defects4JBug bug = (Defects4JBug) TransformHelper.bugRepository.getBug();
        Defects4JBug pat = new Defects4JBug(bug.getProj(), bug.getId(), patchDir,
                bug.getFixingCommit(), bug.getBuggyCommit(), bug.getInducingCommit(), bug.getOriginalCommit());
        bugRepository = new BugRepository(pat);
        bugRepository.switchToBug();
    }

    public static boolean validate(Patch patch, List<Skeleton> skeletons) {
        logger.info("Applying patch: " + patch);
        boolean res = bugRepository.applyPatch(patch);
        if (!res) {
            logger.error("Error occurred when applying patch " + patch.getName());
            return res;
        }
        for (Skeleton skeleton :skeletons) {
            //copy oracle file
            FileUtils.copy(new File(skeleton.getOracleFilePath()), new File(target));
            List<String> failed = skeleton.runGeneratedTests(skeleton.getGeneratedMethods());
            if (failed == null) {
                logger.info("Test execution error in patched version!");
                continue;
            }
            res &= failed.isEmpty();//failed为空贼说明没有失败测试，是一个正确的补丁。
        }
        String correctness = res ? "correct" : "incorrect";
        logger.info("Patch correctness for " + patch.getName() + ": " + correctness);
        String content = patch.getPatchAbsPath() + "#" + correctness + "\n";
        FileUtils.writeToFile(content, resOutput, true);
        return res;
    }
}
