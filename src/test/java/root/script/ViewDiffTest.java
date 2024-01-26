package root.script;

import gui.webdiff.WebDiff;
import org.eclipse.jgit.lib.Repository;
import org.junit.jupiter.api.Test;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.astDiff.actions.ASTDiff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.diff.RefactoringMiner;
import root.util.GitAccess;

import java.util.List;
import java.util.Set;

public class ViewDiffTest implements GitAccess {
    static Logger logger = LoggerFactory.getLogger(ViewDiffTest.class);

    @Test
    public void test() throws Exception {
        RefactoringMiner miner = new RefactoringMiner();
//        File dir1 = new File("data/changes/Closure_48/properties/modified_classes/original/");
//        File dir2 = new File("data/changes/Closure_48/properties/modified_classes/inducing/");
//        Set<ASTDiff> astDiffs = miner.diffAtDirectories(dir1, dir2);
//        miner.detectAtDirectories(dir1, dir2, new RefactoringHandler() {
//            @Override
//            public void handle(String commitId, List<Refactoring> refactorings) {
//                System.out.println("Refactoring at " + commitId);
//                for (Refactoring ref :refactorings) {
//                    System.out.println(ref.toString());
//                }
//            }
//        });
        Repository repo = gitAccess.getGitRepository(
                "/home/liumengjiao/Desktop/CI/bugs/Math_53_bug",
                ""
        );
        miner.detectAtCommit(repo, "6ef3b2932f4ca9219e41784bb0cd229dcabcdb11", new RefactoringHandler() {
            @Override
            public void handle(String commitId, List<Refactoring> refactorings) {
                System.out.println("Refactoring at " + commitId);
                for (Refactoring ref :refactorings) {
                    System.out.println(ref.toString());
                }
            }
        });
//        List<RevCommit> revsWalkOfAll = gitAccess.createRevsWalkOfAll(repo, false);
        //lang 4 :diff with parent commit efa5ce262008d906b119c3ceb45a346952d7a791 buggy
        Set<ASTDiff> astDiffs = miner.diffAtCommit(repo, "bb22a04fd7e1ee5af3c8ec715cc4c1952d4bbeb7","6ef3b2932f4ca9219e41784bb0cd229dcabcdb11");//, "edac79dad83beb61c57b78af2d098fea88191350");
        new WebDiff(astDiffs).run();
        logger.debug("Hello world");
    }
}
