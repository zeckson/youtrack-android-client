package jetbrains.android.data;

public class CaseInsesitiveCache<T> extends Cache<T> {
    public CaseInsesitiveCache(int timeToLive, int cacheMaxSize) {
        super(timeToLive, cacheMaxSize);
    }

    @Override
    public T get(String key) {
        return super.get(key.toLowerCase());
    }

    @Override
    public void put(String key, T value) {
        super.put(key.toLowerCase(), value);
    }
}
