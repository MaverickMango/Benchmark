package root.util;

import root.entities.otherdataset.BugFixCommit;
import root.entities.otherdataset.CommitInfo;
import root.entities.otherdataset.RepositoryInfo;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BearsBugsUtils implements GitAccess {

    static Logger logger = LoggerFactory.getLogger(BearsBugsUtils.class);

    public static List<BugFixCommit> getBugsInfoFromJson(JsonArray jsonArray) {//from bears-bugs-json
        List<BugFixCommit> list = new ArrayList<>();
        for (JsonElement jsonElement : jsonArray) {
            JsonObject eachBugJson = jsonElement.getAsJsonObject();
            BugFixCommit bugFixCommit = new BugFixCommit();
            for (String key : eachBugJson.keySet()) {
                try {
                    switch (key) {
                        case "diff":
                            String diff = eachBugJson.getAsJsonPrimitive(key).getAsString();
                            bugFixCommit.setDiff(diff);
                            break;
                        case "bugId":
                            String bugId = eachBugJson.getAsJsonPrimitive(key).getAsString();
                            bugFixCommit.setBugId(bugId);
                            break;
                        case "bugName":
                            String bugName = eachBugJson.getAsJsonPrimitive(key).getAsString();
                            bugFixCommit.setBugName(bugName);
                            break;
                        case "commits":
                            JsonObject commits = eachBugJson.getAsJsonObject(key);
                            assert commits.keySet().size() >= 2;// not always be two
                            CommitInfo fixerBuild = FileUtils.json2Bean(commits.getAsJsonObject("fixerBuild").toString(), CommitInfo.class);
                            CommitInfo buggyBuild = FileUtils.json2Bean(commits.getAsJsonObject("buggyBuild").toString(), CommitInfo.class);
                            bugFixCommit.setFixedCommit(fixerBuild);
                            bugFixCommit.setInducingCommit(buggyBuild);
                            break;
                        case "repository":
                            JsonObject repository = eachBugJson.getAsJsonObject(key);
                            RepositoryInfo repo = FileUtils.json2Bean(repository.toString(), RepositoryInfo.class);
                            bugFixCommit.setRepo(repo);
                        default:
                            break;
                    }
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
            }
            list.add(bugFixCommit);
        }
        return list;
    }

    private static void checkout(String bugId, String path2dir){
        //python scripts/checkout_bug.py --bugId <bug ID> --workspace <path to folder to store Bears bugs>
        CommandSummary cs = new CommandSummary();
        cs.append("python3", ConfigurationProperties.getProperty("bears-benchmark") + "scripts/" + "checkout_bug.py");
        cs.append("--bugId", bugId);
        cs.append("--workspace", path2dir);
        String[] cmd = cs.flat();
        FileUtils.executeCommand(cmd);
    }

    public static List<BugFixCommit> getBugInfo() {
        String bears_bugs_json_path = ConfigurationProperties.getProperty("bears-benchmark") + "/docs/data/bears-bugs.json";
        JsonArray jarray = FileUtils.readJsonFile(bears_bugs_json_path).getAsJsonArray();
        return getBugsInfoFromJson(jarray);
    }

    public static Repository getGitRepository(String path2dir, String bugId){
        if (FileUtils.notExists(path2dir)) {
            checkout(bugId, path2dir);
        }
        try {
            return gitAccess.getGitRepository(new File(path2dir + "/.git"));
        } catch (Exception e) {
            logger.error("Can not get a git repository from " + path2dir);
            e.printStackTrace();
        }
        return null;
    }
}
