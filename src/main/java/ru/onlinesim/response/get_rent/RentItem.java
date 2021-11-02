package ru.onlinesim.response.get_rent;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

import ru.onlinesim.exceptions.RequestException;

public class RentItem {
	private final int tzid;
	private final int status;
	private final ArrayList<RentItemMessage> msg;
	private final int country;
	private final int rent;
	private final int extension;
	private final float sum;
	private final BigInteger number;
	private final int time;
	private final int hours;
	private final HashMap<Integer, Integer> extend;
	private final boolean checked;
	private final Integer reload;
	private final int day_extend;

	public RentItem(int tzid, int status, ArrayList<RentItemMessage> msg, int country, int rent, int extension, float sum, BigInteger number, int time, int hours, HashMap<Integer, Integer> extend, boolean checked, Integer reload, int day_extend) {
		this.tzid = tzid;
		this.status = status;
		this.msg = msg;
		this.country = country;
		this.rent = rent;
		this.extension = extension;
		this.sum = sum;
		this.number = number;
		this.time = time;
		this.hours = hours;
		this.extend = extend;
		this.checked = checked;
		this.reload = reload;
		this.day_extend = day_extend;
	}

	public int getTzid() {
		return tzid;
	}

	public int getStatus() {
		return status;
	}

	public ArrayList<RentItemMessage> getMesg() {
		return msg;
	}

	public int getCountry() {
		return country;
	}

	public int getRent() {
		return rent;
	}

	public int getExtension() {
		return extension;
	}

	public float getSum() {
		return sum;
	}

	public BigInteger getNumber() {
		return number;
	}

	public int getTime() {
		return time;
	}

	public int getHours() {
		return hours;
	}

	public HashMap<Integer, Integer> getExtend() {
		return extend;
	}

	public boolean isChecked() {
		return checked;
	}

	public int getDay_extend() {
		return day_extend;
	}

	public RentItemMessage getLastMessage() throws RequestException {
		if(msg.size() == 0) {
			throw new RequestException("NO_MESSAGE");
		}
		return msg.get(0);
	}

	public RentItemMessage getFirstMessage() throws RequestException {
		if(msg.size() == 0) {
			throw new RequestException("NO_MESSAGE");
		}
		return msg.get(msg.size() - 1);
	}

	@Override
	public String toString() {
		return "Number{" +
				"tzid=" + tzid +
				", status=" + status +
				", msg=" + msg +
				", country=" + country +
				", rent=" + rent +
				", extension=" + extension +
				", sum=" + sum +
				", number=" + number +
				", time=" + time +
				", hours=" + hours +
				", extend=" + extend +
				", checked=" + checked +
				", day_extend=" + day_extend +
				'}';
	}

	public Integer getReload() {
		return reload;
	}
}
