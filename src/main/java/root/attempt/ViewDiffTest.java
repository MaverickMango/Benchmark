package root.attempt;

import gui.webdiff.WebDiff;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.astDiff.actions.ASTDiff;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.analysis.RefactoringMiner;
import root.util.GitAccess;

import java.io.File;
import java.util.List;
import java.util.Set;

public class ViewDiffTest implements GitAccess {
    static Logger logger = LoggerFactory.getLogger(ViewDiffTest.class);

    public static void main(String[] args) throws Exception {
        RefactoringMiner miner = new RefactoringMiner();
        File dir1 = new File("data/changes/Closure_48/properties/modified_classes/original/");
        File dir2 = new File("data/changes/Closure_48/properties/modified_classes/inducing/");
        Set<ASTDiff> astDiffs = miner.diffAtDirectories(dir1, dir2);
        miner.detectAtDirectories(dir1, dir2, new RefactoringHandler() {
            @Override
            public void handle(String commitId, List<Refactoring> refactorings) {
                System.out.println("Refactoring at " + commitId);
                for (Refactoring ref :refactorings) {
                    System.out.println(ref.toString());
                }
            }
        });
//        Repository repo = gitAccess.getGitRepository(
//                "/home/liumengjiao/Desktop/CI/bugswarm/bugs/okio",
//                ""
//        );
////        miner.detectAtCommit(repo, "fb47b96ab635d7cc6e9edefdddc46f1baf63b117", new RefactoringHandler() {
////            @Override
////            public void handle(String commitId, List<Refactoring> refactorings) {
////                System.out.println("Refactoring at " + commitId);
////                for (Refactoring ref :refactorings) {
////                    System.out.println(ref.toString());
////                }
////            }
////        });
//        List<RevCommit> revsWalkOfAll = gitAccess.createRevsWalkOfAll(repo, false);
//        //lang 4 :diff with parent commit efa5ce262008d906b119c3ceb45a346952d7a791 buggy
//        Set<ASTDiff> astDiffs = miner.diffAtCommit(repo, "91bca7d85645093fd031d7179a0dd44ffe85f47a","edac79dad83beb61c57b78af2d098fea88191350");//, "edac79dad83beb61c57b78af2d098fea88191350");
        new WebDiff(astDiffs).run();
        logger.debug("Hello world");
    }
}
