package net;

import cli.ConsoleInterface;
import cli.InvalidCommandLineArgumentException;
import cli.UnknownCommandException;
import collection.CollectionElement;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Client implements Runnable, Closeable {
    public static void main(String[] args) {
        try (Client client = new Client(args)) {
            client.run();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidCommandLineArgumentException e) {
            System.out.println("Usage: client <address> <port>");
            System.out.println("<address> -- inet address of server");
            System.out.println("<port> -- port of server. Integer between 1024 and 65 535");
            System.err.println(e.getMessage());
        }
    }

    private static boolean shouldRun = true;
    private MessageProcessor messageProcessor = new MessageProcessor();
    private static DatagramSocket socket;
    private InetAddress address;

    private int port;

    private boolean loggedIn = false;
    private String login = "";
    private String password = "";
    private String hashPassword = "";
    private static String token = "";
    private static SocketAddress myAddress;

    private static ArrayList<CollectionElement> elements = new ArrayList<>();

    public static boolean getShouldRun() {
        return shouldRun;
    }

    public Client(String[] args) throws IOException, InvalidCommandLineArgumentException {
        if (args.length < 2) {
            throw new InvalidCommandLineArgumentException();
        }

        address = InetAddress.getByName(args[0]);

        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            throw new InvalidCommandLineArgumentException();
        }

        if (port < 1024 || port > 65_535) {
            throw new InvalidCommandLineArgumentException();
        }

        socket = new DatagramSocket();
        socket.setSoTimeout(10_000);

        messageProcessor.setMessageProcessor(PacketMessage.Head.INFO, msg -> System.out.println(msg.getBody()));
        messageProcessor.setMessageProcessor(PacketMessage.Head.SHOW, msg -> {
            if (msg.getBody() instanceof List) {
                List list = (List) msg.getBody();
                list.forEach(System.out::println);
            }
        });
    }

    public void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            ConsoleInterface authContext = new ConsoleInterface(scanner);
//            authContext.setCommand("login", line -> sendMessage(loginMessage(line)));
//            authContext.setCommand("register", line -> sendMessage(registerMessage(line)));

            ConsoleInterface defaultContext = new ConsoleInterface(scanner);
            defaultContext.setCommand("exit", line -> shouldRun = false);
            defaultContext.setCommand("stop",
                    line -> sendMessage(new PacketMessage(PacketMessage.Head.STOP, null, login, token, myAddress)));
            defaultContext.setCommand("info",
                    line -> sendMessage(new PacketMessage(PacketMessage.Head.INFO, null, login, token, myAddress)));
            defaultContext.setCommand("remove_first",
                    line -> sendMessage(new PacketMessage(PacketMessage.Head.REMOVE_FIRST, null, login, token, myAddress)));
            defaultContext.setCommand("remove_last",
                    line -> sendMessage(new PacketMessage(PacketMessage.Head.REMOVE_LAST, null, login, token, myAddress)));
            defaultContext.setCommand("add",
                    line -> sendMessage(messageWithElement(PacketMessage.Head.ADD, line)));
            defaultContext.setCommand("remove",
                    line -> sendMessage(messageWithElement(PacketMessage.Head.REMOVE, line)));
            defaultContext.setCommand("show",
                    line -> sendMessage(new PacketMessage(PacketMessage.Head.SHOW, null, login, token, myAddress)));
            defaultContext.setCommand("load",
                    line -> sendMessage(new PacketMessage(PacketMessage.Head.LOAD, null, login, token, myAddress)));
            defaultContext.setCommand("save",
                    line -> sendMessage(new PacketMessage(PacketMessage.Head.SAVE, null, login, token, myAddress)));
            defaultContext.setCommand("import",
                    line -> sendMessage(importMessage(line)));
            defaultContext.setCommand("logout", line -> loggedIn = false);

            while (shouldRun) {
                try {
                    ConsoleInterface currentContext = loggedIn ? defaultContext : authContext;
                    if (!loggedIn) printLoginMessage();
                    currentContext.execNextLine();
                } catch (UnknownCommandException e) {
                    System.err.println(e.getMessage());
                } catch (NoSuchElementException ignored) {
                    shouldRun = false;
                }
            }
        }
    }

    private void printLoginMessage() {
        System.out.println("Type:\n" +
                "login 'email' to authorize or\n" +
                "register 'email' to register (password will be sent to the email)");
    }

    @Override
    public void close() {
        socket.close();
    }

    private void sendMessage(PacketMessage packetMessage) {
        if (packetMessage == null) {
            return;
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream oo = new ObjectOutputStream(outputStream)) {
            oo.writeObject(packetMessage);
        } catch (IOException ignored) {
        }

        byte[] sendBytes = outputStream.toByteArray();
        DatagramPacket sendPacket = new DatagramPacket(sendBytes, sendBytes.length, address, port);

        try {
            socket.send(sendPacket);
        } catch (IOException e) {
            System.err.println("Could not send message to server");
            return;
        }
    }

    public static PacketMessage getNext() {
        byte[] receiveBytes = new byte[0x10000];
        DatagramPacket receivePacket = new DatagramPacket(receiveBytes, receiveBytes.length);

        try {
            socket.receive(receivePacket);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(receiveBytes);
            try (ObjectInputStream oi = new ObjectInputStream(inputStream)) {
                Object obj = oi.readObject();
                if (obj instanceof PacketMessage) {
                    return (PacketMessage) obj;
                }
            } catch (ClassNotFoundException ignored) {
            }
        } catch (IOException e) {
            System.err.println("Could not get response from server");
        }
        return new PacketMessage(PacketMessage.Head.ERROR, "Something went wrong");
    }

    private PacketMessage messageWithElement(PacketMessage.Head head, String line) {
        throw new RuntimeException("Unimplemented");
//        CollectionElement element = new CollectionElement();
//        return new PacketMessage(head, element, login, token, myAddress);
    }

    private PacketMessage importMessage(String line) {
        try {
            String str = new String(Files.readAllBytes(new File(line.trim()).toPath()));
            return new PacketMessage(PacketMessage.Head.IMPORT, str);
        } catch (IOException | InvalidPathException e) {
            System.err.println("Could not read file: " + e.getMessage());
            return null;
        }
    }

    public static void setAddress(SocketAddress myNewAddress) {
        myAddress = myNewAddress;
    }

    static void setToken(String acToken) {
        token = acToken;
    }

    //TODO show error message
    static void loginError(String error_message) {

    }

    static void setElements(ArrayList<CollectionElement> e) {
        elements = e;
    }
}
