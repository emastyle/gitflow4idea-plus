package gitflow;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class RepoScopedCacheTest {

    @Test
    public void ignoresOwnerDisposedBeforeRegistration() {
        RepoScopedCache<String> cache = new RepoScopedCache<>();
        Disposable repo = Disposer.newDisposable();
        Disposer.dispose(repo);

        assertFalse(cache.put(repo, "/repo", "value"));
        assertNull(cache.get("/repo"));
    }

    @Test
    public void removesValueWhenOwnerIsDisposed() {
        RepoScopedCache<String> cache = new RepoScopedCache<>();
        Disposable repo = Disposer.newDisposable();

        assertTrue(cache.put(repo, "/repo", "first"));
        assertTrue(cache.put(repo, "/repo", "second"));
        assertEquals("second", cache.get("/repo"));

        Disposer.dispose(repo);
        assertNull(cache.get("/repo"));
    }

    @Test
    public void registersCleanupOncePerOwner() {
        RepoScopedCache<String> cache = new RepoScopedCache<>();
        Disposable repo = Disposer.newDisposable();

        for (int i = 0; i < 100; i++) {
            cache.put(repo, "/repo", "value" + i);
        }
        assertEquals(1, cache.trackedOwnerCount());

        Disposer.dispose(repo);
        assertEquals(0, cache.trackedOwnerCount());
    }

    @Test
    public void disposingOldOwnerKeepsReplacement() {
        RepoScopedCache<String> cache = new RepoScopedCache<>();
        Disposable oldRepo = Disposer.newDisposable();
        Disposable newRepo = Disposer.newDisposable();
        cache.put(oldRepo, "/repo", "old");
        cache.put(newRepo, "/repo", "new");

        Disposer.dispose(oldRepo);
        assertEquals("new", cache.get("/repo"));
        Disposer.dispose(newRepo);
        assertNull(cache.get("/repo"));
    }

    @Test
    public void concurrentUpdatesAndDisposalDoNotRetainValues() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        try {
            for (int i = 0; i < 200; i++) {
                RepoScopedCache<String> cache = new RepoScopedCache<>();
                Disposable repo = Disposer.newDisposable();
                CountDownLatch start = new CountDownLatch(1);
                Future<?> first = executor.submit(() -> {
                    start.await();
                    cache.put(repo, "/repo", "first");
                    return null;
                });
                Future<?> second = executor.submit(() -> {
                    start.await();
                    cache.put(repo, "/repo", "second");
                    return null;
                });
                Future<?> disposal = executor.submit(() -> {
                    start.await();
                    Disposer.dispose(repo);
                    return null;
                });
                start.countDown();
                first.get(10, TimeUnit.SECONDS);
                second.get(10, TimeUnit.SECONDS);
                disposal.get(10, TimeUnit.SECONDS);
                assertNull(cache.get("/repo"));
                assertEquals(0, cache.trackedOwnerCount());
            }
        } finally {
            executor.shutdownNow();
        }
    }
}
