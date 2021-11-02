package ru.onlinesim.apis;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ru.onlinesim.exceptions.OperationException;
import ru.onlinesim.exceptions.RequestException;
import ru.onlinesim.response.get_numbers.Country;
import ru.onlinesim.response.get_numbers.GetNumResult;
import ru.onlinesim.response.get_numbers.NumberItem;
import ru.onlinesim.response.get_numbers.NumberItemMessage;
import ru.onlinesim.response.get_numbers.Service;
import ru.onlinesim.response.get_numbers.Tariff;
import ru.onlinesim.transport.TransportException;


public class GetNumbers extends BaseApi {
	public GetNumbers(String apikey, int dev_id, String lang) {
		super(apikey, dev_id, lang);
	}

	public float price(String service, int country) throws OperationException, TransportException, RequestException {
		Map<String,Object> map = new HashMap<String,Object>();

		JsonObject httpResponse = rawClient.makeGetRequest("/api/getPrice.php", map).getAsJsonObject();

		return httpResponse.get("price").getAsFloat();
	}

	public GetNumResult get(String service) throws OperationException, TransportException, RequestException {
		return this.get(service, 7, new ArrayList<Integer>());
	}

	public GetNumResult get(String service, Integer country) throws OperationException, TransportException, RequestException {
		return this.get(service, country, new ArrayList<Integer>());
	}

	public GetNumResult get(String service, Integer country, ArrayList<Integer> reject) throws OperationException, TransportException, RequestException {
		if (country == null) {
			country = 7;
		}
		if (reject == null) {
			reject = new ArrayList<Integer>();
		}

		Map<String,Object> map = new HashMap<String,Object>();
		map.put("number", true);
		map.put("service", service);
		map.put("country", country);
		map.put("reject", reject);

		JsonObject httpResponse = rawClient.makeGetRequest("/api/getNum.php", map).getAsJsonObject();

		return new GetNumResult(
				httpResponse.get("tzid").getAsInt(),
				httpResponse.get("number").getAsString()
		);
	}

	public HashMap<Integer, NumberItem> state(String orderby) throws OperationException, TransportException, RequestException {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("message_to_code", 2);
		map.put("orderby", orderby == null ? "ASC" : orderby);
		map.put("msg_list", true);

		JsonArray array = rawClient.makeGetRequest("/api/getState.php", map).getAsJsonArray();

		HashMap<Integer, NumberItem> result = new HashMap<Integer, NumberItem>();
		for (int i = 0; i < array.size(); i++) {
			JsonObject line = array.get(i).getAsJsonObject();

			ArrayList<NumberItemMessage> msgs = new ArrayList<NumberItemMessage>();

			if (line.has("msg")) {
				JsonArray messages = line.get("msg").getAsJsonArray();
				for(JsonElement value : messages) {
					JsonObject msg = value.getAsJsonObject();

					msgs.add(new NumberItemMessage(
							msg.get("service").getAsString(),
							msg.get("text").getAsString(),
							msg.get("code").getAsString()
					));
				}
			}

			result.put(line.get("tzid").getAsInt(), new NumberItem(
					line.get("tzid").getAsInt(),
					line.get("response").getAsString(),
					line.get("number").getAsString(),
					line.get("service").getAsString(),
					line.get("time").getAsInt(),
					msgs,
					line.has("extend") ? line.get("extend").getAsInt() : 0,
					line.get("country").getAsInt()
			));
		}

		return result;
	}

	public NumberItem stateOne(int tzid) throws OperationException, TransportException, RequestException {
		HashMap<Integer, NumberItem> list = this.state("ASC");
		if(list.containsKey(tzid)) {
			throw new RequestException("NO_NUMBER");
		}
		return list.get(tzid);
	}

//	public boolean next(int tzid) throws OperationException, TransportException, RequestException {
//		Map<String,Object> map = new HashMap<String,Object>();
//		map.put("tzid", tzid);
//		JsonObject httpResponse = rawClient.makeGetRequest("/api/setOperationRevise.php", map);
//
//		return true;
//	}

	public boolean close(int tzid) throws OperationException, TransportException, RequestException {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("tzid", tzid);
		JsonObject httpResponse = rawClient.makeGetRequest("/api/setOperationOk.php", map).getAsJsonObject();

		return true;
	}

	public boolean ban(int tzid) throws OperationException, TransportException, RequestException {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("tzid", tzid);
		map.put("ban", true);
		JsonObject httpResponse = rawClient.makeGetRequest("/api/setOperationOk.php", map).getAsJsonObject();

		return true;
	}

	public Tariff tariffs(Integer country) throws OperationException, TransportException, RequestException {
		return this.tariffs(country, 1, "");
	}

	public Tariff tariffs(Integer country, Integer page, String filter) throws OperationException, TransportException, RequestException {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("country", country == null ? 7 : country);
		map.put("page", page == null ? 1 : page);
		map.put("filter", filter == null ? "" : filter);

		JsonObject httpResponse = rawClient.makeGetRequest("/api/getTariffs.php", map).getAsJsonObject();
		JsonObject countries = httpResponse.get("countries").getAsJsonObject();
		JsonObject favorites = httpResponse.get("favorites").getAsJsonObject();
		JsonObject services = httpResponse.get("services").getAsJsonObject();

		HashMap<String, Country> countriesResult = new HashMap<String, Country>();
		for(String key : countries.keySet()) {
			JsonObject line = countries.get(key).getAsJsonObject();

			countriesResult.put(key, new Country(
					line.get("name").getAsString(),
					line.get("original").getAsString(),
					line.get("code").getAsInt(),
					line.get("pos").getAsInt(),
					line.get("other").getAsString(),
					line.get("new").getAsBoolean(),
					line.get("enable").getAsBoolean()
			));
		}
		HashMap<String, Service> favoritesResult = new HashMap<String, Service>();
		for(String key : favorites.keySet()) {
			JsonObject line = favorites.get(key).getAsJsonObject();
			favoritesResult.put(key, new Service(
				line.get("id").getAsInt(),
				line.get("count").getAsInt(),
				line.get("price").getAsInt(),
				line.get("service").getAsString(),
				line.get("slug").getAsString()
			));
		}
		HashMap<String, Service> servicesResult = new HashMap<String, Service>();
		for(String key : services.keySet()) {
			JsonObject line = services.get(key).getAsJsonObject();
			servicesResult.put(key, new Service(
					line.get("id").getAsInt(),
					line.get("count").getAsInt(),
					line.get("price").getAsInt(),
					line.get("service").getAsString(),
					line.get("slug").getAsString()
			));
		}

		return new Tariff(
			countriesResult,
			favoritesResult,
			servicesResult,
			httpResponse.get("page").getAsInt(),
			httpResponse.get("end").getAsBoolean()
		);
	}

//	public String wait_code(int tzid, boolean not_end, boolean full_message) throws Exception {
//		String __last_code = "";
//		int _response_type = 1;
//		if (full_message) {
//			_response_type = 0;
//		}
//
//		int counter = 0;
//		while (true) {
//			Thread.sleep(1000);
//			counter += 1;
//			if (counter >= 10) {
//				throw new Exception("Timeout error");
//			}
//			NumberItem response = this.stateOne(tzid, _response_type);
//
//			if ("msg" in response && !not_end && response["msg"] != __last_code) {
//				__last_code = response["msg"];
//				this.close(tzid);
//				break;
//			} else if("msg" in response && not_end && response["msg"] != __last_code) {
//				__last_code = response["msg"];
//				this.next(tzid);
//				break;
//			}
//		}
//		return __last_code;
//	}
}
