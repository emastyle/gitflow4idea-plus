package gitflow;

import com.intellij.openapi.project.Project;
import git4idea.GitUtil;
import git4idea.repo.GitRepository;

import java.util.List;

/**
 * This class maps repos to their corresponding branch utils
 * Note that the static class is used across projects
 */

public class GitflowBranchUtilManager {
    private static final RepoScopedCache<GitflowBranchUtil> repoBranchUtilMap = new RepoScopedCache<>();

    static public GitflowBranchUtil getBranchUtil(GitRepository repo){
        if (repo != null) {
            return repoBranchUtilMap.get(repo.getPresentableUrl());
        } else {
            return null;
        }
    }

    static public void setupBranchUtil(Project project, GitRepository repo){
        if (project.isDisposed() || repo.isDisposed()) {
            return;
        }
        GitflowBranchUtil gitflowBranchUtil = new GitflowBranchUtil(project, repo);
        repoBranchUtilMap.put(repo, repo.getPresentableUrl(), gitflowBranchUtil);
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
