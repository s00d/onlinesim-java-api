package ru.onlinesim.apis;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import ru.onlinesim.exceptions.OperationException;
import ru.onlinesim.exceptions.RequestException;
import ru.onlinesim.response.get_user.Balance;
import ru.onlinesim.response.get_user.Profile;
import ru.onlinesim.transport.TransportException;

public class GetUser extends BaseApi {
	public GetUser(String apikey, int dev_id, String lang) {
		super(apikey, dev_id, lang);
	}

	public Balance balance() throws OperationException, TransportException, RequestException {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("income", true);
		JsonObject httpResponse = rawClient.makeGetRequest("/api/getBalance.php", map).getAsJsonObject();

		return new Balance(
				Float.parseFloat(httpResponse.get("balance").getAsString()),
				Float.parseFloat(httpResponse.get("zbalance").getAsString()),
				Float.parseFloat(httpResponse.get("income").getAsString())
		);
	}

	public Profile profile() throws OperationException, TransportException, RequestException {
		Map<String,Object> map = new HashMap<String,Object>();
		JsonObject httpResponse = rawClient.makeGetRequest("/api/getProfile.php", map).getAsJsonObject();


		JsonObject profile = httpResponse.get("profile").getAsJsonObject();
//		System.out.printf(profile.toString());

		return new Profile(
				Integer.parseInt(profile.get("id").getAsString()),
				profile.get("hash_id") instanceof JsonNull ? "" : profile.get("hash_id").getAsString(),
				profile.get("name").getAsString(),
				profile.get("username").getAsString(),
				profile.get("email").getAsString(),
				profile.get("apikey").getAsString(),
				profile.get("locale").getAsString(),
				profile.get("created_at").getAsString()
		);
	}

}
