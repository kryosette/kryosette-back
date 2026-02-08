package org.boundaries.thread.thread_local;

import java.util.function.Supplier;

/**
 * Strictly typed ThreadLocal container with compile-time safety.
 */
public class BoundedThreadLocal<T> {

    private final ThreadLocal<T> threadLocal;
    private final Class<T> type;
    private final String name;
    private final boolean nullable;

    private BoundedThreadLocal(Class<T> type, String name, boolean nullable) {
        this.type = type;
        this.name = name;
        this.nullable = nullable;
        this.threadLocal = new ThreadLocal<>();
    }

    private BoundedThreadLocal(Class<T> type, String name, boolean nullable, Supplier<T> intialSupplier) {
        this.type = type;
        this.name = name;
        this.nullable = nullable;
        this.threadLocal = ThreadLocal.withInitial(() -> {
            T value = intialSupplier.get();
            validate(value);
            return value;
        });
    }

    public void set(T value) {
        validate(value);
        threadLocal.set(value);
    }

    public T get() {
        T value = threadLocal.get();
        if (!nullable && value == null) {
            throw new IllegalArgumentException(
                    String.format("ThreadLocal '%s' of type %s cannot be null",
                            name, type.getSimpleName()));
        }

        return value;
    }

    public T getOrDefault(T defaultValue) {
        T value = threadLocal.get();
        return value != null ? value : defaultValue;
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

    private void validate(T value) {
        if (!nullable && value == null) {
            throw new IllegalArgumentException(
                    String.format("Value for ThreadLocal '%s' cannot be null", name));
        }

        if (value != null && !type.isInstance(value)) {
            throw new IllegalArgumentException(
                    String.format("Expected type %s but got %s",
                            type.getName(), value.getClass().getName()));
        }
    }
}
