package ru.onlinesim.transport;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ru.onlinesim.Utils;
import ru.onlinesim.exceptions.OperationException;
import ru.onlinesim.exceptions.RequestException;

import java.util.Map;

/**
 * RawClient request
 */
public class RawClient {
	private String lang = "en";
	private String apikey = "";
	private int dev_id = 0;

	public static final String DEFAULT_HOST = "onlinesim.host";

	private final HttpTransport httpTransport;
	private final String agentAddress;

	public RawClient(String apikey, int dev_id, String lang) {
		this(new DefaultHttpTransport(), DEFAULT_HOST, "");
		this.apikey = apikey;
		this.dev_id = dev_id;
		this.lang = lang;
	}

	// hidden constructor, for test
	RawClient(HttpTransport httpTransport, String agentHost, String path) {
		this.httpTransport = httpTransport;

		// check that agentHost has scheme or not
		String agentHostLowercase = agentHost.toLowerCase();
		if (!agentHostLowercase.startsWith("https://") && !agentHostLowercase.startsWith("http://")) {
			// no scheme in host, use default 'http'
			agentHost = "http://" + agentHost;
		}

		this.agentAddress = Utils.assembleAgentAddress(agentHost, path);
	}

	public JsonElement makeGetRequest(String endpoint, Map<String,Object> urlParams) throws TransportException, RequestException, OperationException {
		urlParams.put("apikey", apikey);
		urlParams.put("dev_id", dev_id);
		urlParams.put("lang", lang);

		String params = MapQuery.urlEncodeUTF8(urlParams);

//		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

		String url = prepareUrl(agentAddress + endpoint);

//		System.out.println(url + '?' + params);

		HttpRequest request = HttpRequest.Builder.newBuilder().setUrl(url + '?' + params).build();

		HttpResponse httpResponse = httpTransport.makeGetRequest(request);

//		System.out.println(httpResponse.getContent());

		if (httpResponse.getStatusCode() == 200) {
			JsonElement pars = new JsonParser().parse(httpResponse.getContent());
			try {
				JsonArray jsonArray = pars.getAsJsonArray();
				return pars;
			} catch (Exception e) { }

			JsonObject jsonObject = pars.getAsJsonObject();
			if(jsonObject.has("response")) {
				String resp = jsonObject.get("response").getAsString();
				if(!resp.equals("1")) {
					throw new RequestException(resp);
				}
			}

			return pars;
		}
		throw new OperationException(httpResponse);
	}

//	public HttpResponse makeGetRequest(String endpoint, List<UrlParameters> urlParams) throws TransportException {
//		String url = prepareUrl(agentAddress + endpoint);
//		url = Utils.generateUrl(url, urlParams);
//
//		HttpRequest request = HttpRequest.Builder.newBuilder()
//				.setUrl(url)
//				.build();
//
//		return httpTransport.makeGetRequest(request);
//	}
//
//	public HttpResponse makeGetRequest(Request request) throws TransportException {
//		String url = prepareUrl(agentAddress + request.getEndpoint());
//		url = Utils.generateUrl(url, request.getUrlParameters());
//
//		HttpRequest httpRequest = HttpRequest.Builder.newBuilder()
//				.setUrl(url)
//				.addHeaders(Utils.createTokenMap(request.getToken()))
//				.build();
//
//		return httpTransport.makeGetRequest(httpRequest);
//	}
//
//	public HttpResponse makePutRequest(String endpoint, String content, UrlParameters... urlParams) throws TransportException {
//		String url = prepareUrl(agentAddress + endpoint);
//		url = Utils.generateUrl(url, urlParams);
//
//		HttpRequest request = HttpRequest.Builder.newBuilder()
//				.setUrl(url)
//				.setContent(content)
//				.build();
//
//		return httpTransport.makePutRequest(request);
//	}
//
//	public HttpResponse makePutRequest(Request request) throws TransportException {
//		//,  String endpoint, byte[] binaryContent, UrlParameters... urlParams
//		String url = prepareUrl(agentAddress + request.getEndpoint());
//		url = Utils.generateUrl(url, request.getUrlParameters());
//
//		HttpRequest httpRequest = HttpRequest.Builder.newBuilder()
//				.setUrl(url)
//				.setBinaryContent(request.getBinaryContent())
//				.addHeaders(Utils.createTokenMap(request.getToken()))
//				.build();
//
//		return httpTransport.makePutRequest(httpRequest);
//	}
//
//	public HttpResponse makeDeleteRequest(Request request) throws TransportException {
//		String url = prepareUrl(agentAddress + request.getEndpoint());
//		url = Utils.generateUrl(url, request.getUrlParameters());
//
//		HttpRequest httpRequest = HttpRequest.Builder.newBuilder()
//				.setUrl(url)
//				.addHeaders(Utils.createTokenMap(request.getToken()))
//				.build();
//
//		return httpTransport.makeDeleteRequest(httpRequest);
//	}
//
	private String prepareUrl(String url) {
		if (url.contains(" ")) {
			// temp hack for old clients who did manual encoding and just use %20
			// TODO: Remove it in 2.0
			return Utils.encodeUrl(url);
		} else {
			return url;
		}
	}

}
