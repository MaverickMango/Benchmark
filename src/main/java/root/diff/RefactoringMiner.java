package root.diff;

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
import root.util.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class RefactoringMiner extends GitHistoryRefactoringMinerImpl implements GitAccess {

    static final Logger logger = LoggerFactory.getLogger(GitTool.class);

    public void mapDirectory(Repository repository, String srcCommitId, String dsrCommitId) {
        List<RevCommit> revsWalkOfAll = gitAccess.createRevsWalkOfAll(repository, false);
        int srcIdx = revsWalkOfAll.indexOf(gitAccess.getCommit(repository, srcCommitId));
        int dstIdx = revsWalkOfAll.indexOf(gitAccess.getCommit(repository, dsrCommitId));
        LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<>();
        for (int i = srcIdx; i < dstIdx - 1; i++) {
            //todo: try to resolve mapping process by construct a list of diff
            RevCommit last = revsWalkOfAll.get(i);
            RevCommit next = revsWalkOfAll.get(i + 1);
            System.out.println(last.getParent(0).getName() + " " + next.getName());
            Set<ASTDiff> astDiffs = this.diffAtCommit(repository, srcCommitId);
            for (ASTDiff astDiff :astDiffs) {
                System.out.println(astDiff);
            }
        }
    }

    private void populateDirs(String path, Set<String> repositoryDirectories) {
        String directory = new String(path);
        while(directory.contains("/")) {
            directory = directory.substring(0, directory.lastIndexOf("/"));
            repositoryDirectories.add(directory);
        }
    }

    public Set<ASTDiff> diffBetweenContents(String srcPath, String dstPath, String srcContents, String dstContents) {
        Set<ASTDiff> diffSet = new LinkedHashSet<>();
        Set<String> repositoryDirectoriesBefore = new LinkedHashSet<String>();
        Set<String> repositoryDirectoriesCurrent = new LinkedHashSet<String>();
        Map<String, String> fileContentsBefore = new LinkedHashMap<String, String>();
        fileContentsBefore.put(srcPath, srcContents);
        Map<String, String> fileContentsCurrent = new LinkedHashMap<String, String>();
        fileContentsCurrent.put(dstPath, dstContents);
        populateDirs(srcPath, repositoryDirectoriesBefore);
        populateDirs(dstPath, repositoryDirectoriesCurrent);
        try {
            List<MoveSourceFolderRefactoring> moveSourceFolderRefactorings = processIdenticalFiles(fileContentsBefore, fileContentsCurrent, Collections.emptyMap());
            UMLModel parentUMLModel = createModelForASTDiff(fileContentsBefore, repositoryDirectoriesBefore);
            UMLModel currentUMLModel = createModelForASTDiff(fileContentsCurrent, repositoryDirectoriesCurrent);
            UMLModelDiff modelDiff = parentUMLModel.diff(currentUMLModel);
            ProjectASTDiffer differ = null;
                differ = new ProjectASTDiffer(modelDiff);
            for(ASTDiff diff : differ.getDiffSet()) {
                diff.setSrcContents(fileContentsBefore.get(diff.getSrcPath()));
                diff.setDstContents(fileContentsCurrent.get(diff.getDstPath()));
                diffSet.add(diff);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return diffSet;
    }

    public Set<ASTDiff> diffAtCommit(Repository repository, String oldCommitId, String currentCommitId) {
        Set<ASTDiff> diffSet = new LinkedHashSet<>();
//        String cloneURL = repository.getConfig().getString("remote", "origin", "url");
        File metadataFolder = repository.getDirectory();
//        File projectFolder = metadataFolder.getParentFile();
        RevWalk walk = new RevWalk(repository);
        try {
            RevCommit currentCommit = walk.parseCommit(repository.resolve(currentCommitId));
            RevCommit oldCommit = walk.parseCommit(repository.resolve(oldCommitId));
            Set<String> filePathsBefore = new LinkedHashSet<>();
            Set<String> filePathsCurrent = new LinkedHashSet<>();
            Map<String, String> renamedFilesHint = new HashMap<>();
            fileTreeDiff(repository, oldCommit, currentCommit, filePathsBefore, filePathsCurrent, renamedFilesHint);

            Set<String> repositoryDirectoriesBefore = new LinkedHashSet<>();
            Set<String> repositoryDirectoriesCurrent = new LinkedHashSet<>();
            Map<String, String> fileContentsBefore = new LinkedHashMap<>();
            Map<String, String> fileContentsCurrent = new LinkedHashMap<>();
            if (!filePathsBefore.isEmpty() && !filePathsCurrent.isEmpty()) {
                populateFileContents(repository, oldCommit, filePathsBefore, fileContentsBefore, repositoryDirectoriesBefore);
                populateFileContents(repository, currentCommit, filePathsCurrent, fileContentsCurrent, repositoryDirectoriesCurrent);
//                List<MoveSourceFolderRefactoring> moveSourceFolderRefactorings = processIdenticalFiles(fileContentsBefore, fileContentsCurrent, renamedFilesHint);
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
