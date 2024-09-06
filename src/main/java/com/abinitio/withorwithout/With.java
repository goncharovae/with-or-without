package com.abinitio.withorwithout;

public final class With<T> implements WithOrWithout<T> {

    private final T value;

    private With(T value) {
        this.value = value;
    }

    public static <T> With<T> of(T value) {
        return new With<>(value);
    }

    public T get() {
        return value;
    }
}
