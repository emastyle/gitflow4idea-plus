package gitflow;

import gitflow.ui.GitflowOptionsForm;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.swing.SwingUtilities;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class ReleaseWorkflowTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private File repository;
    private File remote;
    private File globalConfig;

    @Before
    public void setUp() throws Exception {
        repository = temporaryFolder.newFolder("repository");
        remote = temporaryFolder.newFolder("remote.git");
        globalConfig = temporaryFolder.newFile("gitconfig");
        String version = git(repository, "flow", "version");
        assumeTrue("Release workflow tests require Git Flow AVH", version.contains("AVH"));

        git(remote, "init", "--bare");
        git(repository, "init", "-b", "master");
        git(repository, "config", "user.name", "Gitflow Test");
        git(repository, "config", "user.email", "gitflow-test@example.invalid");
        git(repository, "commit", "--allow-empty", "-m", "Initial commit");
        git(repository, "branch", "preprod");
        git(repository, "config", "gitflow.branch.master", "master");
        git(repository, "config", "gitflow.branch.develop", "preprod");
        git(repository, "config", "gitflow.prefix.release", "release/");
        git(repository, "config", "gitflow.prefix.feature", "feature/");
        git(repository, "config", "gitflow.prefix.hotfix", "hotfix/");
        git(repository, "config", "gitflow.prefix.bugfix", "bugfix/");
        git(repository, "config", "gitflow.prefix.support", "support/");
        git(repository, "config", "gitflow.prefix.versiontag", "");
        git(repository, "remote", "add", "origin", remote.getAbsolutePath());
        git(repository, "push", "origin", "master", "preprod");
        git(repository, "switch", "preprod");
    }

    @Test
    public void startsReleaseWithAnExistingRemoteRelease() throws Exception {
        git(repository, "push", "origin", "preprod:refs/heads/release/old");
        git(repository, "fetch", "origin");
        git(repository, "flow", "release", "start", "new");

        assertEquals("release/new", git(repository, "branch", "--show-current").trim());
        assertFalse(git(repository, "ls-remote", "--heads", "origin", "release/old").isBlank());
    }

    @Test
    public void stillBlocksAnExistingLocalRelease() throws Exception {
        git(repository, "flow", "release", "start", "first");
        CommandResult result = execute(repository, "flow", "release", "start", "second");

        assertNotEquals(0, result.exitCode);
        assertTrue(result.output, result.output.contains("There is an existing release branch"));
        assertEquals("release/first", git(repository, "branch", "--show-current").trim());
    }

    @Test
    public void keepRemoteFinishesReleaseWithoutKeepingLocal() throws Exception {
        git(repository, "flow", "release", "start", "1.0");
        git(repository, "commit", "--allow-empty", "-m", "Release changes");
        String releaseCommit = git(repository, "rev-parse", "HEAD").trim();
        git(repository, "flow", "release", "publish", "1.0");

        String keepRemote = GitflowOptionsFactory.getOptionById("RELEASE_keepRemote").get("flag");
        assertEquals("--keepremote", keepRemote);
        git(repository, "flow", "release", "finish", keepRemote, "-m", "Version 1.0", "1.0");

        assertFalse(git(repository, "ls-remote", "--heads", "origin", "release/1.0").isBlank());
        assertNotEquals(0, execute(repository, "show-ref", "--verify", "refs/heads/release/1.0").exitCode);
        git(repository, "merge-base", "--is-ancestor", releaseCommit, "master");
        git(repository, "merge-base", "--is-ancestor", releaseCommit, "preprod");
        git(repository, "rev-parse", "--verify", "refs/tags/1.0");
    }

    @Test
    public void releaseCheckboxesRemainIndependent() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            GitflowOptionsForm form = new GitflowOptionsForm();
            assertNotNull(form.getContentPane());
            form.setOptionActive("RELEASE_keepRemote", true);
            form.setOptionActive("RELEASE_keepLocal", false);
            form.setOptionActive("RELEASE_fetchFromOrigin", false);

            assertTrue(form.isOptionActive("RELEASE_keepRemote"));
            assertFalse(form.isOptionActive("RELEASE_keepLocal"));
            assertFalse(form.isOptionActive("RELEASE_fetchFromOrigin"));
        });
    }

    private String git(File directory, String... arguments) throws Exception {
        CommandResult result = execute(directory, arguments);
        assertEquals("git " + String.join(" ", arguments) + "\n" + result.output, 0, result.exitCode);
        return result.output;
    }

    private CommandResult execute(File directory, String... arguments) throws Exception {
        List<String> command = new ArrayList<>();
        command.add("git");
        command.addAll(Arrays.asList(arguments));
        File output = temporaryFolder.newFile();
        ProcessBuilder builder = new ProcessBuilder(command).directory(directory)
                .redirectErrorStream(true).redirectOutput(output);
        builder.environment().put("GIT_CONFIG_GLOBAL", globalConfig.getAbsolutePath());
        builder.environment().put("GIT_CONFIG_NOSYSTEM", "1");
        builder.environment().put("GIT_TERMINAL_PROMPT", "0");
        builder.environment().put("GIT_MERGE_AUTOEDIT", "no");
        builder.environment().put("GIT_EDITOR", "true");
        builder.environment().put("LC_ALL", "C");
        Process process = builder.start();
        if (!process.waitFor(30, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            fail("Timed out: " + String.join(" ", command));
        }
        return new CommandResult(process.exitValue(), Files.readString(output.toPath(), StandardCharsets.UTF_8));
    }

    private record CommandResult(int exitCode, String output) {}
}
