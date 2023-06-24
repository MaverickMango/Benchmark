package attempt;

import com.github.gumtreediff.actions.EditScript;
import com.github.gumtreediff.actions.EditScriptGenerator;
import com.github.gumtreediff.actions.SimplifiedChawatheScriptGenerator;
import com.github.gumtreediff.actions.TreeClassifier;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.Tree;
import org.eclipse.jgit.lib.Repository;
import org.junit.Ignore;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.astDiff.actions.ASTDiff;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import root.util.GitAccess;

import java.util.List;
import java.util.Set;

public class RefactorMinerTest implements GitAccess {
    
    @Ignore
    public void test() throws Exception {
        GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
//        File dir1 = new File("D:\\IdeaProjects\\BugFixCommits\\tmp1");
//        File dir2 = new File("D:\\IdeaProjects\\BugFixCommits\\tmp");
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
                "tmp/refactoring-toy-example",
                "https://github.com/danilofes/refactoring-toy-example.git"
        );
        miner.detectAll(repo, "master", new RefactoringHandler() {
            @Override
            public void handle(String commitId, List<Refactoring> refactorings) {
                System.out.println("Refactoring at " + commitId);
                for (Refactoring ref :refactorings) {
                    System.out.println(ref.toString());
                }
            }
        });
        miner.detectBetweenCommits(repo, "sha1", "sha2", new RefactoringHandler() {
            @Override
            public void handle(String commitId, List<Refactoring> refactorings) {
                System.out.println("Refactoring at " + commitId);
                for (Refactoring ref :refactorings) {
                    System.out.println(ref.toString());
                }
            }
        });
        String commit = "d4bce13a443cf12da40a77c16c1e591f4f985b47";
        miner.detectAtCommit(repo, commit, new RefactoringHandler() {
            @Override
            public void handle(String commitId, List<Refactoring> refactorings) {
                System.out.println("Refactoring at " + commitId);
                for (Refactoring ref :refactorings) {
                    System.out.println(ref.toString());
                }
            }
        });
        Set<ASTDiff> astDiffs = new GitHistoryRefactoringMinerImpl().diffAtCommit(repo, commit);
//        new WebDiff(astDiffs).run();
        for (ASTDiff astDiff :astDiffs) {
            String srcPath = astDiff.getSrcPath();
            String dstPath = astDiff.getDstPath();
            String dstContents = astDiff.getDstContents();
            String srcContents = astDiff.getSrcContents();
            TreeClassifier rootNodesClassifier = astDiff.createRootNodesClassifier();
            Set<Tree> insertedDsts = rootNodesClassifier.getMovedSrcs();
            Set<Tree> deletedSrcs = rootNodesClassifier.getMovedDsts();
            for (Tree tree :insertedDsts) {
                for (Tree tree1 :deletedSrcs) {
                    String label = tree.getLabel();
                    Matcher defaultMatcher = Matchers.getInstance().getMatcher(); // retrieves the default matcher
                    MappingStore mappings = defaultMatcher.match(tree, tree1); // computes the mappings between the trees
                    EditScriptGenerator editScriptGenerator = new SimplifiedChawatheScriptGenerator(); // instantiates the simplified Chawathe script generator
                    EditScript actions = editScriptGenerator.computeActions(mappings); // computes the edit script
                    System.out.println(actions.asList());
                }
            }
        }
        System.out.println( "Finished!" );
    }
}