package ru.onlinesim.response.get_numbers;

public class GetNumResult {
	private final int tzid;
	private final String number;

	public GetNumResult(int tzid, String number) {
		this.tzid = tzid;
		this.number = number;
	}


	public int getTzid() {
		return tzid;
	}

	public String getNumber() {
		return number;
	}

	@Override
	public String toString() {
		return "GetNumResult{" +
				"tzid=" + tzid +
				", number=" + number +
				'}';
	}
}
