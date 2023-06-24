package root.attempt;

import gui.webdiff.WebDiff;
import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.astDiff.actions.ASTDiff;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import root.analysis.RefactoringMiner;
import root.util.GitAccess;

import java.util.List;
import java.util.Set;

public class ViewDiffTest implements GitAccess {
    static Logger logger = LoggerFactory.getLogger(ViewDiffTest.class);

    public static void main(String[] args) throws Exception {
        RefactoringMiner miner = new RefactoringMiner();
//        File dir1 = new File("/home/liumengjiao/Desktop/CI/Benchmark/tmp/Lang_4_buggy");
//        File dir2 = new File("/home/liumengjiao/Desktop/CI/Benchmark/tmp/Lang_4_inducing");
//        Set<ASTDiff> astDiffs = miner.diffAtDirectories(dir1, dir2);
        Repository repo = gitAccess.getGitRepository(
                "tmp/bugs/Closure_48_buggy",
                ""
        );
//        miner.detectAtCommit(repo, "fb47b96ab635d7cc6e9edefdddc46f1baf63b117", new RefactoringHandler() {
//            @Override
//            public void handle(String commitId, List<Refactoring> refactorings) {
//                System.out.println("Refactoring at " + commitId);
//                for (Refactoring ref :refactorings) {
//                    System.out.println(ref.toString());
//                }
//            }
//        });
        //lang 4 :diff with parent commit efa5ce262008d906b119c3ceb45a346952d7a791 buggy
        Set<ASTDiff> astDiffs = miner.diffAtCommit(repo, "e1feab189248bba77e40434f2bf5185cad6a20e7", "41abf3b75893403d9e21c33d06e8dc233b5cf657");
        new WebDiff(astDiffs).run();
        logger.debug("Hello world");
    }
}
