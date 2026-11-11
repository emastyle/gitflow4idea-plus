package gitflow;

import java.util.concurrent.ExecutionException;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitLocalBranch;
import git4idea.config.GitConfigUtil;
import git4idea.repo.GitRepository;
import gitflow.ui.NotifyUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 *
 * @author Opher Vishnia / opherv.com / opherv@gmail.com
 */

public class GitflowConfigUtil {

    // Old git-flow AVH config keys
    public static final String BRANCH_MASTER = "gitflow.branch.master";
    public static final String BRANCH_DEVELOP = "gitflow.branch.develop";
    public static final String PREFIX_FEATURE = "gitflow.prefix.feature";
    public static final String PREFIX_RELEASE = "gitflow.prefix.release";
    public static final String PREFIX_HOTFIX = "gitflow.prefix.hotfix";
    public static final String PREFIX_BUGFIX = "gitflow.prefix.bugfix";
    public static final String PREFIX_SUPPORT = "gitflow.prefix.support";
    public static final String PREFIX_VERSIONTAG = "gitflow.prefix.versiontag";

    // New git-flow-next config keys
    public static final String BRANCH_FEATURE_PREFIX = "gitflow.branch.feature.prefix";
    public static final String BRANCH_RELEASE_PREFIX = "gitflow.branch.release.prefix";
    public static final String BRANCH_HOTFIX_PREFIX = "gitflow.branch.hotfix.prefix";
    public static final String BRANCH_BUGFIX_PREFIX = "gitflow.branch.bugfix.prefix";
    public static final String BRANCH_SUPPORT_PREFIX = "gitflow.branch.support.prefix";

    private static final Logger LOG = Logger.getInstance(GitflowConfigUtil.class);
    private static final String GITFLOW_KEYS = "^gitflow\\.";

    private static final RepoScopedCache<GitflowConfigUtil> gitflowConfigUtilMap = new RepoScopedCache<>();

    Project project;
    GitRepository repo;
    public volatile String masterBranch;
    public volatile String developBranch;
    public volatile String featurePrefix;
    public volatile String releasePrefix;
    public volatile String hotfixPrefix;
    public volatile String bugfixPrefix;
    public volatile String supportPrefix;
    public volatile String versiontagPrefix;

    public static synchronized GitflowConfigUtil getInstance(@NotNull Project project_, @NotNull GitRepository repo_)
    {
        String key = project_.getLocationHash() + "|" + repo_.getPresentableUrl();
        GitflowConfigUtil instance = gitflowConfigUtilMap.get(key);
        if (instance == null || instance.repo != repo_) {
            instance = new GitflowConfigUtil(project_, repo_);
            gitflowConfigUtilMap.put(repo_, key, instance);
        }
        return instance;
    }

    GitflowConfigUtil(Project project_, GitRepository repo_){
        project = project_;
        repo = repo_;

        update();
    }

    public void update(){
        if (ApplicationManager.getApplication().isDispatchThread()) {
            try {
                ApplicationManager.getApplication().executeOnPooledThread(this::load).get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                LOG.warn(e);
            }
        } else {
            load();
        }
    }

    // One `git config --get-regexp` call: per-branch reads spawned a git process for every local branch.
    private void load() {
        Map<String, String> config = new HashMap<>();
        try {
            for (Map.Entry<String, List<String>> entry : GitConfigUtil.getValues(project, repo.getRoot(), GITFLOW_KEYS).entrySet()) {
                List<String> values = entry.getValue();
                if (values != null && !values.isEmpty()) {
                    config.put(entry.getKey(), values.get(values.size() - 1));
                }
            }
        } catch (VcsException e) {
            // git exits with 1 when no key matches, i.e. the repository has no gitflow config
            LOG.debug(e);
        }

        List<String> localBranchNames = new ArrayList<>();
        for (GitLocalBranch branch : repo.getBranches().getLocalBranches()) {
            localBranchNames.add(branch.getName());
        }

        GitflowConfigValues values = GitflowConfigValues.from(config, localBranchNames);
        masterBranch = values.masterBranch;
        developBranch = values.developBranch;
        featurePrefix = values.featurePrefix;
        releasePrefix = values.releasePrefix;
        hotfixPrefix = values.hotfixPrefix;
        bugfixPrefix = values.bugfixPrefix;
        supportPrefix = values.supportPrefix;
        versiontagPrefix = values.versiontagPrefix;
    }


