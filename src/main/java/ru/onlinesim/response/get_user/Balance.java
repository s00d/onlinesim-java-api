package ru.onlinesim.response.get_user;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Balance {
	private final float balance;
	private final float zbalance;
	private final float income;

	public Balance(float balance, float zbalance, float income) {
		this.balance = balance;
		this.zbalance = zbalance;
		this.income = income;
	}

	public Balance(float balance, float zbalance) {
		this.balance = balance;
		this.zbalance = zbalance;
		this.income = 0;
	}

	public float getBalance() {
		return balance;
	}

	public float getZBalance() {
		return zbalance;
	}

	public float getIncome() {
		return income;
	}

	@Override
	public String toString() {
		return "Balance{" +
				"balance=" + balance +
				", zbalance=" + zbalance +
				", income=" + income +
				'}';
	}
}
