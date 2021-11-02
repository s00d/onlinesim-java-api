package ru.onlinesim.apis;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

import ru.onlinesim.exceptions.OperationException;
import ru.onlinesim.exceptions.RequestException;
import ru.onlinesim.response.get_proxy.Proxy;
import ru.onlinesim.response.get_proxy.Tariff;
import ru.onlinesim.transport.TransportException;

class JSONToProxy {
	public static Proxy convert(JsonObject item) {
		return new Proxy(
				item.get("tzid").getAsInt(),
				item.get("type").getAsString(),
				item.get("connect_type").getAsString(),
				item.get("host").getAsString(),
				item.get("port").getAsInt(),
				item.get("user").getAsString(),
				item.get("pass").getAsString(),
				item.get("operator").getAsString(),
				item.has("status") ? item.get("status").getAsInt() : 0,
				item.get("country").getAsString(),
				item.get("rent").getAsString(),
				!item.has("comment") || item.get("comment") instanceof JsonNull ? "" : item.get("comment").getAsString(),
				item.get("port_count").getAsInt(),
				item.get("session").getAsBoolean(),
				item.get("city").getAsString(),
				item.get("traffic").getAsString(),
				item.get("general_traffic").getAsString(),
				item.get("stop_at").getAsString(),
				!item.has("check_at") || item.get("check_at") instanceof JsonNull ? "" : item.get("check_at").getAsString(),
				item.get("created_at").getAsString(),
				item.get("updated_at").getAsString(),
				item.get("time").getAsInt(),
				item.get("days").getAsInt(),
				item.get("hours").getAsInt(),
				item.get("change_ip").getAsBoolean(),
				item.get("change_type").getAsBoolean(),
				item.get("rotate").getAsBoolean()
		);
	}
}

public class GetProxy extends BaseApi {
	public GetProxy(String apikey, int dev_id, String lang) {
		super(apikey, dev_id, lang);
	}

	public Proxy getDays(int days, String type, String connect) throws OperationException, TransportException, RequestException {
		return this.getDays(days, type, connect, "any", "any", "any", 1);
	}

	public Proxy getDays(int days, String type, String connect, String operator, String country, String city, int port_count) throws OperationException, TransportException, RequestException {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("class", "days");
		map.put("type", type);
		map.put("connect", connect);
		map.put("operator", operator);
		map.put("country", country);
		map.put("city", city);
		map.put("port_count", port_count);
		map.put("count", days);

		JsonObject httpResponse = rawClient.makeGetRequest("/api/proxy/getProxy.php", map).getAsJsonObject().get("item").getAsJsonObject();

		return JSONToProxy.convert(httpResponse);
	}

	public Proxy getTraffic(String traffic) throws OperationException, TransportException, RequestException {
		return this.getTraffic(traffic, "any", "any", "any", true);
	}

	public Proxy getTraffic(String traffic, String operator, String country, String city, boolean session) throws OperationException, TransportException, RequestException {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("class", "traffic");
		map.put("operator", operator);
		map.put("country", country);
		map.put("city", city);
		map.put("session", session);
		map.put("count", traffic);

		JsonObject httpResponse = rawClient.makeGetRequest("/api/proxy/getProxy.php", map).getAsJsonObject().get("item").getAsJsonObject();

		return JSONToProxy.convert(httpResponse);
	}

	public HashMap<Number, Proxy> state(String orderby) throws OperationException, TransportException, RequestException {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("number", true);
		map.put("orderby", orderby);

		JsonArray httpResponse = rawClient.makeGetRequest("/api/proxy/getState.php", map).getAsJsonObject().get("list").getAsJsonArray();

		HashMap<Number, Proxy> result = new HashMap<Number, Proxy>();
		for(JsonElement value : httpResponse) {
			JsonObject item = value.getAsJsonObject();
			result.put(item.get("tzid").getAsInt(), JSONToProxy.convert(item));
		}

		return result;

	}

	public HashMap<Number, Proxy> state() throws OperationException, TransportException, RequestException {
		return this.state("ASC");
	}

	public Proxy stateOne(int tzid) throws OperationException, TransportException, RequestException {
		HashMap<Number, Proxy> list = this.state();
		if(!list.containsKey(tzid)) {
			throw new RequestException("NO_NUMBER");
		}
		return list.get(tzid);
	}


	public boolean close(int tzid) throws OperationException, TransportException, RequestException {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("tzid", tzid);

		rawClient.makeGetRequest("/api/proxy/close.php", map);

		return true;
	}

	public boolean changeIp(int tzid) throws OperationException, TransportException, RequestException {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("tzid", tzid);

		rawClient.makeGetRequest("/api/proxy/changeIp.php", map);

		return true;
	}

	public boolean changeType(int tzid) throws OperationException, TransportException, RequestException {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("tzid", tzid);

		rawClient.makeGetRequest("/api/proxy/changeType.php", map);

		return true;
	}

	public boolean setComment(int tzid) throws OperationException, TransportException, RequestException {
		return this.setComment(tzid, "");
	}

	public boolean setComment(int tzid, String comment) throws OperationException, TransportException, RequestException {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("tzid", tzid);
		map.put("comment", comment);

		rawClient.makeGetRequest("/api/proxy/setComment.php", map);

		return true;
	}

	public Tariff tariffs() throws OperationException, TransportException, RequestException {
		Map<String,Object> map = new HashMap<String,Object>();

		JsonObject list = rawClient.makeGetRequest("/api/proxy/tariffs.php", map).getAsJsonObject();

		return new Gson().fromJson(list, Tariff.class);
	}

}
