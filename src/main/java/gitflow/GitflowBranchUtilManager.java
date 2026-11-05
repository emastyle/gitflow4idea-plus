package gitflow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import git4idea.GitUtil;
import git4idea.repo.GitRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class maps repos to their corresponding branch utils
 * Note that the static class is used across projects
 */

public class GitflowBranchUtilManager {
    private static final Map<String, GitflowBranchUtil> repoBranchUtilMap = new ConcurrentHashMap<>();

    static public GitflowBranchUtil getBranchUtil(GitRepository repo){
        if (repo != null) {
            return repoBranchUtilMap.get(repo.getPresentableUrl());
        } else {
            return null;
        }
    }

    static public void setupBranchUtil(Project project, GitRepository repo){
        GitflowBranchUtil gitflowBranchUtil = new GitflowBranchUtil(project, repo);
        repoBranchUtilMap.put(repo.getPresentableUrl(), gitflowBranchUtil);
        // clean up
        Disposer.register(repo, () -> repoBranchUtilMap.remove(repo.getPresentableUrl()));
    }

    /**
     * Repopulates the branchUtils for each repo
     * @param proj
     */
    static public void update(Project proj){
        List<GitRepository> gitRepositories = GitUtil.getRepositoryManager(proj).getRepositories();
        for (GitRepository repo : gitRepositories) {
            GitflowBranchUtilManager.setupBranchUtil(proj, repo);
        }
    }
}
