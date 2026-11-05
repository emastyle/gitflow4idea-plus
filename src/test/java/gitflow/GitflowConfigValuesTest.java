package gitflow;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class GitflowConfigValuesTest {

    @Test
    public void readsAvhConfigIgnoringPerBranchSections() {
        Map<String, String> config = new HashMap<>();
        config.put("gitflow.branch.master", "master");
        config.put("gitflow.branch.develop", "preprod");
        config.put("gitflow.prefix.feature", "feature/");
        config.put("gitflow.prefix.release", "release/");
        config.put("gitflow.prefix.hotfix", "hotfix/");
        config.put("gitflow.prefix.bugfix", "bugfix/");
        config.put("gitflow.prefix.support", "support/");
        config.put("gitflow.prefix.versiontag", "");
        List<String> branches = new ArrayList<>();
        for (int i = 0; i < 300; i++) {
            branches.add("feature/f" + i);
            config.put("gitflow.branch.feature/f" + i + ".base", "preprod");
        }

        GitflowConfigValues values = GitflowConfigValues.from(config, branches);

        assertEquals("master", values.masterBranch);
        assertEquals("preprod", values.developBranch);
        assertEquals("feature/", values.featurePrefix);
        assertEquals("release/", values.releasePrefix);
        assertEquals("hotfix/", values.hotfixPrefix);
        assertEquals("bugfix/", values.bugfixPrefix);
        assertEquals("support/", values.supportPrefix);
        assertEquals("", values.versiontagPrefix);
    }

    @Test
    public void detectsGitflowNextBaseBranchesAndPrefixes() {
        Map<String, String> config = new HashMap<>();
        config.put("gitflow.branch.main.type", "base");
        config.put("gitflow.branch.develop.type", "base");
        config.put("gitflow.branch.develop.parent", "main");
        config.put("gitflow.branch.feature.prefix", "feat/");
        config.put("gitflow.branch.release.prefix", "rel/");
        config.put("gitflow.branch.hotfix.prefix", "fix/");
        config.put("gitflow.branch.bugfix.prefix", "bug/");
        config.put("gitflow.branch.support.prefix", "sup/");

        GitflowConfigValues values = GitflowConfigValues.from(config, List.of("develop", "main", "feat/x"));

        assertEquals("main", values.masterBranch);
        assertEquals("develop", values.developBranch);
        assertEquals("feat/", values.featurePrefix);
        assertEquals("rel/", values.releasePrefix);
        assertEquals("fix/", values.hotfixPrefix);
        assertEquals("bug/", values.bugfixPrefix);
        assertEquals("sup/", values.supportPrefix);
        assertNull(values.versiontagPrefix);
    }

    @Test
    public void emptyConfigMeansNoGitflow() {
        GitflowConfigValues values = GitflowConfigValues.from(new HashMap<>(), List.of("main"));

        assertNull(values.masterBranch);
        assertNull(values.developBranch);
        assertNull(values.releasePrefix);
    }
}
