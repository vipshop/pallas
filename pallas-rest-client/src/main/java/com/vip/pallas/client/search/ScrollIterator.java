package com.vip.pallas.client.search;

import org.elasticsearch.client.Response;

import java.io.IOException;

public interface ScrollIterator {
    boolean hasNext();
    void setScroll(String scroll);
    Response next() throws IOException;
    void close() throws IOException;
}
