package root.util;

import com.github.javaparser.utils.Pair;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import root.analysis.StringFilter;
import root.entities.otherdataset.BugFixCommit;
import root.entities.otherdataset.CommitInfo;
import root.entities.otherdataset.RepositoryInfo;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.refactoringminer.util.GitServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class GitTool extends GitServiceImpl {
    final Logger logger = LoggerFactory.getLogger(GitTool.class);
    final private String githubPrefix = "https://github.com/";
    final private String githubSuffix = ".git";

    public boolean init(String workingDir, int timeoutSecond) {
        CommandSummary cs = new CommandSummary();
        cs.append("git", "init");
        String[] cmd = cs.flat();
        int res = FileUtils.executeCommand(cmd, new File(workingDir).getAbsolutePath(), timeoutSecond
                , Map.of("JAVA_HOME", ConfigurationProperties.getProperty("jdk8")));
        return res == 0;
    }

    public List<Pair<String, String>> getMappedFiles(List<String> mappingFileContents, List<String> beforePaths) {
        List<Pair<String, String>> beforeAfterPairs = new ArrayList<>();
        for (String line :mappingFileContents) {
            String[] split = line.split("\t");
            if (split.length == 2 && split[0].contains("M")) {
                if (beforePaths.contains(split[1]))
                    beforeAfterPairs.add(new Pair<>(split[1], split[1]));
            }
            if (split.length == 3 && split[0].contains("R")) {
                if (beforePaths.contains(split[1]))
                    beforeAfterPairs.add(new Pair<>(split[1], split[2]));
            }
            if (split.length == 2 && split[0].contains("A")) {
                if (beforePaths.contains(split[1]))
                    beforeAfterPairs.add(new Pair<>(null, split[1]));
            }
            if (split.length == 2 && split[0].contains("D")) {
                if (beforePaths.contains(split[1]))
                    beforeAfterPairs.add(new Pair<>(split[1], null));
            }
        }
        return beforeAfterPairs;
    }

    public List<String> getRelevantFiles(List<String> mappingFileContents, List<String> beforePaths) {
        List<String> afterPaths = new ArrayList<>();
        for (String line :mappingFileContents) {
            String[] split = line.split("\t");
            if (split.length == 2 && split[0].contains("M")) {
                if (beforePaths.contains(split[1]))
                    afterPaths.add(split[1]);
            }
            if (split.length == 3 && split[0].contains("R")) {
                if (beforePaths.contains(split[1]))
                    afterPaths.add(split[2]);
            }
        }
        return afterPaths;
    }

    public String[] getF2i(String mappingFile, String packageName) {
        List<String> f2i = FileUtils.readEachLine(mappingFile);
        String filePath = packageName.replaceAll("[.]", File.separator);
        List<String> collect = f2i.stream().filter(s -> s.contains(filePath)).collect(Collectors.toList());
        if (!collect.isEmpty()) {
            List<String> split = new ArrayList<>(List.of(collect.get(0).split("\t")));
            if (split.size() == 2) {
                split.add(split.get(1));
            }
            return split.toArray(new String[0]);
        }
        return null;
    }

    public List<List<String>> getF2i(String mappingFile, List<String> methods) {
        List<String> f2i = FileUtils.readEachLine(mappingFile);
        List<List<String>> f2is = new ArrayList<>();
        for (String packageName: methods) {
            String filePath = packageName.split("::")[0].replaceAll("[.]", File.separator);
            List<String> collect = f2i.stream().filter(s -> s.contains(filePath) && s.endsWith(".java")).collect(Collectors.toList());
            if (!collect.isEmpty()) {
                List<String> split = new ArrayList<>(List.of(collect.get(0).split("\t")));
                if (split.size() == 2) {
                    split.add(split.get(1));
                }
                if (packageName.split("::").length > 1) {
                    split.add(packageName.split("::")[1]);
                }
                f2is.add(split);
            }
        }
        return f2is;
    }


    public boolean checkoutf(String workingDir, String commitId){
        logger.debug("Forcibly Checking out {} {} ...", workingDir, commitId);
        CommandSummary cs = new CommandSummary();
        cs.append("git", "checkout");
        cs.append("--force", commitId);
        String[] cmd = cs.flat();
        int res = FileUtils.executeCommand(cmd, new File(workingDir).getAbsolutePath(), 300
                , Map.of("JAVA_HOME", ConfigurationProperties.getProperty("jdk8")));
        return res == 0;
    }

    /**
     * get diff status of files between two commits
     * @param workingDIr a git working directory
     * @param srcCommitId source commit id
     * @param dsrCommitId destination commit id
     * @param outputFile target file to save result, NOTE! make sure the file path exists!
     */
    public void getFileStatDiffBetweenCommits(String workingDIr, String srcCommitId, String dsrCommitId, String outputFile) {
        CommandSummary cs = new CommandSummary();
        cs.append("/bin/bash", "-c");
        cs.append("cd " + workingDIr + " && git diff --name-status " + srcCommitId + " " + dsrCommitId + " --", null);
        String[] cmd = cs.flat();
        List<String> res = FileUtils.executeCommand(cmd);
        FileUtils.writeToFile(FileUtils.getStrOfIterable(res, "\n").toString(), outputFile, false);
    }

    public List<String> getFileStatDiffBetweenCommits(String workingDIr, String srcCommitId, String dsrCommitId) {
        CommandSummary cs = new CommandSummary();
        cs.append("/bin/bash", "-c");
        cs.append("cd " + workingDIr + " && git diff --name-status " + srcCommitId + " " + dsrCommitId + " --", null);
        String[] cmd = cs.flat();
        List<String> res = FileUtils.executeCommand(cmd);
        return res;
    }

    public String getRepositoryURL(String repoName) {
        return githubPrefix + repoName + githubSuffix;
    }

    /**
     * get a git repository from an existing ".git" file.
     * @param file2Git file to an existing ".git" file
     * @return Repository
     */
    public Repository getGitRepository(File file2Git) throws IOException {
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
    public Repository getGitRepository(String path2dir, String url) throws Exception {
        return this.cloneIfNotExists(path2dir, url);
    }

    /**
     * create a list of commit of the repository.
     * @param repository a git repository.
     * @param reverse 'true' to order the commit list from initial one to the latest, 'false' gives same order as the 'git log'.
     * @return a list of RevCommit.
     */
    public List<RevCommit> createRevsWalkOfAll(Repository repository, boolean reverse){
        try (Git git = new Git(repository)) {
            List<RevCommit> revCommits = StreamSupport.stream(git.log().call()
                            .spliterator(), false)
                    .collect(Collectors.toList());//latest commit to the first one.
            if (reverse)
                Collections.reverse(revCommits);//from first to the end.
            return revCommits;
        } catch (Exception e) {
            logger.error("Repository " + repository.getIdentifier() + " is not a Git repository or ERRORS occurred when walk its commits: " + e.getMessage());
        }
        return null;
    }

    public String getFileAtCommit(Repository repository, String commit, String filePath) {
        RevCommit commit1 = getCommit(repository, commit);
        RevTree tree = commit1.getTree();
        try(TreeWalk treeWalk = new TreeWalk(repository)) {
            treeWalk.addTree(tree);
            treeWalk.setRecursive(true);
            treeWalk.setFilter(PathFilter.create(filePath));
            if (treeWalk.next()) {
                ObjectId objectId = treeWalk.getObjectId(0);
                ObjectLoader loader = repository.open(objectId);
                String fileContent = new String(loader.getBytes());
                return fileContent;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * get a specific commit with a commitId.
     * @param repository a git repository
     * @param commitId a string of commit name
     * @return a instance of 'commitId'
     */
    public RevCommit getCommit(Repository repository, String commitId) {
        RevWalk walk = new RevWalk(repository);
        try {
            return walk.parseCommit(repository.resolve(commitId));
        } catch (Exception e) {
            logger.error("Commit " + commitId + " can't be resolved in repository " + repository.getIdentifier());
        }
        return null;
    }

    /**
     * get the head name of a specific repository.
     * @param repository a git repository
     * @return a name of its head
     */
    public String getCurrentHeadCommit(Repository repository){
        RevWalk walker = new RevWalk(repository);
        try {
            RevCommit commit = walker.parseCommit(repository.resolve("HEAD"));
            return commit.getName();
        } catch (Exception e) {
            logger.error("Maybe there is not HEAD in this repository...: " + e.getMessage());
        }
        return null;
    }

    public String getNextCommit(Repository repository, String commitId, boolean reverse){
        try (Git git = new Git(repository)) {
            List<RevCommit> revCommits = StreamSupport.stream(git.log().call()
                            .spliterator(), false)
                    .collect(Collectors.toList());//latest commit to the first one.
            if (reverse)
                Collections.reverse(revCommits);//from first to the end.
            List<String> collect = revCommits.stream().map(AnyObjectId::getName).collect(Collectors.toList());
            int i = collect.indexOf(commitId);
            i = i + 1 >= collect.size() ? i - 1 : i;
            return collect.get(i + 1);
        } catch (Exception e) {
            logger.error("Repository " + repository.getIdentifier() + " is not a Git repository or ERRORS occurred when walk its commits: " + e.getMessage());
        }
        return null;
    }

    public String getInitialCommit(Repository repository){
        try (Git git = new Git(repository)) {
            List<RevCommit> revCommits = StreamSupport.stream(git.log().call()
                            .spliterator(), false)
                    .collect(Collectors.toList());//latest commit to the first one.
            Collections.reverse(revCommits);//from first to the end.
            return revCommits.get(0).getName();
        } catch (Exception e) {
            logger.error("Repository " + repository.getIdentifier() + " is not a Git repository or ERRORS occurred when walk its commits: " + e.getMessage());
        }
        return null;
    }

    public BugFixCommit getBugFixCommit(String bugName, String bugId, Repository repository, String inducingCommit, String fixedCommit) {
        BugFixCommit bugFixCommit = new BugFixCommit(bugName, bugId);
        bugFixCommit.setRepo(getRepositoryInfo(repository));
        RevCommit inducing = getCommit(repository, inducingCommit);
//        assert inducing != null && inducing.getParentCount() != 0;
        RevCommit before = getCommit(repository, inducing.getParent(0).getName());
        bugFixCommit.setInducingCommit(getCommitInfo(repository, inducing));
//        assert before != null;
        bugFixCommit.setOriginalCommit(getCommitInfo(repository, before));
        RevCommit fixed = getCommit(repository, fixedCommit);
//        assert fixed != null && fixed.getParentCount() != 0;
        before = getCommit(repository, fixed.getParent(0).getName());
        bugFixCommit.setFixedCommit(getCommitInfo(repository, fixed));
//        assert before != null;
        bugFixCommit.setBuggyCommit(getCommitInfo(repository, before));
        return bugFixCommit;
    }

    public RepositoryInfo getRepositoryInfo(Repository repository) {
        RepositoryInfo repositoryInfo = new RepositoryInfo(repository.getIdentifier());
        repository.getRemoteNames();
        return repositoryInfo;
    }

    public CommitInfo getCommitInfo(Repository repository, RevCommit commit) {
        CommitInfo commitInfo = new CommitInfo();
        commitInfo.setSha(commit.getName());
        commitInfo.setRepoName(repository.getIdentifier());
        commitInfo.setFullMessage(commit.getFullMessage());
        try (Git git = new Git(repository)) {
            List<Ref> refs = git.branchList().setContains(commit.getName()).call();
            assert !refs.isEmpty();
            commitInfo.setBranchName(refs.get(0).getName());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return commitInfo;
    }

    public CommitInfo getCommitInfo(Repository repository, String commitSha) {
        RevCommit commit = getCommit(repository, commitSha);
        CommitInfo commitInfo = new CommitInfo();
        commitInfo.setSha(commit.getName());
        commitInfo.setRepoName(repository.getIdentifier());
        commitInfo.setFullMessage(commit.getFullMessage());
        try (Git git = new Git(repository)) {
            List<Ref> refs = git.branchList().setContains(commit.getName()).call();
            assert !refs.isEmpty();
            commitInfo.setBranchName(refs.get(0).getName());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return commitInfo;
    }

    public String diff(Repository repository, String srcCommit, String dstCommit) {
        RevCommit oldCommit = getCommit(repository, srcCommit);
        RevCommit newCommit = getCommit(repository, dstCommit);
        if (oldCommit != null && newCommit != null) {
            try (OutputStream outputStream = new ByteArrayOutputStream()) {
                DiffFormatter diffFormatter = new DiffFormatter(outputStream);
                diffFormatter.setRepository(repository);
                List<DiffEntry> scan = diffFormatter.scan(oldCommit, newCommit);
                for (DiffEntry entry : scan) {
                    diffFormatter.format(diffFormatter.toFileHeader(entry));
                }
                return outputStream.toString();
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        return null;
    }

    public String diff(Repository repository, String dstCommit) {
        RevCommit newCommit = getCommit(repository, dstCommit);
        if (newCommit != null && newCommit.getParentCount() > 0) {
            RevCommit oldCommit = getCommit(repository, newCommit.getParent(0).getName());
            if (oldCommit != null) {
                try (OutputStream outputStream = new ByteArrayOutputStream()) {
                    DiffFormatter diffFormatter = new DiffFormatter(outputStream);
                    diffFormatter.setRepository(repository);
                    List<DiffEntry> scan = diffFormatter.scan(oldCommit, newCommit);
                    for (DiffEntry entry : scan) {
                        diffFormatter.format(diffFormatter.toFileHeader(entry));
                    }
                    return outputStream.toString();
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        }
        return null;
    }

    public String diffWithFilter(Repository repository, String srcCommit, String dstCommit, StringFilter filter) {
        RevCommit oldCommit = getCommit(repository, srcCommit);
        RevCommit newCommit = getCommit(repository, dstCommit);
        if (oldCommit != null && newCommit != null) {
            try (OutputStream outputStream = new ByteArrayOutputStream()) {
                DiffFormatter diffFormatter = new DiffFormatter(outputStream);
                diffFormatter.setRepository(repository);
                List<DiffEntry> scan = diffFormatter.scan(oldCommit, newCommit);
                for (DiffEntry entry : scan) {
                    if (filter.canMatch(entry.getNewPath())) {
                        continue;
                    }
                    diffFormatter.format(diffFormatter.toFileHeader(entry));
                }
                return outputStream.toString();
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        return null;
    }
}
