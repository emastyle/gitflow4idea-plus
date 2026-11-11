package gitflow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;

//import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GitflowVersionTester {

	private static final Logger logger = Logger.getInstance(GitflowVersionTester.class);

	private static final Map<Project, GitflowVersionTester> testers = new ConcurrentHashMap<>();

	public static synchronized GitflowVersionTester forProject(@NotNull Project project) {
		GitflowVersionTester tester = testers.get(project);
		if (tester == null) {
			tester = new GitflowVersionTester(ApplicationManager.getApplication().getService(Gitflow.class), project);
			testers.put(project, tester);
			GitflowVersionTester registeredTester = tester;
			if (!Disposer.tryRegister(project, () -> testers.remove(project, registeredTester))) {
				testers.remove(project, tester);
			}
		}
		return tester;
	}

	@NotNull private final Gitflow gitflow;
	@NotNull private final Project project;

	private String version = null;

	private GitflowVersionTester(@NotNull Gitflow gitflow, @NotNull Project project) {
		this.gitflow = gitflow;
		this.project = project;
	}

	/**
	 * <p>Returns the installed {@code git-flow} version. The version
	 * is loaded on the first call to this method and is determined
	 * by looking at the output of a {@code git flow version} command.</p>
	 * <p>If the command fails, {@code null} is returned.</p>
	 *
	 * @return the {@code git flow} version, or {@code null}
	 */
//	@Nullable
	public String getVersion() {
		return version;
	}

	/**
	 * Returns true if the {@code git flow} version can be determined
	 * and is a supported implementation: AVH Edition, git-flow-next,
	 * or gitflow-cjs. The unmaintained NVIE original version is not supported.
	 *
	 * @return true if the installed git flow CLI is a supported version.
	 */
	public boolean isSupportedVersion() {
		if (version == null) {
			return false;
		}

		String normalized = version.toLowerCase(Locale.ROOT);
		// Support: AVH Edition, git-flow-next, gitflow-cjs
		return normalized.contains("avh")
			|| normalized.contains("git-flow-next")
			|| normalized.contains("git flow next")
			|| normalized.contains("cjs");
	}

	public void init(){
		String returnedVersion = null;
		try {
			returnedVersion = gitflow.version(project).getOutputOrThrow();
			logger.info("git flow version: " + version);
		} catch (Exception e) {
			logger.error("Could not determine git flow version", e);
		}
		if (returnedVersion != null){
			version = returnedVersion;
		}
	}
	
	public boolean hasVersionBeenTested(){
		return version != null;
	}
}
