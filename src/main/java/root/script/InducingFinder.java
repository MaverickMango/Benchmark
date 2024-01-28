package root.script;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.entities.benchmarks.Defects4JBug;
import root.util.FileUtils;
import root.util.GitAccess;

import java.util.*;

public class InducingFinder implements GitAccess {
    private static final Logger logger = LoggerFactory.getLogger(InducingFinder.class);

    public static void main(String[] args) {
        String filePath = args[0];//"bug.csv";//
        List<String> succeed = new ArrayList<>();
        Map<String, String[]> filter = new HashMap<>();
        List<String> proj_bugs = FileUtils.readEachLine(filePath);
        logger.info("Starting find inducing commit...");
        for (String proj_bug: proj_bugs) {
            String proj = proj_bug.split(":")[0];
            String[] bugs = proj_bug.split(":")[1].split(",");
            for (String id :bugs) {
                String bugName = proj + "_" + id + "_buggy";
                logger.info("Starting process " + bugName + "...");
                Defects4JBug defects4JBug = new Defects4JBug(proj, id, "data/bugs/" + bugName);
                String bugFixingCommit = defects4JBug.getFixingCommit();
                Repository repo = defects4JBug.getGitRepository("b");
                List<RevCommit> revsWalkOfAll = gitAccess.createRevsWalkOfAll(repo, true);
                String bugInducingCommit = revsWalkOfAll.get(0).getName();
                String res = null;
                String fakeInducing = "";
                try {
                    res = defects4JBug.findInducingCommit(bugFixingCommit, bugInducingCommit, false);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.info("error occurred when processing " + bugName + ": " + e.getMessage());
                }
                if (res != null) {
                    logger.info("----------" + bugName + " can not find inducing commit");
                    FileUtils.writeToFile(proj + "," + id + "," + fakeInducing + "\n", "data/bug_inducing_commits", true);
                }
                logger.info("Finished processing " + bugName + "...");
            }
        }
//        logger.info("Starting find original commit...");
//        //original wrong
//        filter.put("Closure", new String[]{"19","52","59","60","66","68","76","80","82","85","90","99","118","131"});
//        filter.put("Math", new String[]{"12","13","26","45","46","48","60","74","88"});
//        filter.put("Time", new String[]{"23"});
//        for (List<String> bug :d4jinfos) {
//            String bugName = bug.get(2);
//            String proj = bugName.split("_")[0];
//            String id = bugName.split("_")[1];
//            if (!filter.containsKey(proj) || !Arrays.asList(filter.get(proj)).contains(id))
//                continue;
//            logger.info("Starting process " + bugName + "...");
//            String bugFixingCommit = bug.get(3);
//            String bugInducingCommit = bug.get(5);
//            String fakeInducing = "";
//            String res = null;
//            try {
//                Defects4JBug defects4JBug = new Defects4JBug(proj, id, "tmp/bugs/" + bugName);
//                res = defects4JBug.findInducingCommit(bugFixingCommit, bugInducingCommit, true);
//            } catch (Exception e) {
//                e.printStackTrace();
//                logger.info("error occurred when processing " + bugName + ": " + e.getMessage());
//            }
//            if (res != null) {
//                logger.info("----------" + bugName + " can not find original commit");
//                FileUtils.writeToFile(proj + "," + id + "," + fakeInducing + "\n", "tmp/bug_original_commits", true);
//            }
//            logger.info("Finished processing " + bugName + "...");
//        }
//        logger.info("Starting find inducing commit...");
//        //inducing wrong
//        filter.clear();
//        filter.put("Closure", new String[]{"130"});
//        filter.put("Math", new String[]{"14","23","24"});
//        for (List<String> bug :d4jinfos) {
//            String bugName = bug.get(2);
//            String proj = bugName.split("_")[0];
//            String id = bugName.split("_")[1];
//            if (!filter.containsKey(proj) || !Arrays.asList(filter.get(proj)).contains(id))
//                continue;
//            logger.info("Starting process " + bugName + "...");
//            String bugFixingCommit = bug.get(3);
//            String bugInducingCommit = bug.get(5);
//            String res = null;
//            String fakeInducing = "";
//            try {
//                Defects4JBug defects4JBug = new Defects4JBug(proj, id, "tmp/bugs/" + bugName);
//                res = defects4JBug.findInducingCommit(bugFixingCommit, bugInducingCommit, false);
//            } catch (Exception e) {
//                e.printStackTrace();
//                logger.info("error occurred when processing " + bugName + ": " + e.getMessage());
//            }
//            if (res != null) {
//                logger.info("----------" + bugName + " can not find inducing commit");
//                FileUtils.writeToFile(proj + "," + id + "," + fakeInducing + "\n", "tmp/bug_inducing_commits", true);
//            }
//            logger.info("Finished processing " + bugName + "...");
//        }

    }
}
