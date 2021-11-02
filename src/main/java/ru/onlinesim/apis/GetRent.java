package ru.onlinesim.apis;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ru.onlinesim.exceptions.OperationException;
import ru.onlinesim.exceptions.RequestException;
import ru.onlinesim.response.get_rent.RentItem;
import ru.onlinesim.response.get_rent.RentItemMessage;
import ru.onlinesim.response.get_rent.Tariff;
import ru.onlinesim.transport.TransportException;

class JSONToNumber {
	public static RentItem convert(JsonObject item) {
		ArrayList<RentItemMessage> messages = new ArrayList<RentItemMessage>();
		if(item.has("messages")) {
			JsonArray mesg = item.get("messages").getAsJsonArray();
			for(JsonElement key : mesg) {
				JsonObject m = key.getAsJsonObject();
				messages.add(new RentItemMessage(
						m.get("id").getAsInt(),
						m.get("service").getAsString(),
						m.get("text").getAsString(),
						m.get("code").getAsString(),
						m.get("created_at").getAsString()
				));
			}
		}

		HashMap<Integer, Integer> extend = new HashMap<Integer, Integer>();
		if(item.has("extend")) {
			JsonObject ext = item.get("extend").getAsJsonObject();
			for(String key : ext.keySet()) {
				extend.put(Integer.parseInt(key), ext.get(key).getAsInt());
			}
		}
		return new RentItem(
				item.get("tzid").getAsInt(),
				item.get("status").getAsInt(),
				messages,
				item.get("country").getAsInt(),
				item.get("rent").getAsInt(),
				item.get("extension").getAsInt(),
				item.get("sum").getAsFloat(),
				item.get("number").getAsBigInteger(),
				item.get("time").getAsInt(),
				item.get("hours").getAsInt(),
				extend,
				item.get("checked").getAsBoolean(),
				item.get("reload") instanceof JsonNull ? null : item.get("reload").getAsInt(),
				item.get("day_extend").getAsInt()
		);
	}
}

public class GetRent extends BaseApi {
	public GetRent(String apikey, int dev_id, String lang) {
		super(apikey, dev_id, lang);
	}

	public RentItem get(int country, int days, boolean extension) throws OperationException, TransportException, RequestException {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("country", country);
		map.put("days", days);
		map.put("extension", extension);
		map.put("pagination", false);
		JsonObject httpResponse = rawClient.makeGetRequest("/api/rent/getRentNum.php", map).getAsJsonObject().get("item").getAsJsonObject();

		return JSONToNumber.convert(httpResponse);
	}

	public RentItem get(int country, int days) throws OperationException, TransportException, RequestException {
		return this.get(country, days, true);
	}


	public HashMap<Number, RentItem> state() throws OperationException, TransportException, RequestException {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("number", true);
		map.put("pagination", false);

		JsonArray httpResponse = rawClient.makeGetRequest("/api/rent/getRentState.php", map).getAsJsonObject().get("list").getAsJsonArray();

		HashMap<Number, RentItem> result = new HashMap<Number, RentItem>();
		for(JsonElement value : httpResponse) {
			JsonObject item = value.getAsJsonObject();
			result.put(item.get("tzid").getAsInt(), JSONToNumber.convert(item));
		}

		return result;
	}

	public RentItem stateOne(int tzid) throws OperationException, TransportException, RequestException {
		HashMap<Number, RentItem> list = this.state();
		if(!list.containsKey(tzid)) {
			throw new RequestException("NO_NUMBER");
		}
		return list.get(tzid);
	}

	public RentItem extend(int tzid, int days) throws OperationException, TransportException, RequestException {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("tzid", tzid);
		map.put("days", days);

		JsonObject httpResponse = rawClient.makeGetRequest("/api/rent/extendRentState.php", map).getAsJsonObject().get("item").getAsJsonObject();

		return JSONToNumber.convert(httpResponse);
	}

	public boolean close(int tzid) throws OperationException, TransportException, RequestException {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("tzid", tzid);

		rawClient.makeGetRequest("/api/rent/closeRentNum.php", map);

		return true;
	}

	public boolean unfreeze(int tzid) throws OperationException, TransportException, RequestException {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("tzid", tzid);

		rawClient.makeGetRequest("/api/rent/portUnFreeze.php", map);

		return true;
	}

	public boolean portReload(int tzid) throws OperationException, TransportException, RequestException {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("tzid", tzid);

		rawClient.makeGetRequest("/api/rent/portReload.php", map);

		return true;
	}

	public HashMap<String, Tariff> tariffs() throws OperationException, TransportException, RequestException {
		Map<String,Object> map = new HashMap<String,Object>();
		JsonObject httpResponse = rawClient.makeGetRequest("/api/rent/tariffsRent.php", map).getAsJsonObject();

		HashMap<String, Tariff> result = new HashMap<String, Tariff>();

		for(String key : httpResponse.keySet()) {
			JsonObject line = httpResponse.get(key).getAsJsonObject();

			HashMap<String, Integer> count = new HashMap<String, Integer>();
			if(line.has("count")) {
				JsonObject c = line.get("count").getAsJsonObject();
				for(String k : c.keySet()) {
					int v = c.get(k).getAsInt();
					count.put(k, v);
				}
			}
			HashMap<String, Integer> days = new HashMap<String, Integer>();
			if(line.has("days")) {
				JsonObject d = line.get("days").getAsJsonObject();
				for(String k : d.keySet()) {
					int v = d.get(k).getAsInt();
					days.put(k, v);
				}
			}
			result.put(key, new Tariff(
					line.get("code").getAsInt(),
					line.get("enabled").getAsBoolean(),
					line.get("name").getAsString(),
					line.get("new").getAsBoolean(),
					line.get("position").getAsInt(),
					count,
					days,
					line.get("extend").getAsInt()
			));
		}

		return result;
	}
}
