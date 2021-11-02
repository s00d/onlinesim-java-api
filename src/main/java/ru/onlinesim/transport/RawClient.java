package ru.onlinesim.transport;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ru.onlinesim.Utils;
import ru.onlinesim.exceptions.OperationException;
import ru.onlinesim.exceptions.RequestException;

import java.util.Map;

public class RawClient {
	private String lang = "en";
	private String apikey = "";
	private int dev_id = 0;

	public static final String DEFAULT_HOST = "onlinesim.ru";
//	public static final int DEFAULT_PORT = 8500;
//	public static final String DEFAULT_PATH = "";
//
//	// one real HTTP client for all instances
//	private static final HttpTransport DEFAULT_HTTP_TRANSPORT = new DefaultHttpTransport();

	private final HttpTransport httpTransport;
	private final String agentAddress;

//	public static final class Builder {
//		private String agentHost;
//		private int agentPort;
//		private String agentPath;
//		private HttpTransport httpTransport;
//
//		public static RawClient.Builder builder() {
//			return new RawClient.Builder();
//		}
//
//		private Builder() {
//			this.agentHost = DEFAULT_HOST;
//			this.agentPort = DEFAULT_PORT;
//			this.agentPath = DEFAULT_PATH;
//			this.httpTransport = DEFAULT_HTTP_TRANSPORT;
//		}
//
//		public Builder setHost(String host) {
//			this.agentHost = host;
//			return this;
//		}
//
//		public Builder setPort(int port) {
//			this.agentPort = port;
//			return this;
//		}
//
//		public Builder setPath(String path) {
//			this.agentPath = path;
//			return this;
//		}
//
//		public Builder setTlsConfig(TLSConfig tlsConfig) throws TransportException {
//			this.httpTransport = new DefaultHttpsTransport(tlsConfig);
//			return this;
//		}
//
//		public Builder setHttpClient(HttpClient httpClient) {
//			this.httpTransport = new DefaultHttpTransport(httpClient);
//			return this;
//		}
//
//		public RawClient build() {
//			return new RawClient(httpTransport, agentHost, agentPath);
//		}
//	}

//	public RawClient() {
//		this(DEFAULT_HOST);
//	}
//
//	public RawClient(TLSConfig tlsConfig) throws TransportException {
//		this(DEFAULT_HOST, tlsConfig);
//	}
//	public RawClient(String agentHost) {
//		this(agentHost, DEFAULT_PORT);
//	}
//
//	public RawClient(String agentHost, TLSConfig tlsConfig) throws TransportException {
//		this(agentHost, DEFAULT_PORT, tlsConfig);
//	}
//
//	public RawClient(String agentHost, int agentPort) {
//		this(DEFAULT_HTTP_TRANSPORT, agentHost, agentPort, DEFAULT_PATH);
//	}
//
//	public RawClient(HttpClient httpClient) {
//		this(DEFAULT_HOST, httpClient);
//	}
////
//	public RawClient(String agentHost, HttpClient httpClient) {
//		this(new DefaultHttpTransport(httpClient), agentHost, DEFAULT_PORT, DEFAULT_PATH);
//	}
//
//	public RawClient(String agentHost, int agentPort, HttpClient httpClient) {
//		this(new DefaultHttpTransport(httpClient), agentHost, agentPort, DEFAULT_PATH);
//	}
//
//	public RawClient(String agentHost, int agentPort, TLSConfig tlsConfig) throws TransportException {
//		this(new DefaultHttpsTransport(tlsConfig), agentHost, agentPort, DEFAULT_PATH);
//	}
//
//	public RawClient(HttpClient httpClient, String host, int port, String path) {
//		this(new DefaultHttpTransport(httpClient), host, port, path);
//	}

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
