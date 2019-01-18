package com.vip.pallas.search.netty;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.vip.pallas.search.netty.http.NettyPallasRequest;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import junit.framework.TestCase;

/**
 * Created by owen on 22/11/2017.
 */
public class NettyPallasRequestTest extends TestCase {

    @Test
    public void testGetParameterMap() {

        String s = "title=test&sub%5B%5D=1&sub%5B%5D=2&sub%5B%5D=3";

        ByteBuf buf = Unpooled.copiedBuffer(s.getBytes());

        DefaultFullHttpRequest originRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "http://localhost/abc?name=Alice", buf, false);
        originRequest.headers().add("Content-Type", "application/x-www-form-urlencoded");

        NettyPallasRequest request = new NettyPallasRequest(originRequest, null);
        Map<String, List<String>> parameterMap = request.getParameterMap();
        assertTrue(parameterMap.containsKey("title"));
        assertTrue(request.isPostFormBody());

    }

    @Test
    public void testAddParameterByUrlAndKey() {
        String s = "title=test&sub%5B%5D=1&sub%5B%5D=2&sub%5B%5D=3";

        ByteBuf buf = Unpooled.copiedBuffer(s.getBytes());

        DefaultFullHttpRequest originRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "http://localhost/abc?name=Alice", buf, false);
        originRequest.headers().add("Content-Type", "application/x-www-form-urlencoded");

        NettyPallasRequest request = new NettyPallasRequest(originRequest, null);
        request.addParamterByUrlAndkey("a=b&c=d", "title");
        Map<String, List<String>> parameterMap = request.getParameterMap();
        assertTrue(parameterMap.containsKey("title"));
        assertTrue(parameterMap.containsKey("a"));
        assertTrue(parameterMap.containsKey("c"));
    }

    @Test
    public void testRemoveParamter() {
        String s = "title=test&sub%5B%5D=1&sub%5B%5D=2&sub%5B%5D=3";

        ByteBuf buf = Unpooled.copiedBuffer(s.getBytes());

        DefaultFullHttpRequest originRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "http://localhost/abc?name=Alice", buf, false);
        originRequest.headers().add("Content-Type", "application/x-www-form-urlencoded");

        NettyPallasRequest request = new NettyPallasRequest(originRequest, null);
        request.removeParamter("title");
        Map<String, List<String>> parameterMap = request.getParameterMap();
        assertFalse(parameterMap.containsKey("title"));
    }

    @Test
    public void testGetParameter() {
        String s = "title=test&sub=1&sub=2&sub=3";

        ByteBuf buf = Unpooled.copiedBuffer(s.getBytes());

        DefaultFullHttpRequest originRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "http://localhost/abc?name=Alice", buf, false);
        originRequest.headers().add("Content-Type", "application/x-www-form-urlencoded");

        NettyPallasRequest request = new NettyPallasRequest(originRequest, null);
        String sub = request.getParameter("sub");
        assertEquals(sub, "1");
    }


    @Test
    public void testGetTemplateName() {
        String s = "{\n" +
                "\t\"id\" : \"msearch_msearch\",\n" +
                "\t\"template\" : \"XXX\"\n" +
                "}";

        ByteBuf buf = Unpooled.copiedBuffer(s.getBytes());

        DefaultFullHttpRequest originRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "http://localhost/abc?name=Alice", buf, false);
        originRequest.headers().add("Content-Type", "application/json").add("X-PALLAS-SEARCH-TEMPLATE-ID", "msearch_msearch");
        NettyPallasRequest request = new NettyPallasRequest(originRequest, null);
        String templateName = request.getTemplateId();
        assertEquals(templateName, "msearch_msearch");
    }

    @Test
    public void testGetString () throws UnsupportedEncodingException {
        String s = "title=test&sub=1&sub=2&sub=3";

        ByteBuf buf = Unpooled.copiedBuffer(s.getBytes());

        DefaultFullHttpRequest originRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "http://localhost/abc?name=Alice", buf, false);
        originRequest.headers().add("Content-Type", "application/x-www-form-urlencoded");

        NettyPallasRequest request = new NettyPallasRequest(originRequest, null);
        String modifiedUri = request.getModifiedUri();
        ByteBuf modifyBodyContent = request.getModifyBodyContent();
        assertTrue(modifiedUri.contains("name=Alice"));
    }



}
