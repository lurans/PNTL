package com.huawei.blackhole.network.core.service;


import java.io.IOException;
import java.net.*;

public class SocketClientService extends Thread {
    private DatagramSocket socket;
    private InetAddress hostAddress;
    private byte[] buf = new byte[20];
    private DatagramPacket pkg_receive;
    private DatagramPacket pkg_send;
    private String hostIp;
    private int hostPort;
    private static final int MAX_TRY_NUM = 3;

    public SocketClientService(String ip, int port, String send_msg) {
        hostIp = ip;
        hostPort = port;
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(3000);
            hostAddress = InetAddress.getByName(hostIp);
            pkg_send = new DatagramPacket(send_msg.getBytes(), send_msg.length(), hostAddress, hostPort);
            pkg_receive = new DatagramPacket(buf, buf.length);
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        int tries = 0;
        boolean receivedRsp = false;
        while (!receivedRsp && tries < MAX_TRY_NUM) {
            try {
                socket.send(pkg_send);
                //System.out.println("address="+pkg_send.getAddress() + " tries=" + tries);
                socket.receive(pkg_receive);
                if (!pkg_receive.getAddress().equals(hostAddress)){
                    throw new IOException("receive packet from an unknow source");
                }
                receivedRsp = true;
            } catch (SocketTimeoutException e) {
                tries++;
            } catch (IOException e){
                tries++;
                //e.printStackTrace();
            }
        }
        socket.close();
    }
}
