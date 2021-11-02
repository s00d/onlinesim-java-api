package ru.onlinesim.response.get_user;

public class Payment {
	private final float payment;
	private final float spent;
	private final float now;
	private final float income;

	public Payment(float payment, float spent, float now, float income) {
		this.payment = payment;
		this.spent = spent;
		this.now = now;
		this.income = income;
	}


	public float getPayment() {
		return payment;
	}

	public float getSpent() {
		return spent;
	}

	public float getNow() {
		return now;
	}

	public float getIncome() {
		return income;
	}

	@Override
	public String toString() {
		return "Balance{" +
				", payment=" + payment +
				", spent=" + spent +
				", now=" + now +
				", income='" + income + '\'' +
				'}';
	}
}
