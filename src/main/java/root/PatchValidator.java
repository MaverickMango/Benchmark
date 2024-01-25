package root;

import com.github.javaparser.ast.body.MethodDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.entity.BugRepository;
import root.entity.benchmarks.Defects4JBug;
import root.entity.ci.CIBug;
import root.generation.entity.Patch;
import root.generation.entity.Skeleton;
import root.generation.transformation.TransformHelper;
import root.util.ConfigurationProperties;
import root.util.FileUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

public class PatchValidator {

    private final Logger logger = LoggerFactory.getLogger(PatchValidator.class);
    private final String target;
    private final String resOutput;
    private final BugRepository bugRepository;

    public PatchValidator() {
        String bugName = TransformHelper.bugRepository.getBug().getBugName();
        //save the generatedOracle file.
        this.target = ConfigurationProperties.getProperty("resultOutput") + File.separator
                + bugName + File.separator + "generatedOracles" + File.separator;
        this.resOutput = ConfigurationProperties.getProperty("resultOutput") + File.separator
                + bugName + File.separator + "result" + File.separator;
        Defects4JBug bug = (Defects4JBug) TransformHelper.bugRepository.getBug();
        bug.setWorkingDir(ConfigurationProperties.getProperty("patchDir"));
        this.bugRepository = new BugRepository(bug);
    }

    public boolean validate(List<Patch> patches, List<Skeleton> skeletons) {
        boolean res = false;
        for (Patch patch: patches) {
            res = bugRepository.switchToBug();
            if (!res) {
                logger.error("Error occurred when switching to buggy version!");
                return res;
            }
            logger.info("Applying patch: " + patch);
            //todo another better way to apply patch???
            String patchDir = ConfigurationProperties.getProperty("patchDir") + File.separator;
            String[] cmd = new String[]{"/bin/bash", "-c", "cp -r "+ patch.getPatchDir() + File.separator + "* " + patchDir};
            FileUtils.executeCommand(cmd);
            res = validatePatch(patch, skeletons);
        }
        return res;
    }

    private boolean validatePatch(Patch patch, List<Skeleton> skeletons) {
        boolean res = false;
        for (Skeleton skeleton :skeletons) {
            //copy oracle file
            FileUtils.copy(new File(skeleton.getOracleFilePath()), new File(target));
            List<String> failed = skeleton.runGeneratedTests(skeleton.getGeneratedMethods());
            if (failed == null) {
                logger.info("Test execution error in patched version!");
                continue;
            }
            res = failed.isEmpty();//failed为空贼说明没有失败测试，是一个正确的补丁。
            String correctness = res ? "correct" : "incorrect";
            logger.info("Patch correctness for " + patch.getName() + ": " + correctness);
            String content = patch.getPatchAbsPath() + "#" + correctness + "\n";
            FileUtils.writeToFile(content, resOutput, true);
        }
        return res;
    }
}
