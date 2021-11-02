package ru.onlinesim.apis;

import org.jetbrains.annotations.Nullable;

import ru.onlinesim.transport.RawClient;

public class BaseApi {
	protected final RawClient rawClient;

	public BaseApi(String apikey, @Nullable int dev_id, String lang) {
		this.rawClient = new RawClient(apikey, dev_id, lang);
	}
}
