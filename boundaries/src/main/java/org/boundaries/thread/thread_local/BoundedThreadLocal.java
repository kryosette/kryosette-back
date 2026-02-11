package org.boundaries.thread.thread_local;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Strictly typed ThreadLocal container with compile-time safety.
 */
public final class BoundedThreadLocal<T> {

    private final ThreadLocal<T> threadLocal;
    private final Class<T> type;
    private final String name;
    private final boolean nullable;
    private final Supplier<T> defaultValueSupplier;

    private BoundedThreadLocal(Builder<T> builder) {
        this.type = builder.type;
        this.name = builder.name;
        this.nullable = builder.nullable;
        this.defaultValueSupplier = builder.defaultValueSupplier;

        if (builder.initialSupplier != null) {
            this.threadLocal = ThreadLocal.withInitial(() -> {
                T value = builder.initialSupplier.get();
                validate(value);
                return value;
            });
        } else {
            this.threadLocal = new ThreadLocal<>();
        }
    }

    /**
     * Builder для создания BoundedThreadLocal.
     */
    public static class Builder<T> {
        private final Class<T> type;
        private final String name;
        private boolean nullable = true;
        private Supplier<T> initialSupplier;
        private Supplier<T> defaultValueSupplier;

        private Builder(Class<T> type, String name) {
            if (type == null) throw new IllegalArgumentException("Type cannot be null");
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Name cannot be null or empty");
            }
            this.type = type;
            this.name = name;
        }

        public Builder<T> notNullable() {
            this.nullable = false;
            return this;
        }

        public Builder<T> withInitial(Supplier<T> initialSupplier) {
            this.initialSupplier = initialSupplier;
            return this;
        }

        public Builder<T> withInitialValue(T initialValue) {
            this.initialSupplier = () -> initialValue;
            return this;
        }

        public Builder<T> withDefaultValue(Supplier<T> defaultValueSupplier) {
            this.defaultValueSupplier = defaultValueSupplier;
            return this;
        }

        public Builder<T> withDefaultValue(T defaultValue) {
            this.defaultValueSupplier = () -> defaultValue;
            return this;
        }

        public BoundedThreadLocal<T> build() {
            return new BoundedThreadLocal<>(this);
        }
    }

    /**
     * Создает builder для BoundedThreadLocal.
     */
    public static <T> Builder<T> builder(Class<T> type, String name) {
        return new Builder<>(type, name);
    }

    /**
     * Устаревшие фабричные методы (для обратной совместимости).
     */
    @Deprecated
    public static <T> BoundedThreadLocal<T> nullable(Class<T> type, String name) {
        return builder(type, name).build();
    }

    @Deprecated
    public static <T> BoundedThreadLocal<T> withInitial(
            Class<T> type, String name, Supplier<T> supplier) {
        return builder(type, name)
                .notNullable()
                .withInitial(supplier)
                .build();
    }

    @Deprecated
    public static <T> BoundedThreadLocal<T> withInitialValue(
            Class<T> type, String name, T initialValue) {
        return builder(type, name)
                .notNullable()
                .withInitialValue(initialValue)
                .build();
    }

    /**
     * Основные методы.
     */
    public void set(T value) {
        validate(value);
        threadLocal.set(value);
    }

    public T get() {
        T value = threadLocal.get();
        if (value == null && !nullable) {
            if (defaultValueSupplier != null) {
                value = defaultValueSupplier.get();
                set(value); // кэшируем значение по умолчанию
            } else {
                throw new IllegalStateException(
                        String.format("ThreadLocal '%s' of type %s cannot be null",
                                name, type.getSimpleName()));
            }
        }
        return value;
    }

    public Optional<T> getOptional() {
        return Optional.ofNullable(threadLocal.get());
    }

    public T getOrDefault() {
        T value = threadLocal.get();
        if (value == null && defaultValueSupplier != null) {
            return defaultValueSupplier.get();
        }
        return value;
    }

    public T getOrDefault(T customDefault) {
        T value = threadLocal.get();
        return value != null ? value : customDefault;
    }

    public void remove() {
        threadLocal.remove();
    }

    public boolean isSet() {
        return threadLocal.get() != null;
    }

    public Class<T> getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    /**
     * Валидация значения.
     */
    private void validate(T value) {
        if (value == null) {
            if (!nullable) {
                throw new IllegalArgumentException(
                        String.format("Value for ThreadLocal '%s' cannot be null", name));
            }
            return;
        }

        if (!type.isInstance(value)) {
            throw new IllegalArgumentException(
                    String.format("Expected type %s but got %s for ThreadLocal '%s'",
                            type.getName(), value.getClass().getName(), name));
        }
    }

    /**
     * Выполнение с временным значением.
     */
    public void runWithValue(T value, Runnable action) {
        T previous = threadLocal.get();
        try {
            set(value);
            action.run();
        } finally {
            restore(previous);
        }
    }

    /**
     * Выполнение с временным значением и возвратом результата.
     */
    public <R> R callWithValue(T value, Supplier<R> action) {
        T previous = threadLocal.get();
        try {
            set(value);
            return action.get();
        } finally {
            restore(previous);
        }
    }

    /**
     * Выполнение с очисткой контекста.
     */
    public void runWithoutValue(Runnable action) {
        T previous = threadLocal.get();
        try {
            remove();
            action.run();
        } finally {
            restore(previous);
        }
    }

    /**
     * Восстановление предыдущего значения.
     */
    private void restore(T previous) {
        if (previous != null) {
            threadLocal.set(previous);
        } else {
            threadLocal.remove();
        }
    }

    @Override
    public String toString() {
        return String.format("BoundedThreadLocal[name=%s, type=%s, nullable=%s, set=%s]",
                name, type.getSimpleName(), nullable, isSet());
    }
}