package ru.onlinesim.response.get_numbers;

import java.util.ArrayList;

import ru.onlinesim.exceptions.RequestException;

/**
 * NumberItem resp
 */
public class NumberItem {
	private final int tzid;
	private final String response;
	private final String number;
	private final String service;
	private final int time;
	private final ArrayList<NumberItemMessage> msg;
	private final int extend;
	private final int country;

	public NumberItem(int tzid, String response, String number, String service, int time, ArrayList<NumberItemMessage> msg, int extend, int country) {
		this.tzid = tzid;
		this.response = response;
		this.number = number;
		this.service = service;
		this.time = time;
		this.msg = msg;
		this.extend = extend;
		this.country = country;
	}

	public int getTzid() {
		return tzid;
	}

	public String getResponse() {
		return response;
	}

	public String getNumber() {
		return number;
	}

	public String getService() {
		return service;
	}

	public int getTime() {
		return time;
	}

	public ArrayList<NumberItemMessage> getMsg() {
		return msg;
	}

	public int getExtend() {
		return extend;
	}

	public int getCountry() {
		return country;
	}

	public NumberItemMessage getLastMessage() throws RequestException {
		if(msg.size() == 0) {
			throw new RequestException("NO_MESSAGE");
		}
		return msg.get(msg.size() - 1);
	}
	public NumberItemMessage getFirstMessage() throws RequestException {
		if(msg.size() == 0) {
			throw new RequestException("NO_MESSAGE");
		}
		return msg.get(0);
	}


	@Override
	public String toString() {
		return "Number{" +
				"tzid=" + tzid +
				", response=" + response +
				", number=" + number +
				", service=" + service +
				", time=" + time +
				", msg=" + msg +
				", extend=" + extend +
				", country=" + country +
				'}';
	}
}
