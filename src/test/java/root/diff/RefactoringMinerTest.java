package root.diff;

import com.github.gumtreediff.actions.EditScript;
import com.github.gumtreediff.actions.EditScriptGenerator;
import com.github.gumtreediff.actions.SimplifiedChawatheScriptGenerator;
import com.github.gumtreediff.actions.TreeClassifier;
import com.github.gumtreediff.matchers.Mapping;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.Tree;
import org.eclipse.jgit.lib.Repository;
import org.junit.Ignore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
//import org.refactoringminer.RefactoringMiner;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.astDiff.actions.ASTDiff;
import org.refactoringminer.astDiff.matchers.ExtendedMultiMappingStore;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import root.util.FileUtils;
import root.util.GitAccess;

import java.io.File;
import java.util.List;
import java.util.Set;

public class RefactoringMinerTest implements GitAccess {

//    @Test
//    public void test4diff() {
//        String srcPath = "/home/liumengjiao/Desktop/CI/Benchmark/src/test/java/res1/FileRead.java";
//        String dstPath = "/home/liumengjiao/Desktop/CI/Benchmark/src/test/java/res2/examples.FileRead2.java";//not in a git repository?
//        RefactoringMiner refactoringMiner = new RefactoringMiner();
//        Set<ASTDiff> astDiffs = refactoringMiner.diffAtDirectories(new File(srcPath), new File(dstPath));
//        Assertions.assertFalse(astDiffs.isEmpty());
//    }
    
    @Test
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
                "data/refactoring-toy-example",
                "https://github.com/danilofes/refactoring-toy-example.git"
        );
//        miner.detectAll(repo, "master", new RefactoringHandler() {
//            @Override
//            public void handle(String commitId, List<Refactoring> refactorings) {
//                System.out.println("Refactoring at " + commitId);
//                for (Refactoring ref :refactorings) {
//                    System.out.println(ref.toString());
//                }
//            }
//        });
//        miner.detectBetweenCommits(repo, "sha1", "sha2", new RefactoringHandler() {
//            @Override
//            public void handle(String commitId, List<Refactoring> refactorings) {
//                System.out.println("Refactoring at " + commitId);
//                for (Refactoring ref :refactorings) {
//                    System.out.println(ref.toString());
//                }
//            }
//        });
        String commit = gitAccess.getCurrentHeadCommit(repo);
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
            //for each changed files, mapping the line number with methods.
            boolean srcFlag = srcPath.contains("test") || srcPath.endsWith("Test.java");
            boolean dstFlag = dstPath.contains("test") || dstPath.endsWith("Test.java");
            boolean flag = srcFlag || dstFlag;
            if (flag)// filter changes about test
                break;
            ExtendedMultiMappingStore multiMappings = astDiff.getMultiMappings();
            for (Mapping mapping :multiMappings.getMappings()) {
                Tree first = mapping.first;
                Tree second = mapping.second;
                int pos = first.getPos();
            }
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