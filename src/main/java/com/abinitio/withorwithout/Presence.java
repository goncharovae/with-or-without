package com.abinitio.withorwithout;

import java.util.function.Supplier;

public final class Presence<T, P extends WithOrWithout<T>> {

    @SuppressWarnings("rawtypes")
    private static final Presence WITH = new Presence<>(true);
    @SuppressWarnings("rawtypes")
    private static final Presence WITHOUT = new Presence<>(false);

    private final boolean isPresent;

    private Presence(boolean isPresent) {
        this.isPresent = isPresent;
    }

    @SuppressWarnings("unchecked")
    static <T> Presence<T, With<T>> with() {
        return WITH;
    }

    @SuppressWarnings("unchecked")
    static <T> Presence<T, Without<T>> without() {
        return WITHOUT;
    }

    @SuppressWarnings("unchecked")
    static <T> Presence<T, WithOrWithout<T>> withOrWithout(boolean with) {
        return with ? WITH : WITHOUT;
    }

    public boolean isPresent() {
        return isPresent;
    }

    @SuppressWarnings("unchecked")
    public P ifPresent(Supplier<T> supplier) {
        if (isPresent) {
            return (P) With.of(supplier.get());
        }
        return null;
    }
}
