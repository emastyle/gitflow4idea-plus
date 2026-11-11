package gitflow;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Caches values whose lifetime is bound to a {@link Disposable} owner (typically a GitRepository).
 * Safe against owners disposed concurrently and registers at most one cleanup per owner.
 */
final class RepoScopedCache<V> {
    private record Entry<V>(Disposable owner, V value) {}

    private final Map<String, Entry<V>> entries = new HashMap<>();
    private final Map<Disposable, Set<String>> ownerKeys = new HashMap<>();

    @Nullable
    synchronized V get(@NotNull String key) {
        Entry<V> entry = entries.get(key);
        return entry == null ? null : entry.value();
    }

    /**
     * @return false if the owner is already disposed and nothing was cached
     */
    synchronized boolean put(@NotNull Disposable owner, @NotNull String key, @NotNull V value) {
        Set<String> keys = ownerKeys.get(owner);
        if (keys == null) {
            if (!Disposer.tryRegister(owner, () -> evict(owner))) {
                return false;
            }
            keys = new HashSet<>();
            ownerKeys.put(owner, keys);
        }
        keys.add(key);
        entries.put(key, new Entry<>(owner, value));
        if (Disposer.isDisposed(owner)) {
            evict(owner);
            return false;
        }
        return true;
    }

    synchronized int trackedOwnerCount() {
        return ownerKeys.size();
    }

    private synchronized void evict(Disposable owner) {
        Set<String> keys = ownerKeys.remove(owner);
        if (keys == null) {
            return;
        }
        for (String key : keys) {
            entries.computeIfPresent(key, (k, entry) -> entry.owner() == owner ? null : entry);
        }
    }
}
