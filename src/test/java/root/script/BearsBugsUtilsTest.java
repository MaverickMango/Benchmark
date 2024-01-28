package root.script;

import root.diff.RefactoringMiner;
import root.entities.otherdataset.BugFixCommit;
import com.github.gumtreediff.actions.model.Action;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Test;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.astDiff.actions.ASTDiff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.util.BearsBugsUtils;
import root.util.GitAccess;

import java.util.List;
import java.util.Set;


public class BearsBugsUtilsTest implements GitAccess {
    Logger logger = LoggerFactory.getLogger(BearsBugsUtilsTest.class);

    @Test
    public void test() {
        String tmpPath = "/home/liumengjiao/Desktop/CI/Benchmark/data/";//cloned target directory
        List<BugFixCommit> bugs = BearsBugsUtils.getBugInfo();//get bears bugs info
        BugFixCommit bug = bugs.get(1);
        //get its repository
        Repository repository = BearsBugsUtils.getGitRepository(
                tmpPath + bug.getBugId(),
                bug.getBugId()
        );
    }

    @Test
    public void testCommitsDiffs() throws Exception {
        //get repository infos of bears-[path2dir, url];
        String tmpPath = "/home/liumengjiao/Desktop/CI/Benchmark/data/";//cloned target directory
        List<BugFixCommit> bugs = BearsBugsUtils.getBugInfo();//get bears bugs info
        BugFixCommit bug = bugs.get(1);
        //get its repository
        Repository repository = BearsBugsUtils.getGitRepository(
                tmpPath + bug.getBugId(),
                bug.getBugId()
        );
        assert repository != null;
        List<RevCommit> commits = gitAccess.createRevsWalkOfAll(repository, false);
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
        logger.debug("Finished!");
    }

}