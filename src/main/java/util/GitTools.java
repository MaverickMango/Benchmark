package util;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.refactoringminer.api.GitService;
import org.refactoringminer.util.GitServiceImpl;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class GitTools extends GitServiceImpl{

    /**
     * get a git repository from an existing ".git" file.
     * @param file2Git file to an existing ".git" file
     * @return Repository
     */
    public static Repository getGitRepository(File file2Git) throws IOException {
        FileRepositoryBuilder fileRepositoryBuilder = new FileRepositoryBuilder();
        return fileRepositoryBuilder.setGitDir(file2Git)
                .readEnvironment()
                .findGitDir()
                .build();
    }

    /**
     * get a git repository whether it exists.
     * @param path2dir directory path to save a cloned repository
     * @param url a git repository clone url
     * @return Repository
     */
    public static Repository getGitRepository(String path2dir, String url) throws Exception {
        GitService gitService = new GitServiceImpl();
        return gitService.cloneIfNotExists(path2dir, url);
    }

    public static Repository getGitRepository(String path2dir, String bugId, String url, boolean isBearsBug) throws Exception {
        if (isBearsBug) {
            BearsBugsTools.checkBugOfBears(bugId, path2dir);
        }
        return getGitRepository(path2dir, url);
    }

    public static List<RevCommit> createRevsWalkToCurrentHEAD(Repository repository){
        try (Git git = new Git(repository)) {
            List<RevCommit> revCommits = StreamSupport.stream(git.log().call()
                            .spliterator(), false)
                    .collect(Collectors.toList());//last commit to the first one.
//            Collections.reverse(revCommits);//valid it to make commit from first to the end.
            return revCommits;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
