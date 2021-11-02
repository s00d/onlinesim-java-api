package ru.onlinesim.transport;

import java.util.Map;

/**
 * @author Vasily Vasilkov (vgv@ecwid.com)
 */
public interface HttpTransport {

	public HttpResponse makeGetRequest(HttpRequest request) throws TransportException;

	public HttpResponse makePutRequest(HttpRequest request) throws TransportException;

	public HttpResponse makeDeleteRequest(HttpRequest request) throws TransportException;

}
