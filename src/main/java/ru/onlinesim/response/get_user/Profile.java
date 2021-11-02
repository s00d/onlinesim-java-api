package ru.onlinesim.response.get_user;

import org.jetbrains.annotations.Nullable;

import ru.onlinesim.response.Base;

public class Profile extends Base {
	private final int id;
	private final String hash_id;
	private final String name;
	private final String username;
	private final String email;
	private final String apikey;
	private final String locale;
	private final String created_at;
//	private final Payment payment;

	public Profile(int id, @Nullable String hash_id, String name, String username, String email, String apikey, String locale, String created_at) {
		this.id = id;
		this.hash_id = hash_id;
		this.name = name;
		this.username = username;
		this.email = email;
		this.apikey = apikey;
		this.locale = locale;
		this.created_at = created_at;
//		this.payment = payment;
	}

	public int getId() {
		return id;
	}

	public String getHash_id() {
		return hash_id;
	}

	public String getName() {
		return name;
	}

	public String getUsername() {
		return username;
	}

	public String getEmail() {
		return email;
	}

	public String getApikey() {
		return apikey;
	}

	public String getLocale() {
		return locale;
	}

	public String getCreated_at() {
		return created_at;
	}

}
