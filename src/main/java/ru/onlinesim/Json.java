package ru.onlinesim;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.Strictness;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * The one Gson instance and the client-side normalisation of server quirks. Stateless and thread-safe.
 *
 * <ul>
 *   <li>Field names: Java camelCase ↔ wire snake_case.</li>
 *   <li>Numbers are lenient: a numeric string ({@code "1442.330"}) parses, anything else ({@code false}, {@code "?"})
 *       becomes {@code null} (0 for primitives) instead of failing.</li>
 *   <li>Booleans accept {@code 1}/{@code 0}; strings accept numbers; other mismatches become {@code null}.</li>
 *   <li>A map sent by PHP as an empty array {@code []} becomes an empty map.</li>
 * </ul>
 */
final class Json {
    static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setStrictness(Strictness.STRICT)
            .registerTypeAdapterFactory(new ArrayAsEmptyMap())
            .registerTypeAdapter(Double.class, lenient(Json::toDouble))
            .registerTypeAdapter(double.class, lenient(Json::toDouble))
            .registerTypeAdapter(Long.class, lenient(p -> toDecimal(p).longValueExact()))
            .registerTypeAdapter(long.class, lenient(p -> toDecimal(p).longValueExact()))
            .registerTypeAdapter(Integer.class, lenient(p -> toDecimal(p).intValueExact()))
            .registerTypeAdapter(int.class, lenient(p -> toDecimal(p).intValueExact()))
            .registerTypeAdapter(Boolean.class, lenient(Json::toBoolean))
            .registerTypeAdapter(boolean.class, lenient(Json::toBoolean))
            .registerTypeAdapter(String.class, lenient(p -> p.isBoolean() ? null : p.getAsString()))
            .create();

    private Json() {}

    /** Strict parse; {@code null} for an empty body. Throws {@link com.google.gson.JsonParseException} for non-JSON. */
    static JsonElement parse(String text) {
        return GSON.fromJson(text, JsonElement.class);
    }

    /** Element → {@code type} (a class or a parameterized type); {@code null} for a missing element. */
    static <T> T convert(JsonElement element, Type type) {
        return GSON.fromJson(element, type);
    }

    static Type listOf(Type item) {
        return TypeToken.getParameterized(List.class, item).getType();
    }

    static Type mapOf(Type value) {
        return TypeToken.getParameterized(Map.class, String.class, value).getType();
    }

    /** Unmodifiable view, or an empty map for {@code null}. */
    static <V> Map<String, V> map(Map<String, V> value) {
        return value == null ? Collections.emptyMap() : Collections.unmodifiableMap(value);
    }

    /** Unmodifiable view, or an empty list for {@code null}. */
    static <T> List<T> list(List<T> value) {
        return value == null ? Collections.emptyList() : Collections.unmodifiableList(value);
    }

    /** String of a JSON string or number; {@code null} otherwise. */
    static String string(JsonElement e) {
        if (e == null || !e.isJsonPrimitive() || e.getAsJsonPrimitive().isBoolean()) return null;
        return e.getAsString();
    }

    private static Double toDouble(JsonPrimitive p) {
        double d = p.isNumber()
                ? p.getAsDouble()
                : Double.parseDouble(p.getAsString().trim());
        return Double.isFinite(d) ? d : null;
    }

    private static BigDecimal toDecimal(JsonPrimitive p) {
        return new BigDecimal(p.getAsString().trim());
    }

    private static Boolean toBoolean(JsonPrimitive p) {
        if (p.isBoolean()) return p.getAsBoolean();
        String s = p.getAsString().trim();
        if (s.equals("1") || s.equals("true")) return true;
        if (s.equals("0") || s.equals("false")) return false;
        return null;
    }

    /** Reads any JSON value; a primitive goes through {@code convert}, failures and non-primitives give {@code null}. */
    private static <T> TypeAdapter<T> lenient(Function<JsonPrimitive, T> convert) {
        return new TypeAdapter<T>() {
            @Override
            public T read(JsonReader in) throws IOException {
                JsonElement e = JsonParser.parseReader(in);
                if (!e.isJsonPrimitive()) return null;
                try {
                    return convert.apply(e.getAsJsonPrimitive());
                } catch (NumberFormatException | ArithmeticException ex) {
                    return null;
                }
            }

            @Override
            public void write(JsonWriter out, T value) throws IOException {
                if (value == null) out.nullValue();
                else if (value instanceof Number) out.value((Number) value);
                else if (value instanceof Boolean) out.value((Boolean) value);
                else out.value(value.toString());
            }
        };
    }

    /** PHP encodes an empty map as {@code []}: read it as an empty map. */
    private static final class ArrayAsEmptyMap implements TypeAdapterFactory {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (!Map.class.isAssignableFrom(type.getRawType())) return null;
            TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
            return new TypeAdapter<T>() {
                @Override
                public T read(JsonReader in) throws IOException {
                    if (in.peek() == JsonToken.BEGIN_ARRAY) {
                        in.skipValue();
                        return delegate.fromJsonTree(new JsonObject());
                    }
                    return delegate.read(in);
                }

                @Override
                public void write(JsonWriter out, T value) throws IOException {
                    delegate.write(out, value);
                }
            };
        }
    }
}
