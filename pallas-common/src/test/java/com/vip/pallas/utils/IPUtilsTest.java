package com.vip.pallas.utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class IPUtilsTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void testGetHostAddress() {
    assertNull(IPUtils.getHostAddress(null));
    assertEquals("192.168.1.1:255", IPUtils.getHostAddress(
            new InetSocketAddress("192.168.1.1", 255)));
  }

  @Test
  public void testGetHostName() {
    assertEquals("www.google.com", IPUtils.getHostName("www.google.com"));
    assertEquals("www.google.com", IPUtils.getHostName("www.google.com:255"));
  }

  @Test
  public void testGetHostNameAndPort() {
    assertEquals("www.google.com:255", IPUtils.getHostNameAndPort(
            "www.google.com:255"));
  }

  @Test
  public void testGetHostAndPort() {
    assertEquals("192.168.1.1:-1", IPUtils.getHostAndPort(
            "192.168.1.1").toString());
    assertEquals("192.168.1.1:255", IPUtils.getHostAndPort(
            "192.168.1.1:255").toString());
    assertEquals("24:-1", IPUtils.getHostAndPort(
            "192.168.1.1/24").toString());
    assertEquals("null:-1", IPUtils.getHostAndPort("").toString());
  }

  @Test
  public void testGetHostAndPort2() {
    assertEquals("192.168.1.1:255", IPUtils.getHostAndPort(
            new InetSocketAddress("192.168.1.1", 255)).toString());
    final InetSocketAddress inetSocketAddress = null;
    assertEquals("null:-1",
            IPUtils.getHostAndPort(inetSocketAddress).toString());
    assertEquals("127.0.1.1:255", IPUtils.getHostAndPort(
            new InetSocketAddress("127.0.1.1", 255)).toString());
  }

  @Test
  public void testIPV42Integer() {
    assertEquals(new Integer(2130706433), IPUtils.IPV42Integer("127.0.0.1"));
    assertNull(IPUtils.IPV42Integer(null));
  }

  @Test
  public void testInteger2IPV4() {
    assertNull(IPUtils.integer2IPV4(0));
    assertEquals("127.0.0.1", IPUtils.integer2IPV4(2130706433));
  }

  @Test
  public void testIsPortUsing() throws UnknownHostException {
    thrown.expect(UnknownHostException.class);
    assertFalse(IPUtils.isPortUsing("fooBar", 0));
  }
}
