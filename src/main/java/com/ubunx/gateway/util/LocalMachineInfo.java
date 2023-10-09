package com.ubunx.gateway.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 本机信息
 */
public class LocalMachineInfo {

    public static String getIpAddress() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostAddress();
        } catch (UnknownHostException ex) {
            return "";
        }
    }
}
