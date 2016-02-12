package org.ict4h.atomfeed.client.repository.datasource;

import org.ict4h.atomfeed.client.AtomFeedProperties;
import org.ict4h.atomfeed.spring.resource.EventResource;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HttpClientStub implements HttpClient {
    private EventResource eventResource;
    private HttpServletRequest httpServletRequest;

    public HttpClientStub(EventResource eventResource) {
        this.eventResource = eventResource;
        httpServletRequest = mock(HttpServletRequest.class);
    }

    @Override
    public String fetch(URI uri, AtomFeedProperties atomFeedProperties, Map<String, String> clientCookies) {
    	when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer(uri.toString()));
        String feedId = uri.getPath().substring(uri.getPath().lastIndexOf("/") + 1);
        try {
            return eventResource.getEventFeed(httpServletRequest, Integer.valueOf(feedId));
        } catch (NumberFormatException e) {
            return eventResource.getRecentEventFeed(httpServletRequest);
        }
    }
}