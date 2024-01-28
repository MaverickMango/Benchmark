package root.script;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.analysis.StringFilter;
import root.entities.otherdataset.D4JBug;
import root.entities.benchmarks.Defects4JBug;
import root.util.ConfigurationProperties;
import root.util.FileUtils;
import root.util.GitAccess;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class DiffMain implements GitAccess {

    static Logger logger = LoggerFactory.getLogger(DiffMain.class);

    /**
     *
     * @param args [0]: proj_bugs file [1]: target bug cloned directory [2]: bugWithrepairTools.csv [3]: result_storage file
     */
    public static void total(String[] args) {
        List<List<String>> infos = FileUtils.readCsv(args[2], false);
        List<String> tools = infos.get(0);
        List<String> bugNames = infos.stream().map(info -> info.get(0)).collect(Collectors.toList());
        List<String> proj_bug = FileUtils.readEachLine(args[0]);
        int count = 0;
        int before = 0;
        for (String tmp : proj_bug) {
            boolean flag = false;
            String proj = tmp.split("_")[0];
            if (proj.equals("Closure") || proj.equals("Math") || proj.equals("Lang") || proj.equals("Time") || proj.equals("Mockito"))
                before ++;
            String id = tmp.split("_")[1];
            try {
                String bugName = proj + "_" + id + "_buggy";
                logger.info("Start processing " + bugName + "...");
                D4JBug bug = new D4JBug(proj, id, args[1] + bugName);
                int idx = bugNames.indexOf(tmp);if (idx == -1) continue;
                List<String> repairMode = infos.get(idx);
                for (int i = 1; i < repairMode.size(); i++) {
                    String m = repairMode.get(i);
                    if (!m.equals("")) {
                        flag = true;
                        bug.addRepairTool(tools.get(i), m);
                    }
                }
                if (flag) {
                    count ++;
                    String resultFilePath = args[3];
                    FileUtils.writeToFile(bug.toString() + ",", resultFilePath, true);
                }

                logger.info("Finished processing " + bugName + "...");
            } catch (Exception e) {
                logger.error("Error occurred: " + e.getMessage() + "...");
                e.printStackTrace();
            }
        }
        System.out.println("Finally repaired bug in diff File : " + count);
        System.out.println("before 1.5 bug in diff File : " + before);
        System.out.println("total bug in diff File : " + (proj_bug.size() - 1));
    }

    /**
     *
     * @param args [0]: proj_bugs.csv [1]: target bug cloned directory
     */
    public static void filterAnnotations(String[] args) {
        List<String> proj_bugs = FileUtils.readEachLine(args[0]);
        String resultFilePath = "data/diffFromReal2D4jBuggy/realDiff.txt";
        StringFilter filter = new StringFilter(StringFilter.STARTS_WITH);
        filter.addPattern("//");
        filter.addPattern("/*");
        filter.addPattern("*");
        for (String proj_bug : proj_bugs) {
            String proj = proj_bug.split(":")[0];
            String[] bugs = proj_bug.split(":")[1].split(",");
            for (String id : bugs) {
                try {
                    String bugName = proj + "_" + id + "_buggy";
                    logger.info("Start processing " + bugName + "...");

                    String inducingDiffDir = "data/diffFromReal2D4jBuggy/" + bugName + "/changes.diff";
                    if (FileUtils.notExists(inducingDiffDir))
                        continue;
                    List<String> diff = FileUtils.readEachLine(inducingDiffDir);
                    List<String> realDiff = diff.stream().filter(
                            line -> !filter(line, filter)
                    ).collect(Collectors.toList());
                    if (!realDiff.isEmpty()) {
                        FileUtils.writeToFile(proj + "_" + id + "\n", resultFilePath, true);
                    }

                    logger.info("Finished processing " + bugName + "...");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean filter(String line, StringFilter filter) {
        String start;
        if (line.startsWith("- ")) {
            start = "- ";
        } else if (line.startsWith("+ ")) {
            start = "\\+ ";
        } else {
            return true;
        }
        String[] split = line.split(start);
        if (split.length > 1) {
            String tmp = split[1].strip();
            return tmp.equals("") || filter.canMatch(tmp);
        }
        return false;
    }

    /**
     *
     * @param
     */
    public static void main(String[] args){
        String proj = args[0], id = args[1];
        String bugName = proj + "_" + id;
        logger.info("Start processing " + bugName + "...");
        try {
            Defects4JBug defects4JBug = new Defects4JBug(proj, id, "../bugs/" + bugName);
            String buggyCommit = defects4JBug.getBuggyCommit();
            String d4JBuggy = defects4JBug.getD4JBuggy();
            String modifiedClasses = ConfigurationProperties.getProperty("defects4j") + "/framework/projects/" + proj + "/modified_classes/" + id + ".src";
            List<String> lines = FileUtils.readEachLine(modifiedClasses);
            StringFilter filter = new StringFilter(StringFilter.MATCHES);
            filter.addPattern(".*Test.java$");
            filter.addPattern(".*test/.*");
            StringBuilder pattern = new StringBuilder("^(?!.*(?:");
            for (String clz : lines) {
                String s = clz.replaceAll("[.]", File.separator);
                pattern.append(s).append("|");
            }
            pattern.replace(pattern.length() - 1, pattern.length(), ")");
            pattern.append("\\.java$).*");
            filter.addPattern(pattern.toString());
            String diff = gitAccess.diffWithFilter(defects4JBug.getGitRepository("buggy"), buggyCommit, d4JBuggy, filter);
            String inducingDiffDir = "data/diffFromReal2D4jBuggy/" + bugName + "/changes.diff";
            if (diff != null) {
                logger.info("Writing diff in " + bugName + "...");
                FileUtils.writeToFile(diff, inducingDiffDir, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("Finished processing " + bugName + "...");
    }
}
