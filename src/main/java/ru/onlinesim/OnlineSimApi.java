package ru.onlinesim;

import org.jetbrains.annotations.Nullable;

import ru.onlinesim.apis.GetFree;
import ru.onlinesim.apis.GetNumbers;
import ru.onlinesim.apis.GetProxy;
import ru.onlinesim.apis.GetRent;
import ru.onlinesim.apis.GetUser;
public class OnlineSimApi {
	private String lang;
	private String apikey;
	private int dev_id;

	public OnlineSimApi(String apikey, int dev_id, String lang) {
		this.apikey = apikey;
		this.dev_id = dev_id;
		this.lang = lang;
	}

	public OnlineSimApi(String apikey, int dev_id) {
		this(apikey, dev_id, "en");
	}

	public OnlineSimApi(String apikey) {
		this(apikey, 0, "en");
	}

	public GetUser user() {
		return new GetUser(apikey, dev_id, lang);
	}

	public GetFree free() {
		return new GetFree(apikey, dev_id, lang);
	}

	public GetNumbers numbers() {
		return new GetNumbers(apikey, dev_id, lang);
	}

	public GetRent rent() {
		return new GetRent(apikey, dev_id, lang);
	}

	public GetProxy proxy() {
		return new GetProxy(apikey, dev_id, lang);
	}

	public String getApiKey() {
		return this.apikey;
	}

	public void setApiKey(String apikey) {
		this.apikey = apikey;
	}

	public String getLang() {
		return this.lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

}
