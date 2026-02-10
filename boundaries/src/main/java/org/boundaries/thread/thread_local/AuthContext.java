package org.boundaries.thread.thread_local;

import java.util.Optional;

/**
 * Thread-safe context with bounded type safety.
 */
@UtilityClass
public class AuthContext {

    // Явно указываем границы типа и добавляем валидацию
    private static final BoundedThreadLocal<AuthVerifyResponse> CURRENT_USER =
            BoundedThreadLocal.nullable(AuthVerifyResponse.class, "currentUser");

    private static final BoundedThreadLocal<String> RAW_TOKEN =
            BoundedThreadLocal.nullable(String.class, "rawToken");

    // Альтернатива: создаем через фабрику с дополнительными правилами
    private static final ThreadLocal<AuthVerifyResponse> CURRENT_USER_STRICT =
            ThreadLocalFactory.AuthThreadLocal.createWithValidator(() -> {
                // Можно добавить логику по умолчанию
                throw new IllegalStateException("Auth context not initialized");
            });

    /**
     * Вариант с предустановленными проверками.
     */
    public static void setCurrentUser(AuthVerifyResponse userResponse) {
        if (userResponse == null) {
            throw new IllegalArgumentException("User response cannot be null");
        }
        if (userResponse.getUserId() == null || userResponse.getUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        CURRENT_USER.set(userResponse);
    }

    /**
     * Получение с гарантией non-null через bounded типы.
     */
    public static AuthVerifyResponse getCurrentUserOrThrow() {
        return CURRENT_USER.getOptional()
                .orElseThrow(() -> new SecurityException("No authenticated user"));
    }

    /**
     * Внутренний helper для работы с optional.
     */
    private static Optional<AuthVerifyResponse> getCurrentUserOptional() {
        try {
            return Optional.ofNullable(CURRENT_USER.get());
        } catch (IllegalStateException e) {
            // Если bounded ThreadLocal настроен как non-nullable
            return Optional.empty();
        }
    }

    /**
     * Метод с ковариантным возвращаемым типом.
     */
    public static Optional<? extends AuthVerifyResponse> getCurrentUser() {
        return getCurrentUserOptional();
    }

    /**
     * Автоматическая очистка с проверкой.
     */
    public static void clear() {
        try {
            CURRENT_USER.remove();
            RAW_TOKEN.remove();
        } catch (Exception e) {
            // Логирование ошибки очистки
            throw new IllegalStateException("Failed to clear auth context", e);
        }
    }

    /**
     * Безопасное выполнение с автоматической очисткой.
     */
    public static <T> T executeInContext(AuthVerifyResponse user, Supplier<T> action) {
        setCurrentUser(user);
        try {
            return action.get();
        } finally {
            clear();
        }
    }
}