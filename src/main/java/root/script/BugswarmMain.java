package root.script;

import com.google.gson.JsonElement;
import root.entities.otherdataset.BugswarmShow;
import root.util.FileUtils;
import root.util.GitAccess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BugswarmMain implements GitAccess {
//[{"image_tag":"square-okio-140452393","lang":"Java","metrics":{"changes":75},"reproducibility_status":{"status":"Reproducible"},"failed_job":{"job_id":140452393,"build_id":140452392,"num_tests_run":579,"num_tests_failed":2,"trigger_sha":"73e1f77abe183f4af00134b6159fa6c87821d3ef"},"passed_job":{"trigger_sha":"34a634cfd86292e7c13bc3c5e80ef5795d989424"},"classification":{"build":"No","code":"Partial","exceptions":["ArrayIndexOutOfBoundsException"],"test":"Partial"},"diff_url":"https://github.com/square/okio/compare/73e1f77abe183f4af00134b6159fa6c87821d3ef..34a634cfd86292e7c13bc3c5e80ef5795d989424"}]
    public static void main(String[] args) throws Exception {
        String dockerhubPrefix = "bugswarm/images:";
        String bugsLocation = "/home/liumengjiao/Desktop/CI/bugswarm-bugs/bugs/";
        String jsonFile = "/home/liumengjiao/Desktop/CI/bugswarm-bugs/bugswarmShow.json";
        JsonElement export = FileUtils.readJsonFile(jsonFile);
        List<String> failed = new ArrayList<>();
        List<JsonElement> jsonElements = Collections.singletonList(export.getAsJsonArray());
        List<BugswarmShow> bugswarms
                = jsonElements.stream().map(e -> FileUtils.json2Bean(e.toString(), BugswarmShow.class)).collect(Collectors.toList());
//        List<Bugswarm> bugsUseful = bugswarms.stream().filter(bug -> bug.getFailedJob().getNumTestsFailed() >= 1).collect(Collectors.toList());
        List<BugswarmShow> bugsUseful = bugswarms.stream().filter(
                bug -> bug.getReproducibilityStatus().getStatus().equals("Reproducible")
                        && !bug.getFailedJob().getFailedTests().equals("")
                        && bug.getPrNum() == -1
        ).collect(Collectors.toList());
        int total = bugsUseful.size();
        for (BugswarmShow bug :bugsUseful) {
            try {
//                String repoName = bug.getImageTag().substring(0, bug.getImageTag().lastIndexOf("-"));
//                String repo = bug.getDiffUrl().substring(0, bug.getDiffUrl().lastIndexOf("/compare")) + ".git";
//                failed.add(bug.getImageTag() + "," + repoName + "," + repo);
//                gitAccess.cloneIfNotExists(bugsLocation + repoName, repo);
//                FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "docker pull " + dockerhubPrefix + bug.getImageTag()});
                String repo = bug.getRepo();
                FileUtils.writeToFile(dockerhubPrefix + bug.getCurrentImageTag() + "\n", "data/bugWithOutPR.txt", true);
                FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "docker pull " + dockerhubPrefix + bug.getCurrentImageTag()}, null, 600, null);
//                int res = FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "docker pull " + dockerhubPrefix + bug.getCurrentImageTag()}, null, 600, null);
//                FileUtils.executeCommand(new String[]{"/bin/bash", "-c", "git clone " + gitAccess.getRepositoryURL(repo) + " " + repo.substring(repo.lastIndexOf("/") + 1)}, bugsLocation, 300, null);
//                if (res != 0) {
//                    //最后一个bugswarm/images:raphw-byte-buddy-101978585没执行完
//                    total --;
//                }
//                gitAccess.cloneIfNotExists(bugsLocation + repo.substring(repo.lastIndexOf("/") + 1), gitAccess.getRepositoryURL(repo));
            } catch (Exception e) {
                total --;
//                failed.add(bug.getImageTag());
            }
        }
//        System.out.println(total);
    }

}
