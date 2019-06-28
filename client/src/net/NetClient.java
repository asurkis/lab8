package net;

import client.Main;

import java.io.*;
import java.net.*;

public class NetClient implements AutoCloseable {
    private Main main;

    private DatagramSocket socket = new DatagramSocket();
    private InetAddress address;
    private int port;

    private boolean loggedIn = false;
    private String login = "";
    private String token = "";

    private SocketAddress selfAddress;

    public NetClient(Main main) throws SocketException {
        this.main = main;
        socket.setSoTimeout(1000);
    }

    public void sendMessage(PacketMessage.Head head, Object body) {
        System.err.println("Send: " + head + " with body " + body);
        sendMessage(new PacketMessage(head, body, login, token, selfAddress));
    }

    public void sendMessage(PacketMessage message) {
        if (message == null) {
            return;
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(message);
        } catch (IOException ignored) {
        }

        byte[] sendBytes = byteArrayOutputStream.toByteArray();
        DatagramPacket packet = new DatagramPacket(sendBytes, sendBytes.length, address, port);

        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PacketMessage awaitMessage() {
        byte[] receiveBytes = new byte[0x10000];
        DatagramPacket packet = new DatagramPacket(receiveBytes, receiveBytes.length);

        try {
            socket.receive(packet);
            System.err.println("Received");

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(receiveBytes);
            try (ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
                Object obj = objectInputStream.readObject();
                System.err.println("Parsed: " + obj);
                if (obj instanceof PacketMessage) {
                    return (PacketMessage) obj;
                }
            } catch (ClassNotFoundException ignored) {
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    @Override
    public void close() throws Exception {
        socket.close();
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setSoTimeout(int timeout) {
        try {
            socket.setSoTimeout(timeout);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void setSelfAddress(SocketAddress selfAddress) {
        this.selfAddress = selfAddress;
    }
}
