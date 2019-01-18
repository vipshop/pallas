/**
 * Copyright 2019 vip.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.vip.pallas.utils;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.vjtools.vjkit.text.MoreStringUtil;

public class IPUtils {
    private static String NETWORK_INTERFACE_NAMES = System.getProperty("pallas.network.interface.names",
            "bond0,eth0,em0,br0,enp2s0f0");

    private static String NETWORK_INTERFACE_SPECIAL_PREFIX = System
            .getProperty("pallas.network.interface.special.prefix", "bond0.");

    private static final Logger logger = LoggerFactory.getLogger(IPUtils.class);

    public static final String LOCALHOST = "127.0.0.1";

    private static InetAddress localInetAddress;
    private static String localHostAddress = LOCALHOST;
    private static int localHostAddressPrefixInteger = IPV42PrefixInteger(LOCALHOST);

    private static final AtomicInteger integer2IPV4MapCounter = new AtomicInteger(); // String形式的IP的缓存计数
    private static final AtomicInteger IPV42IntegerMapCounter = new AtomicInteger(); // 整数形式的IP的缓存计数
    private static final AtomicInteger inetAddressCacheCounter = new AtomicInteger(); // Inet形式的IP的缓存计数
    private static final AtomicInteger hostPortCacheCounter = new AtomicInteger();

    private static final ConcurrentMap<Integer, String> integer2IPV4Map = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Integer> IPV42IntegerMap = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, InetAddress> inetAddressCache = new ConcurrentHashMap<>(256);
    private static final ConcurrentMap<String, HostAndPort> hostPortCache = new ConcurrentHashMap<>(256);

    // 每种cache最大值
    private static final int MAX_CACHE_SIZE = 5000;

    static {
        initLocalIP();
    }

    private static void initLocalIP() {
        try {
            Map<String, NetworkInterface> networkInterfaceMap = new HashMap<String, NetworkInterface>();
            Enumeration<NetworkInterface> networkInterfaceEnum = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaceEnum.hasMoreElements()) {
                NetworkInterface ni = networkInterfaceEnum.nextElement();
                if (ni.isUp()) {
                    String name = ni.getName();
                    if (name.startsWith(NETWORK_INTERFACE_SPECIAL_PREFIX)) {
                        NETWORK_INTERFACE_NAMES = (name + ',').concat(NETWORK_INTERFACE_NAMES);
                    }
                    networkInterfaceMap.put(name, ni);
                }
            }

            try {
                localInetAddress = InetAddress.getLocalHost();
            } catch (UnknownHostException e) {// NOSONAR
                logger.info("The server doesn't config /etc/hosts file. will choose network interface!");
            }

            String selectedNetworkInterface = "";
            if (localInetAddress == null || localInetAddress.getHostAddress() == null
                    || LOCALHOST.equals(localInetAddress.getHostAddress())) {
                String[] networkInterfaceNames = NETWORK_INTERFACE_NAMES.split(",");
                for (String networkInterfaceName : networkInterfaceNames) {
                    NetworkInterface ni = networkInterfaceMap.get(networkInterfaceName);
                    if (ni != null) {
                        Enumeration<InetAddress> ips = ni.getInetAddresses();
                        while (ips.hasMoreElements()) {
                            InetAddress nextElement = ips.nextElement();
                            if (LOCALHOST.equals(nextElement.getHostAddress()) || nextElement instanceof Inet6Address
                                    || nextElement.getHostAddress().contains(":")) {
                                continue;
                            }
                            localInetAddress = nextElement;
                            selectedNetworkInterface = networkInterfaceName;
                        }
                        break;
                    }
                }
            }

            logger.info("we got ip :" + localInetAddress + " from " + selectedNetworkInterface
                    + " within networkinterfaces :" + NETWORK_INTERFACE_NAMES);
            setLocalHostAddress(localInetAddress != null ? localInetAddress.getHostAddress() : null);
        } catch (SocketException e) {
            logger.error("InetAddress.getLocalHost error.", e);
        } catch (Throwable e) {
            logger.error("[init IpUtils error][please configure hostname or networkinterface:]"
                    + NETWORK_INTERFACE_NAMES, e);
        }
    }

    public static InetAddress getAddresses(String ip) {
        InetAddress result = inetAddressCache.get(ip);
        if (result == null) {
            try {
                result = InetAddress.getByName(ip);
            } catch (UnknownHostException e) {
                logger.error("InetAddress.getByName(" + ip + ") error.", e);
                result = localInetAddress;
            }

            if (inetAddressCacheCounter.get() < MAX_CACHE_SIZE) {
                inetAddressCacheCounter.incrementAndGet();
                inetAddressCache.put(ip, result);
            }
        }

        return result;
    }

    private static void setLocalHostAddress(String address) {
        IPUtils.localHostAddress = address;
        IPUtils.localHostAddressPrefixInteger = IPV42PrefixInteger(address);
    }

    public static String integer2IPV4(Integer iIPV4) {
        if (iIPV4 == null || iIPV4 == 0) {
            return null;
        }

        String result = integer2IPV4Map.get(iIPV4);
        if (result != null) {
            return result;
        }

        StringBuilder sb = new StringBuilder(24);
        sb.append(0xff & (iIPV4 >> 24)).append('.').append(0xff & (iIPV4 >> 16)).append('.').append(0xff & (iIPV4 >> 8))
                .append('.').append(0xff & (iIPV4));
        result = sb.toString();

        // 加入cache，如果cache满了就不再增加，只加不减，如果同时put，有可能和MAX_CACHE_SIZE略有误差
        if (integer2IPV4MapCounter.get() < MAX_CACHE_SIZE) {
            integer2IPV4MapCounter.incrementAndGet();
            integer2IPV4Map.put(iIPV4, result);
        }

        return result;
    }

    /*
     * Not support ip:port
     */
    public static Integer IPV42Integer(String strIPV4) {
        if (strIPV4 == null) {
            return null;
        }

        Integer result = IPV42IntegerMap.get(strIPV4);
        if (result != null) {
            return result;
        }
        List<String> it = MoreStringUtil.split(strIPV4, '.', 4);
        if (it.size() != 4) {
            return 0;
        }
        int tempInt;
        byte[] byteAddress = new byte[4];
        for (int i = 0; i < 4; i++) {
            tempInt = Integer.parseInt(it.get(i));
            byteAddress[i] = (byte) tempInt;
        }

        result = ((byteAddress[0] & 0xff) << 24) | ((byteAddress[1] & 0xff) << 16) | ((byteAddress[2] & 0xff) << 8)
                | (byteAddress[3] & 0xff);

        if (IPV42IntegerMapCounter.get() < MAX_CACHE_SIZE) {
            IPV42IntegerMapCounter.incrementAndGet();
            IPV42IntegerMap.put(strIPV4, result);
        }

        return result;
    }

    public static Integer IPV42PrefixInteger(String strIPV4) {
        if (strIPV4 == null) {
            return 0;
        }

        List<String> it = MoreStringUtil.split(strIPV4, '.', 4);
        if (it.size() != 4) {
            return 0;
        }
        byte[] byteAddress = new byte[4];
        for (int i = 0; i < 2; i++) {
            int tempInt = Integer.parseInt(it.get(i));
            byteAddress[i] = (byte) tempInt;
        }

        return ((byteAddress[0] & 0xff) << 8) | (byteAddress[1] & 0xff);
    }

    public static String localIp4Str() {
        return localHostAddress;
    }

    public static int localIp4Prefix() {
        return localHostAddressPrefixInteger;
    }

    public static InetAddress localIp() {
        return localInetAddress;
    }

    /**
     * 分析字符串，返回InetAddress,如果不存在则返回本地地址
     */
    public static InetAddress getInetAddress(String hostName) {
        InetAddress ipAddr = null;
        try {
            ipAddr = hostName != null ? getAddresses(hostName) : IPUtils.localIp();
        } catch (Exception e) {
            logger.warn("hostName format is worng：" + hostName, e);
            ipAddr = IPUtils.localIp();
        }
        return ipAddr;
    }

    /**
     * 从SocketAddress中获取ip:port字符串
     */
    public static String getHostAddress(SocketAddress socketAddress) {
        if (socketAddress == null) {
            return null;
        }
        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
        return inetSocketAddress.getAddress().getHostAddress() + ':' + inetSocketAddress.getPort();
    }

    private static String getHostName(String hostString, int index) {
        String hostName = null;
        if (hostString != null && hostString.length() > 0) {
            if (index != -1) {
                hostName = hostString.substring(0, index);
            } else {
                hostName = hostString;
            }

            if (LOCALHOST.equals(hostName)) {
                return IPUtils.localIp4Str();
            }
        }

        return hostName;
    }

    public static String getHostNameAndPort(String hostString) {
        String hostName = null;
        if (hostString != null && hostString.length() > 0) {
            int index = hostString.indexOf(':');
            hostName = getHostName(hostString, index);
            if (index != -1) {
                hostName += hostString.substring(index);
            }
        }

        return hostName;
    }

    /**
     * 分析字符串，去除字符串中的端口号，转换localhost地址
     */
    public static String getHostName(String hostString) {
        String hostName = null;
        if (hostString != null && hostString.length() > 0) {
            int index = hostString.indexOf(':');
            hostName = getHostName(hostString, index);
        }

        return hostName;
    }

    /**
     * 分析字符串，返回HostAddress与Port，转换localhost地址
     */
    public static HostAndPort getHostAndPort(String hostString) {
        if (hostString == null || hostString.length() == 0) {
            return new HostAndPort();
        }

        HostAndPort hostAndPort = hostPortCache.get(hostString);
        if (hostAndPort == null) {

            hostAndPort = new HostAndPort();
            int index = hostString.indexOf(':');
            if (index != -1) {
                hostAndPort.hostAddress = hostString.substring(0, index);
                hostAndPort.port = Integer.parseInt(hostString.substring(index + 1));
            } else {
                hostAndPort.hostAddress = hostString;
            }

            // 去除旧版InetSocketAddress.toString()产生的/
            index = hostAndPort.hostAddress.indexOf('/');
            if (index != -1) {
                hostAndPort.hostAddress = hostAndPort.hostAddress.substring(index + 1,
                        hostAndPort.hostAddress.length());
            }

            // 将127.0.0.1 替换为机器IP地址(for local proxy)
            if (LOCALHOST.equals(hostAndPort.hostAddress)) {
                hostAndPort.hostAddress = IPUtils.localIp4Str();
            }

            if (hostPortCacheCounter.get() < MAX_CACHE_SIZE) {
                hostPortCacheCounter.incrementAndGet();
                hostPortCache.put(hostString, hostAndPort);
            }
        }

        return hostAndPort;
    }

    public static HostAndPort getHostAndPort(InetSocketAddress socketAddress) {
        HostAndPort hostAndPort = new HostAndPort();

        if (socketAddress == null) {
            return hostAndPort;
        }

        String hostAddress = socketAddress.getAddress().getHostAddress();

        if (LOCALHOST.equals(hostAddress)) {
            hostAddress = localIp4Str();
        }

        hostAndPort.hostAddress = hostAddress;
        hostAndPort.port = socketAddress.getPort();

        return hostAndPort;
    }

    /***
     * true:already in using false:not using
     */
    public static boolean isLocalPortUsing(int port) {
        try {
            return isPortUsing(LOCALHOST, port);
        } catch (Exception e) {// NOSONAR
            logger.info("local host port {} doesn't use", port);
            return false;
        }
    }

    /***
     * true:already in using false:not using
     */
    public static boolean isPortUsing(String host, int port) throws UnknownHostException {
        InetAddress theAddress = InetAddress.getByName(host);
        try (Socket socket = new Socket(theAddress, port);){
            return true;
        } catch (Exception e) {// NOSONAR
            logger.info(host + ':' + port + " doesn't use");
        }
        return false;
    }

    public static class HostAndPort {
        public String hostAddress;
        public int port = -1;

        @Override
        public String toString() {
            return hostAddress + ':' + port;
        }
    }

}