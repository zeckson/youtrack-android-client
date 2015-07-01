package jetbrains.android.data;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Cache<T> {
    private int timeToLive;
    private int cacheMaxSize;
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private Map<String, CacheEntry<T>> cache = new LinkedHashMap<String, CacheEntry<T>>();

    public Cache(int timeToLive, int cacheMaxSize) {
        this.timeToLive = timeToLive;
        this.cacheMaxSize = cacheMaxSize;
    }

    public T get(String key) {
        T result = null;
        Lock readLock = lock.readLock();
        readLock.lock();
        try {
            CacheEntry<T> cacheEntry = cache.get(key);
            if (cacheEntry != null && cacheEntry.isValid(timeToLive)) {
                result = cacheEntry.getValue();
            }
        } finally {
            readLock.unlock();
        }
        return result;
    }

    public void put(String key, T value) {
        Lock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            if (cache.containsKey(key)) {
                cache.remove(key);
            }
            CacheEntry<T> cacheEntry = new CacheEntry<T>(value);
            cache.put(key, cacheEntry);
            clean();
        } finally {
            writeLock.unlock();
        }
    }

    private void clean() {
        int itemsToDelete = cache.size() - cacheMaxSize;
        int index = 0;
        for (Iterator<String> i = cache.keySet().iterator(); i.hasNext();) {
            String key = i.next();
            if (++index < itemsToDelete || !cache.get(key).isValid(timeToLive)) {
                i.remove();
            } else {
                break;
            }
        }
    }

    private static class CacheEntry<T> {
        private T value;
        private long createTime;

        private CacheEntry(T value) {
            this.value = value;
            createTime = System.currentTimeMillis();
        }

        public T getValue() {
            return value;
        }

        public boolean isValid(long timeToLive) {
            return System.currentTimeMillis() - createTime <= timeToLive;
        }
    }
}
