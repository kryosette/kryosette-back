package org.boundaries.thread.thread_local;

import java.util.function.Supplier;

/**
 * Strictly typed ThreadLocal container with compile-time safety. (for bounders)
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

    /**
     * Creates a nullable bounded ThreadLocal.
     */
    public static <T> BoundedThreadLocal<T> nullable(Class<T> type, String name) {
        return new BoundedThreadLocal<>(type, name, true);
    }

    /**
     * Creates with initial value supplier.
     */
    public static <T> BoundedThreadLocal<T> withInitial(
            Class<T> type, String name, Supplier<T> supplier) {
        return new BoundedThreadLocal<>(type, name, false, supplier);
    }

    /**
     * Creates with initial value.
     */
    public static <T> BoundedThreadLocal<T> withInitialValue(
            Class<T> type, String name, T initialValue) {
        return new BoundedThreadLocal<>(type, name, false, () -> initialValue);
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

    /**
     * Executes action with temporary value, ensuring cleanup.
     */
    public void withValue(T value, Runnable action) {
        T previous = threadLocal.get();
        try {
            set(value);
            action.run();
        } finally {
            if (previous != null) {
                set(previous);
            } else {
                remove();
            }
        }
    }

    /**
     * Executes supplier with temporary value.
     */
    public <R> R withValue(T value, Supplier<R> supplier) {
        T previous = threadLocal.get();
        try {
            set(value);
            return supplier.get();
        } finally {
            if (previous != null) {
                set(previous);
            } else {
                remove();
            }
        }
    }
}