    public String getFeatureNameFromBranch(String branchName){
        return branchName.substring(branchName.indexOf(featurePrefix)+featurePrefix.length(),branchName.length());
    }

    public String getReleaseNameFromBranch(String branchName){
        return branchName.substring(branchName.indexOf(releasePrefix) + releasePrefix.length(), branchName.length());
    }

    public String getHotfixNameFromBranch(String branchName){
        return branchName.substring(branchName.indexOf(hotfixPrefix) + hotfixPrefix.length(), branchName.length());
    }

    public String getBugfixNameFromBranch(String branchName){
        return branchName.substring(branchName.indexOf(bugfixPrefix)+bugfixPrefix.length(),branchName.length());
    }

    public String getRemoteNameFromBranch(String branchName){
        return branchName.substring(0,branchName.indexOf("/"));
    }

    public void setMasterBranch(String branchName)
    {
        masterBranch = branchName;
        VirtualFile root = repo.getRoot();
        try {
            GitConfigUtil.setValue(project, root, BRANCH_MASTER, branchName);
        } catch (VcsException e) {
            NotifyUtil.notifyError(project, "Config error", e);
        }
    }

    public void setDevelopBranch(String branchName) {
        developBranch = branchName;
        VirtualFile root = repo.getRoot();
        try {
            GitConfigUtil.setValue(project, root, BRANCH_DEVELOP, branchName);
        } catch (VcsException e) {
            NotifyUtil.notifyError(project, "Config error", e);
        }
    }

    public void setReleasePrefix(String prefix) {
        releasePrefix = prefix;
        VirtualFile root = repo.getRoot();

        try {
            GitConfigUtil.setValue(project, root, PREFIX_RELEASE, prefix);
        } catch (VcsException e) {
            NotifyUtil.notifyError(project, "Config error", e);
        }
    }

    public void setFeaturePrefix(String prefix) {
        featurePrefix = prefix;
        VirtualFile root = repo.getRoot();

        try {
            GitConfigUtil.setValue(project, root, PREFIX_FEATURE, prefix);
        } catch (VcsException e) {
            NotifyUtil.notifyError(project, "Config error", e);
        }
    }

    public void setHotfixPrefix(String prefix) {
        hotfixPrefix = prefix;
        VirtualFile root = repo.getRoot();

        try {
            GitConfigUtil.setValue(project, root, PREFIX_HOTFIX, prefix);
        } catch (VcsException e) {
            NotifyUtil.notifyError(project, "Config error", e);
        }
    }

    public void setBugfixPrefix(String prefix) {
        bugfixPrefix = prefix;
        VirtualFile root = repo.getRoot();

        try {
            GitConfigUtil.setValue(project, root, PREFIX_BUGFIX, prefix);
        } catch (VcsException e) {
            NotifyUtil.notifyError(project, "Config error", e);
        }
    }

    public void setSupportPrefix(String prefix) {
        supportPrefix = prefix;
        VirtualFile root = repo.getRoot();

        try {
            GitConfigUtil.setValue(project, root, PREFIX_SUPPORT, prefix);
        } catch (VcsException e) {
            NotifyUtil.notifyError(project, "Config error", e);
        }
    }

    public void setVersionPrefix(String prefix) {
        versiontagPrefix = prefix;
        VirtualFile root = repo.getRoot();

        try {
            GitConfigUtil.setValue(project, root, PREFIX_VERSIONTAG, prefix);
        } catch (VcsException e) {
            NotifyUtil.notifyError(project, "Config error", e);
        }
    }

    // this still reads from config because it always runs from an action and never from the EDT
    public String getBaseBranch(String branchName){

        VirtualFile root = repo.getRoot();

        String baseBranch=null;
        try{
            baseBranch = GitConfigUtil.getValue(project, root, "gitflow.branch."+branchName+".base");
        }
        catch (VcsException e) {
            NotifyUtil.notifyError(project, "Config error", e);
        }

        return baseBranch;
    }
}
