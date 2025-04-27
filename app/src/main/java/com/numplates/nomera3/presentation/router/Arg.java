package com.numplates.nomera3.presentation.router;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * created by c7j on 23.05.18
 */
public class Arg<K, V> {

    public final @Nullable K key;
    public final @Nullable V value;

    @SuppressWarnings("WeakerAccess")
    public Arg(@Nullable K key, @Nullable V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("Arg{%s %s}", key, value);
    }

    @NonNull
    public static <A, B> Arg<A, B> create(@Nullable A a, @Nullable B b) {
        return new Arg<>(a, b);
    }

}
