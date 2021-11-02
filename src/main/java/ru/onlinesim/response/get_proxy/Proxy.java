package ru.onlinesim.response.get_proxy;

public class Proxy {
	private final int tzid;
	private final String type;
	private final String connect_type;
	private final String host;
	private final int port;
	private final String user;
	private final String pass;
	private final String operator;
	private final int status;
	private final String country;
	private final String rent;
	private final String comment;
	private final int port_count;
	private final boolean session;
	private final String city;
	private final String traffic;
	private final String general_traffic;
	private final String stop_at;
	private final String check_at;
	private final String created_at;
	private final String updated_at;
	private final int time;
	private final int days;
	private final int hours;
	private final boolean change_ip;
	private final boolean change_type;
	private final boolean rotate;

	public Proxy(int tzid, String type, String connect_type, String host, int port, String user, String pass, String operator, int status, String country, String rent, String comment, int port_count, boolean session, String city, String traffic, String general_traffic, String stop_at, String check_at, String created_at, String updated_at, int time, int days, int hours, boolean change_ip, boolean change_type, boolean rotate) {
		this.tzid = tzid;
		this.type = type;
		this.connect_type = connect_type;
		this.host = host;
		this.port = port;
		this.user = user;
		this.pass = pass;
		this.operator = operator;
		this.status = status;
		this.country = country;
		this.rent = rent;
		this.comment = comment;
		this.port_count = port_count;
		this.session = session;
		this.city = city;
		this.traffic = traffic;
		this.general_traffic = general_traffic;
		this.stop_at = stop_at;
		this.check_at = check_at;
		this.created_at = created_at;
		this.updated_at = updated_at;
		this.time = time;
		this.days = days;
		this.hours = hours;
		this.change_ip = change_ip;
		this.change_type = change_type;
		this.rotate = rotate;
	}

	public int getTzid() {
		return tzid;
	}

	public String getType() {
		return type;
	}

	public String getConnect_type() {
		return connect_type;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getUser() {
		return user;
	}

	public String getPass() {
		return pass;
	}

	public String getOperator() {
		return operator;
	}

	public int getStatus() {
		return status;
	}

	public String getCountry() {
		return country;
	}

	public String getRent() {
		return rent;
	}

	public String getComment() {
		return comment;
	}

	public int getPort_count() {
		return port_count;
	}

	public boolean isSession() {
		return session;
	}

	public String getCity() {
		return city;
	}

	public String getTraffic() {
		return traffic;
	}

	public String getGeneral_traffic() {
		return general_traffic;
	}

	public String getStop_at() {
		return stop_at;
	}

	public String getCheck_at() {
		return check_at;
	}

	public String getCreated_at() {
		return created_at;
	}

	public String getUpdated_at() {
		return updated_at;
	}

	public int getTime() {
		return time;
	}

	public int getDays() {
		return days;
	}

	public int getHours() {
		return hours;
	}

	public boolean isChange_ip() {
		return change_ip;
	}

	public boolean isChange_type() {
		return change_type;
	}

	public boolean isRotate() {
		return rotate;
	}

	@Override
	public String toString() {
		return "Proxy{" +
				"tzid=" + tzid +
				", type=" + type +
				", connect_type=" + connect_type +
				", host=" + host +
				", port=" + port +
				", user=" + user +
				", pass=" + pass +
				", operator=" + operator +
				", status=" + status +
				", country=" + country +
				", rent=" + rent +
				", comment=" + comment +
				", port_count=" + port_count +
				", session=" + session +
				", city=" + city +
				", traffic=" + traffic +
				", general_traffic=" + general_traffic +
				", stop_at=" + stop_at +
				", check_at=" + check_at +
				", created_at=" + created_at +
				", updated_at=" + updated_at +
				", time=" + time +
				", days=" + days +
				", hours=" + hours +
				", change_ip=" + change_ip +
				", change_type=" + change_type +
				", rotate=" + rotate +
				'}';
	}
}
