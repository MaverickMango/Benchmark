package analysis;

import gr.uom.java.xmi.UMLModel;
import gr.uom.java.xmi.diff.MoveSourceFolderRefactoring;
import gr.uom.java.xmi.diff.UMLModelDiff;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.refactoringminer.api.RefactoringMinerTimedOutException;
import org.refactoringminer.astDiff.actions.ASTDiff;
import org.refactoringminer.astDiff.matchers.ProjectASTDiffer;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.GitTools;

import java.io.File;
import java.util.*;

public class RefactoringMiner extends GitHistoryRefactoringMinerImpl {

    static final Logger logger = LoggerFactory.getLogger(GitTools.class);

    public Set<ASTDiff> diffAtCommit(Repository repository, String oldCommitId, String currentCommitId) {
        Set<ASTDiff> diffSet = new LinkedHashSet<>();
        String cloneURL = repository.getConfig().getString("remote", "origin", "url");
        File metadataFolder = repository.getDirectory();
        File projectFolder = metadataFolder.getParentFile();
        RevWalk walk = new RevWalk(repository);
        try {
            RevCommit currentCommit = walk.parseCommit(repository.resolve(currentCommitId));
            RevCommit oldCommit = walk.parseCommit(repository.resolve(oldCommitId));
            walk.parseCommit(currentCommit.getParent(0));
            Set<String> filePathsBefore = new LinkedHashSet<>();
            Set<String> filePathsCurrent = new LinkedHashSet<>();
            Map<String, String> renamedFilesHint = new HashMap<>();
            fileTreeDiff(repository, oldCommit, currentCommit, filePathsBefore, filePathsCurrent, renamedFilesHint);

            Set<String> repositoryDirectoriesBefore = new LinkedHashSet<>();
            Set<String> repositoryDirectoriesCurrent = new LinkedHashSet<>();
            Map<String, String> fileContentsBefore = new LinkedHashMap<>();
            Map<String, String> fileContentsCurrent = new LinkedHashMap<>();
            // If no java files changed, there is no refactoring. Also, if there are
            // only ADD's or only REMOVE's there is no refactoring
            if (!filePathsBefore.isEmpty() && !filePathsCurrent.isEmpty() && currentCommit.getParentCount() > 0) {
                populateFileContents(repository, oldCommit, filePathsBefore, fileContentsBefore, repositoryDirectoriesBefore);
                populateFileContents(repository, currentCommit, filePathsCurrent, fileContentsCurrent, repositoryDirectoriesCurrent);
                List<MoveSourceFolderRefactoring> moveSourceFolderRefactorings = processIdenticalFiles(fileContentsBefore, fileContentsCurrent, renamedFilesHint);
                UMLModel oldUMLModel = createModelForASTDiff(fileContentsBefore, repositoryDirectoriesBefore);
                UMLModel currentUMLModel = createModelForASTDiff(fileContentsCurrent, repositoryDirectoriesCurrent);
                UMLModelDiff modelDiff = oldUMLModel.diff(currentUMLModel);
                ProjectASTDiffer differ = new ProjectASTDiffer(modelDiff);
                for(ASTDiff diff : differ.getDiffSet()) {
                    diff.setSrcContents(fileContentsBefore.get(diff.getSrcPath()));
                    diff.setDstContents(fileContentsCurrent.get(diff.getDstPath()));
                    diffSet.add(diff);
                }
            }
        } catch (MissingObjectException moe) {
            moe.printStackTrace();
        } catch (RefactoringMinerTimedOutException e) {
            logger.warn(String.format("Ignored revision %s due to timeout", currentCommitId), e);
        } catch (Exception e) {
            logger.warn(String.format("Ignored revision %s due to error", currentCommitId), e);
        } finally {
            walk.close();
            walk.dispose();
        }
        return diffSet;
    }

    public void fileTreeDiff(Repository repository, RevCommit oldCommit, RevCommit currentCommit,
                             Set<String> javaFilesBefore, Set<String> javaFilesCurrent, Map<String, String> renamedFilesHint) throws Exception {
        if (currentCommit.getParentCount() > 0) {
            ObjectId oldTree = oldCommit.getTree();
            ObjectId newTree = currentCommit.getTree();
            final TreeWalk tw = new TreeWalk(repository);
            tw.setRecursive(true);
            tw.addTree(oldTree);
            tw.addTree(newTree);

            final RenameDetector rd = new RenameDetector(repository);
            rd.setRenameScore(55);
            rd.addAll(DiffEntry.scan(tw));

            for (DiffEntry diff : rd.compute(tw.getObjectReader(), null)) {
                DiffEntry.ChangeType changeType = diff.getChangeType();
                String oldPath = diff.getOldPath();
                String newPath = diff.getNewPath();
                if (changeType != DiffEntry.ChangeType.ADD) {
                    if (isJavafile(oldPath)) {
                        javaFilesBefore.add(oldPath);
                    }
                }
                if (changeType != DiffEntry.ChangeType.DELETE) {
                    if (isJavafile(newPath)) {
                        javaFilesCurrent.add(newPath);
                    }
                }
                if (changeType == DiffEntry.ChangeType.RENAME && diff.getScore() >= rd.getRenameScore()) {
                    if (isJavafile(oldPath) && isJavafile(newPath)) {
                        renamedFilesHint.put(oldPath, newPath);
                    }
                }
            }
        }
    }

    private boolean isJavafile(String path) {
        return path.endsWith(".java");
    }
}
