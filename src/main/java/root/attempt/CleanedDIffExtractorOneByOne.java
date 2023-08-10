package root.attempt;

import root.util.FileUtils;
import root.util.GitAccess;

import java.util.List;

public class CleanedDIffExtractorOneByOne implements GitAccess {

    public static void main(String[] args) {
        String bugInfo = "src/test/resources/BugFixInfo.csv";
        List<List<String>> d4jinfos = FileUtils.readCsv(bugInfo, true);
        for (int i = 0; i < d4jinfos.size(); i ++) {
            String bugName = d4jinfos.get(i).get(2);
            String proj = bugName.split("_")[0];
            String id = bugName.split("_")[1];
            String bug_tag = proj + "_" + id;
            if (!bug_tag.equals("Closure_170"))
                continue;
            // get diff infos
            String originalDir = "data/changesInfo/" + bug_tag + "/properties/modified_classes/inducing";
            String fixingDir = "data/changesInfo/" + bug_tag + "/cleaned/fixing";
            if (FileUtils.notExists(fixingDir))
                continue;
            String inducingDir = "data/changesInfo/" + bug_tag + "/cleaned/inducing";
            String fixingDiff = "data/changesInfo/" + bug_tag + "/patches/cleaned.fixing.diff";
            String inducingDiff = "data/changesInfo/" + bug_tag + "/patches/cleaned.inducing.diff";
            List<String> diffs = FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "diff -u -r " + originalDir + " " + inducingDir});
            FileUtils.writeToFile(FileUtils.getStrOfIterable(diffs, "\n").toString(), inducingDiff, false);
            diffs = FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "diff -u -r " + originalDir + " " + fixingDir});
            FileUtils.writeToFile(FileUtils.getStrOfIterable(diffs, "\n").toString(), fixingDiff, false);
        }
    }
}
