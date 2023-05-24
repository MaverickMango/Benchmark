package attempt;

import analysis.RefactoringMiner;
import bean.BugFixCommit;
import com.github.gumtreediff.actions.model.Action;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Ignore;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.astDiff.actions.ASTDiff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.BearsBugsTools;
import util.GitTools;

import java.util.List;
import java.util.Set;


public class BearsBugsToolsTest {
    static Logger logger = LoggerFactory.getLogger(BearsBugsToolsTest.class);

    @Ignore
    public void testCheckout() {
        //checkout a bug
        String tmpPath = "/home/liumengjiao/Desktop/CI/Benchmark/tmp/";
        BugFixCommit bug = BearsBugsTools.getBugInfo().get(1);
        boolean res = BearsBugsTools.checkBugOfBears(bug.getBugId(), tmpPath);
        assert res;
    }

    @Ignore
    public void testCommitsDiffs() throws Exception {
        //get repository infos of bears-[path2dir, url];
        String tmpPath = "/home/liumengjiao/Desktop/CI/Benchmark/tmp/";//cloned target directory
        List<BugFixCommit> bugs = BearsBugsTools.getBugInfo();//get bears bugs info
        BugFixCommit bug = bugs.get(1);
        //get its repository
        Repository repository = GitTools.getGitRepository(
                tmpPath + bug.getBugId(),
                bug.getBugId(),
                bug.getRepo().getUrl(),
                true
        );
        List<RevCommit> commits = GitTools.createRevsWalkToCurrentHEAD(repository);
        assert commits != null;
        /* Bears has organized each bugs' commits as following:
            Commit #3 contains the version of the program with the bug
            Commit #2 contains the changes in the tests
            Commit #1 contains the version of the program with the human-written patch
            Commit #0 contains the metadata file bears.json, which is a gathering of information collected during the bug reproduction process. It contains information about the bug (e.g. test failure names), the patch (e.g. patch size), and the bug reproduction process (e.g. duration).
            so we use commits[1] as the bug-fixing commit, and commits[3] as the bug-inducing commit.
        */
        String buggy = commits.get(3).getName(), fixed = commits.get(1).getName();
        // find refactoring, in this example, there is no refactoring.
        RefactoringMiner miner = new RefactoringMiner();
        Set<ASTDiff> astDiffs = miner.diffAtCommit(repository, buggy, fixed);
        for (ASTDiff astdiff :astDiffs) {
            System.out.println("Showing astDiff from " + astdiff.getSrcPath() + " " + astdiff.getDstPath());
            List<Action> actions = astdiff.editScript.asList();
            assert actions != null;
            for (Action action :actions) {
                System.out.println("Showing action tree: ");
                System.out.println(action.toString());
            }
        }
        miner.detectBetweenCommits(repository, buggy, fixed, new RefactoringHandler() {
            @Override
            public void handle(String commitId, List<Refactoring> refactorings) {
                if (refactorings.isEmpty()) {
                    System.out.println("There is no refactoring at " + commitId);
                } else {
                    System.out.println("Refactoring at " + commitId);
                    for (Refactoring ref : refactorings) {
                        System.out.println(ref.toString());
                    }
                }
            }
        });
        logger.info("Finished!");
    }

}