package org.boundaries.thread.thread_local;

import org.boundaries.thread.thread_local.BoundedThreadLocal;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Централизованный реестр для управления всеми ThreadLocal.
 * Предотвращает утечки памяти.
 */
public final class ThreadLocalRegistry {

    private static final Set<BoundedThreadLocal<?>> REGISTRY =
            new CopyOnWriteArraySet<>();

    private static final Map<String, BoundedThreadLocal<?>> BY_NAME =
            new ConcurrentHashMap<>();

    private static final ThreadLocal<Set<BoundedThreadLocal<?>>> THREAD_LOCALS =
            ThreadLocal.withInitial(CopyOnWriteArraySet::new);

    private ThreadLocalRegistry() {
        throw new AssertionError("No instances allowed");
    }

    /**
     * Регистрирует новый BoundedThreadLocal.
     */
    public static <T> BoundedThreadLocal<T> register(BoundedThreadLocal<T> threadLocal) {
        REGISTRY.add(threadLocal);
        BY_NAME.put(threadLocal.getName(), threadLocal);

        // Добавляем слушатель для отслеживания использования в потоках
        Thread current = Thread.currentThread();
        THREAD_LOCALS.get().add(threadLocal);

        return threadLocal;
    }

    /**
     * Создает и регистрирует ThreadLocal через builder.
     */
    public static <T> BoundedThreadLocal<T> create(Class<T> type, String name) {
        BoundedThreadLocal<T> threadLocal = BoundedThreadLocal.builder(type, name)
                .build();
        return register(threadLocal);
    }

    /**
     * Получает ThreadLocal по имени.
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<BoundedThreadLocal<T>> getByName(String name) {
        return Optional.ofNullable((BoundedThreadLocal<T>) BY_NAME.get(name));
    }

    /**
     * Очищает ВСЕ зарегистрированные ThreadLocal в текущем потоке.
     */
    public static void clearAll() {
        REGISTRY.forEach(BoundedThreadLocal::remove);
        THREAD_LOCALS.get().clear();
    }

    /**
     * Очищает только ThreadLocal, использованные в текущем потоке.
     */
    public static void clearCurrentThread() {
        THREAD_LOCALS.get().forEach(BoundedThreadLocal::remove);
        THREAD_LOCALS.get().clear();
    }

    /**
     * Дамп всех ThreadLocal значений для отладки.
     */
    public static Map<String, Object> dump() {
        Map<String, Object> dump = new LinkedHashMap<>();

        THREAD_LOCALS.get().forEach(tl -> {
            try {
                dump.put(tl.getName(), tl.getOptional().orElse(null));
            } catch (Exception e) {
                dump.put(tl.getName(), "<error: " + e.getMessage() + ">");
            }
        });

        return Collections.unmodifiableMap(dump);
    }

    /**
     * Получает статистику использования.
     */
    public static Statistics getStatistics() {
        return new Statistics(
                REGISTRY.size(),
                THREAD_LOCALS.get().size(),
                BY_NAME.size()
        );
    }

    /**
     * Проверяет, есть ли незакрытые ThreadLocal.
     */
    public static boolean hasUnclosed() {
        return !THREAD_LOCALS.get().isEmpty();
    }

    /**
     * Класс для статистики.
     */
    public static class Statistics {
        private final int totalRegistered;
        private final int currentThreadCount;
        private final int namedCount;

        public Statistics(int totalRegistered, int currentThreadCount, int namedCount) {
            this.totalRegistered = totalRegistered;
            this.currentThreadCount = currentThreadCount;
            this.namedCount = namedCount;
        }

        // геттеры
        public int getTotalRegistered() { return totalRegistered; }
        public int getCurrentThreadCount() { return currentThreadCount; }
        public int getNamedCount() { return namedCount; }

        @Override
        public String toString() {
            return String.format(
                    "Statistics{total=%d, currentThread=%d, named=%d}",
                    totalRegistered, currentThreadCount, namedCount);
        }
    }
}}