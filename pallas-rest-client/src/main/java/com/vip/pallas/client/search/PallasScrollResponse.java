package com.vip.pallas.client.search;

import org.elasticsearch.client.Response;

public class PallasScrollResponse {
    private Response response;

    private ScrollIterator iterator;

    public PallasScrollResponse(Response response, ScrollIterator iterator) {
        this.response = response;
        this.iterator = iterator;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public ScrollIterator getIterator() {
        return iterator;
    }

    public void setIterator(ScrollIterator iterator) {
        this.iterator = iterator;
    }
}
