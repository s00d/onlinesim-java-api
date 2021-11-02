package ru.onlinesim.apis;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ru.onlinesim.exceptions.OperationException;
import ru.onlinesim.exceptions.RequestException;
import ru.onlinesim.response.get_free.Country;
import ru.onlinesim.response.get_free.Message;
import ru.onlinesim.response.get_free.Number;
import ru.onlinesim.transport.TransportException;

public class GetFree extends BaseApi {
	public GetFree(String apikey, int dev_id, String lang) {
		super(apikey, dev_id, lang);
	}

	public ArrayList<Country> countries() throws OperationException, TransportException, RequestException {
		Map<String,Object> map = new HashMap<String,Object>();
		JsonObject httpResponse = rawClient.makeGetRequest("/api/getFreeCountryList.php", map).getAsJsonObject();

		ArrayList<Country> result = new ArrayList<Country>();
		JsonArray array = httpResponse.get("countries").getAsJsonArray();

		for (int i = 0; i < array.size(); i++) {
			JsonObject line = array.get(i).getAsJsonObject();
			result.add(new Country(
					line.get("country").getAsInt(),
					line.get("country_text").getAsString()
			));
		}

		return result;
	}

	public ArrayList<Number> numbers(int country) throws OperationException, TransportException, RequestException {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("country", country);
		JsonObject httpResponse = rawClient.makeGetRequest("/api/getFreePhoneList.php", map).getAsJsonObject();

		ArrayList<Number> result = new ArrayList<Number>();
		JsonArray array = httpResponse.get("numbers").getAsJsonArray();

		for (int i = 0; i < array.size(); i++) {
			JsonObject line = array.get(i).getAsJsonObject();
			result.add(new Number(
					line.get("number").getAsBigInteger(),
					line.get("country").getAsInt(),
					line.get("country_text").getAsString(),
					line.get("updated_at").getAsString(),
					line.get("full_number").getAsString()
			));
		}

		return result;
	}

	public ArrayList<Message> messages(int country, BigInteger phone, int page) throws OperationException, TransportException, RequestException {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("country", country);
		map.put("phone", phone);
		map.put("page", page);
		JsonObject httpResponse = rawClient.makeGetRequest("/api/getFreeMessageList.php", map).getAsJsonObject().get("messages").getAsJsonObject();

		ArrayList<Message> result = new ArrayList<Message>();
		JsonArray array = httpResponse.get("data").getAsJsonArray();

		for (int i = 0; i < array.size(); i++) {
			JsonObject line = array.get(i).getAsJsonObject();
			result.add(new Message(
					line.get("text").getAsString(),
					line.get("in_number").getAsString(),
					line.get("created_at").getAsString()
			));
		}

		return result;
	}

}
