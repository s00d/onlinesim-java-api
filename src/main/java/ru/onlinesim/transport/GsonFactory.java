package ru.onlinesim.transport;

import com.google.gson.Gson;

public class GsonFactory {
	private static final Gson GSON = new Gson();

	public static Gson getGson() {
		return GSON;
	}
}
