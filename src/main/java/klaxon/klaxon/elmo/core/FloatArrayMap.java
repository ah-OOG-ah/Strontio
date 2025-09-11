package klaxon.klaxon.elmo.core;

import static java.lang.Math.round;
import static java.lang.System.arraycopy;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import klaxon.klaxon.elmo.core.math.FloatUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FloatArrayMap<V> implements Map<Float, V> {
    private float[] keys;
    private V[] values;
    private int size;
    private final Class<V> clazz;

    public FloatArrayMap(Class<V> clazz) {
        this.clazz = clazz;

        keys = new float[4];
        //noinspection unchecked
        values = (V[]) Array.newInstance(clazz, 4);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        if (!(key instanceof Float f)) return false;

        return indexOf(f) > -1;
    }

    @Override
    public boolean containsValue(Object value) {
        if (clazz.isAssignableFrom(value.getClass())) return false;

        for (int i = 0; i < size; ++i) { if (Objects.equals(values[i], value)) return true; }
        return false;
    }

    @Override
    public V get(Object key) {
        if (!(key instanceof Float f)) return null;

        final var idx = indexOf(f);

        return idx < 0 ? null : values[idx];
    }

    @Override
    public @Nullable V put(Float key, V value) {
        if (value == null) return null;

        final var idx = indexOf(key);
        if (idx > -1) {
            final var ret = values[idx];
            values[idx] = value;
            return ret;
        }

        if (size >= keys.length) resize();
        keys[size] = key;
        values[size] = value;
        ++size;
        return null;
    }

    private int indexOf(float key) {
        for (int i = 0; i < size; ++i) { if (FloatUtils.equals(key, keys[i])) return i; }
        return -1;
    }

    private void resize() {
        final var newSize = (int) round(keys.length * 1.5);
        keys = Arrays.copyOf(keys, newSize);
        values = Arrays.copyOf(values, newSize);
    }

    @Override
    public V remove(Object key) {
        if (!(key instanceof Float f)) return null;

        final var idx = indexOf(f);
        if (idx < 0) return null;

        final var ret = values[idx];
        final var len = keys.length - idx - 1;
        arraycopy(keys, idx + 1, keys, idx, len);
        arraycopy(values, idx + 1, values, idx, len);
        return ret;
    }

    @Override
    public void putAll(@NotNull Map<? extends Float, ? extends V> m) {
        for (var e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public void clear() {
        size = 0;
    }

    @Override
    public @NotNull Set<Float> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Collection<V> values() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Set<Entry<Float, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEach(BiConsumer<? super Float, ? super V> action) {
        for (int i = 0; i < size; ++i) {
            action.accept(keys[i], values[i]);
        }
    }
}
