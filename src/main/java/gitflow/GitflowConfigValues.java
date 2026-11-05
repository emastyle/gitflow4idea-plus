package gitflow;

import java.util.Collection;
import java.util.Map;

/**
 * Gitflow settings resolved from a single snapshot of the repository's {@code gitflow.*} config.
 */
public final class GitflowConfigValues {
    public final String masterBranch;
    public final String developBranch;
    public final String featurePrefix;
    public final String releasePrefix;
    public final String hotfixPrefix;
    public final String bugfixPrefix;
    public final String supportPrefix;
    public final String versiontagPrefix;

    private GitflowConfigValues(Map<String, String> config, Collection<String> localBranchNames) {
        String production = null;
        String develop = null;

        // git-flow-next: base branches have type=base; develop has a parent, production has none
        for (String branchName : localBranchNames) {
            String prefix = "gitflow.branch." + branchName + ".";
            if ("base".equals(config.get(prefix + "type"))) {
                String parent = config.get(prefix + "parent");
                if (parent == null || parent.isEmpty()) {
                    production = branchName;
                } else {
                    develop = branchName;
                    if (production == null) {
                        production = parent;
                    }
                }
            }
            if (production != null && develop != null) {
                break;
            }
        }

        if (production == null || develop == null) {
            production = config.get(GitflowConfigUtil.BRANCH_MASTER);
            develop = config.get(GitflowConfigUtil.BRANCH_DEVELOP);
        }

        masterBranch = production;
        developBranch = develop;
        featurePrefix = firstNonNull(config, GitflowConfigUtil.PREFIX_FEATURE, GitflowConfigUtil.BRANCH_FEATURE_PREFIX);
        releasePrefix = firstNonNull(config, GitflowConfigUtil.PREFIX_RELEASE, GitflowConfigUtil.BRANCH_RELEASE_PREFIX);
        hotfixPrefix = firstNonNull(config, GitflowConfigUtil.PREFIX_HOTFIX, GitflowConfigUtil.BRANCH_HOTFIX_PREFIX);
        bugfixPrefix = firstNonNull(config, GitflowConfigUtil.PREFIX_BUGFIX, GitflowConfigUtil.BRANCH_BUGFIX_PREFIX);
        supportPrefix = firstNonNull(config, GitflowConfigUtil.PREFIX_SUPPORT, GitflowConfigUtil.BRANCH_SUPPORT_PREFIX);
        versiontagPrefix = config.get(GitflowConfigUtil.PREFIX_VERSIONTAG);
    }

    public static GitflowConfigValues from(Map<String, String> config, Collection<String> localBranchNames) {
        return new GitflowConfigValues(config, localBranchNames);
    }

    private static String firstNonNull(Map<String, String> config, String key, String fallbackKey) {
        String value = config.get(key);
        return value != null ? value : config.get(fallbackKey);
    }
}
