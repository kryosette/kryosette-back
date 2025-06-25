package com.example.demo.config.cache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
class SimpleCache<K, V> {

    private final long ttlMillis; // Time-to-live в миллисекундах
    private final Map<K, CacheEntry<V>> cache = new ConcurrentHashMap<>();

    public V get(K key) {
        CacheEntry<V> entry = cache.get(key);
        if (entry != null) {
            if (isExpired(entry)) {
                cache.remove(key);
                return null; // Элемент устарел
            }
            entry.lastAccess = System.currentTimeMillis(); // Обновляем время доступа
            return entry.value;
        }
        return null;
    }

    public void put(K key, V value) {
        cache.put(key, new CacheEntry<>(value));
    }

    public void remove(K key) {
        cache.remove(key);
    }

    public void clear() {
        cache.clear();
    }

    private boolean isExpired(CacheEntry<V> entry) {
        return System.currentTimeMillis() - entry.createTime > ttlMillis;
    }

    private static class CacheEntry<V> {
        final V value;
        final long createTime;
        long lastAccess;

        CacheEntry(V value) {
            this.value = value;
            this.createTime = System.currentTimeMillis();
            this.lastAccess = createTime;
        }
    }
}

@Getter
@Setter
@RequiredArgsConstructor
class User {
    private final String id;
    private final String name;
    private final String email;

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}

@RequiredArgsConstructor
class UserService {

    private final SimpleCache<String, User> userCache;

    public UserService(long cacheTtlMillis) {
        this.userCache = new SimpleCache<>(cacheTtlMillis);
    }

    public User getUser(String userId) {
        // 1. Сначала проверяем кэш
        User user = userCache.get(userId);
        if (user != null) {
            System.out.println("Получено из кэша: " + userId);
            return user;
        }

        // 2. Если нет в кэше, загружаем из базы данных (или другого источника)
        user = loadUserFromDatabase(userId);

        // 3. Кэшируем результат
        if (user != null) {
            userCache.put(userId, user);
            System.out.println("Загружено из базы данных и помещено в кэш: " + userId);
        }

        return user;
    }

    private User loadUserFromDatabase(String userId) {
        // Здесь должна быть логика для загрузки данных пользователя из базы данных
        // В этом примере просто создаем фейкового пользователя
        System.out.println("Загружаем пользователя из базы данных: " + userId);
        if ("1".equals(userId)) {
            return new User("1", "John Doe", "john.doe@example.com");
        } else if ("2".equals(userId)) {
            return new User("2", "Jane Smith", "jane.smith@example.com");
        }
        return null;
    }

    // Методы для управления кэшем (опционально)
    public void invalidateUser(String userId) {
        userCache.remove(userId);
    }

    public void clearCache() {
        userCache.clear();
    }
}

public class Main {
    public static void main(String[] args) throws InterruptedException {
        UserService userService = new UserService(2000); // Кэш с TTL 2 секунды

        // Первый запрос - загрузка из базы данных и помещение в кэш
        User user1 = userService.getUser("1");
        System.out.println(user1);

        // Второй запрос - получение из кэша
        User user1Cached = userService.getUser("1");
        System.out.println(user1Cached);

        Thread.sleep(3000); // Ждем, пока истечет время жизни кэша

        // Третий запрос - снова загрузка из базы данных (т.к. время жизни истекло)
        User user1Reloaded = userService.getUser("1");
        System.out.println(user1Reloaded);
    }
}
