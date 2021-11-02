package ru.onlinesim;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import io.github.cdimascio.dotenv.Dotenv;
import nl.jqno.equalsverifier.internal.util.Assert;
import ru.onlinesim.apis.GetFree;
import ru.onlinesim.apis.GetNumbers;
import ru.onlinesim.apis.GetProxy;
import ru.onlinesim.apis.GetRent;
import ru.onlinesim.apis.GetUser;
import ru.onlinesim.exceptions.OperationException;
import ru.onlinesim.exceptions.RequestException;
import ru.onlinesim.response.get_free.Country;
import ru.onlinesim.response.get_free.Message;
import ru.onlinesim.response.get_free.Number;
import ru.onlinesim.response.get_numbers.GetNumResult;
import ru.onlinesim.response.get_numbers.NumberItem;
import ru.onlinesim.response.get_numbers.Service;
import ru.onlinesim.response.get_proxy.Proxy;
import ru.onlinesim.response.get_proxy.Tariff;
import ru.onlinesim.response.get_rent.RentItem;
import ru.onlinesim.response.get_user.Balance;
import ru.onlinesim.response.get_user.Profile;
import ru.onlinesim.transport.TransportException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class OnlineSimApiTest {
	private String apikey;
	private OnlineSimApi loader;

	@BeforeEach
	void setUp() {
		Dotenv dotenv = Dotenv.load();
		this.apikey = dotenv.get("TEST_APIKEY");
		this.loader = new OnlineSimApi(apikey, 0);
	}

	@AfterEach
	void tearDown() {

	}

	@Test
	void user() throws OperationException, TransportException, RequestException {
		GetUser user = loader.user();

		Balance balance = user.balance();
		assertTrue(balance.getBalance() > 0);

		Profile profile = user.profile();
		assertEquals(profile.getName(),"s00d");
	}


	@Test
	void free() throws OperationException, TransportException, RequestException {
		GetFree free = loader.free();

		ArrayList<Country> countries = free.countries();
		assertTrue(countries.size() > 0);

		ArrayList<Number> numbers = free.numbers(7);
		assertTrue(numbers.size() > 0);

		ArrayList<Message> messages = free.messages(7, numbers.get(0).getNumber(), 1);
		assertTrue(messages.size() > 0);
	}

	@Test
	void numbers() throws OperationException, TransportException, RequestException {
		GetNumbers numbers = loader.numbers();

		ru.onlinesim.response.get_numbers.Tariff countries = numbers.tariffs(7);

		assertTrue(countries.getCountries().containsKey("_7"));
		assertTrue(countries.getServices().containsKey("_3223"));
		assertEquals(countries.getPage(), 1);
		assertFalse(countries.isEnd());

		Service select = countries.getServices().get("_3223");
		assertTrue(select.getCount() > 0);

		GetNumResult number = numbers.get(select.getService(), 7);

		assertFalse(number.getNumber().isEmpty());
		assertTrue(number.getTzid() > 0);

		HashMap<Integer, NumberItem> state = numbers.state("ASC");
		Object first = state.keySet().toArray()[0];
		NumberItem myNumber = state.get(first);
		System.out.printf(myNumber.toString());

		assertEquals(myNumber.getNumber(), number.getNumber());

		numbers.close(number.getTzid());
	}

	@Test
	void rent() throws OperationException, TransportException, RequestException {
		GetRent rent = loader.rent();
		HashMap<String, ru.onlinesim.response.get_rent.Tariff> countries = rent.tariffs();

		RentItem number = rent.get(7, 7);

		assertTrue(number.getTzid() > 0);

		int tzid = number.getTzid();
		RentItem selectNumber = rent.stateOne(tzid);
//
		System.out.println(selectNumber.toString());
		System.out.println(selectNumber.getLastMessage().toString());
		System.out.println(selectNumber.getFirstMessage().toString());
//
		assertEquals(selectNumber.getTzid(), tzid);

		HashMap<java.lang.Number, RentItem> stateNumber = rent.state();

		boolean closeNumber = rent.close(tzid);
		assertTrue(closeNumber);
	}

	@Test
	void proxy() throws OperationException, TransportException, RequestException {
		GetProxy proxy = loader.proxy();

		Tariff tariffs = proxy.tariffs();

		System.out.println(tariffs.toString());
		assertTrue(tariffs.getDays().getConfig().size() > 0);

		Proxy myProxy = proxy.getTraffic("100MB");

		System.out.println(myProxy.toString());

		HashMap<java.lang.Number, Proxy> list = proxy.state();

		System.out.println(list.toString());
	}
}
