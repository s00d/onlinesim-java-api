package ru.onlinesim;

/** Base of the response types: {@code toString()} prints the object as JSON with wire field names. */
abstract class Model {
    @Override
    public String toString() {
        return Json.GSON.toJson(this);
    }
}
