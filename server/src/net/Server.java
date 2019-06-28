package net;

import cli.InvalidCommandLineArgumentException;
import collection.CollectionElement;
import db.Database;
import db.PostgreSQLDatabase;
import utils.Utils;

import javax.mail.*;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.sql.SQLException;
import java.util.*;

public class Server implements Runnable, AutoCloseable {
    private ArrayList<SocketAddress> users = new ArrayList<>();

    public ArrayList<SocketAddress> getUsers() { return users; }

    public static void main(String[] args) {
        try (Server server = new Server(args)) {
            server.run();
        } catch (InvalidCommandLineArgumentException e) {
            System.out.println("Usage: server <port> <uri> <user>");
            System.out.println("<port> -- integer between 1024 and 65 535");
            System.out.println("<uri> -- URI of the database");
            System.out.println("<user> -- login for localhost database");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean shouldRun = true;

    private final Database database;
    private DatagramChannel channel;

    public Database getDatabase() { return database; }

    public Server(String[] args) throws IOException, SQLException, InvalidCommandLineArgumentException {
        if (args.length < 3) {
            throw new InvalidCommandLineArgumentException();
        }

        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            throw new InvalidCommandLineArgumentException();
        }

        if (port < 1024 || port > 65_535) {
            throw new InvalidCommandLineArgumentException();
        }

        String password = new String(System.console().readPassword("Password: "));
//        String password = "root";
        database = new PostgreSQLDatabase(args[1], args[2], password);

        channel = DatagramChannel.open();
        channel.bind(new InetSocketAddress(port));
    }

    @Override
    public void close() throws Exception {
        channel.close();
        database.close();
    }

    public void run() {
        Shower shower = new Shower();
        shower.setDefaults(this, database);
        shower.run();
        MessageProcessor messageProcessor = new MessageProcessor();
        messageProcessor.setMessageProcessor(PacketMessage.Head.EMAIL_LOGIN, msg -> genToken(msg));
        messageProcessor.setMessageProcessor(PacketMessage.Head.TOKEN_LOGIN, msg -> authorize(msg));
        messageProcessor.setMessageProcessor(PacketMessage.Head.INFO, this::infoMessage);
        messageProcessor.setMessageProcessor(PacketMessage.Head.REMOVE_FIRST, msg -> {
            if (database.removeFirst(database.getUserId(msg.getLogin(), msg.getToken()))) {
                sendMessage(msg.getAddress(), new PacketMessage(PacketMessage.Head.REMOVE_FIRST_OK));
                showMessage(msg);
                return;
            }
            sendMessage(msg.getAddress(), new PacketMessage(PacketMessage.Head.REMOVE_FIRST_ERROR));
        });
        messageProcessor.setMessageProcessor(PacketMessage.Head.REMOVE_LAST, msg -> {
            if (database.removeLast(database.getUserId(msg.getLogin(), msg.getToken()))) {
                sendMessage(msg.getAddress(), new PacketMessage(PacketMessage.Head.REMOVE_LAST_OK));
                showMessage(msg);
                return;
            }
            sendMessage(msg.getAddress(), new PacketMessage(PacketMessage.Head.REMOVE_LAST_ERROR));
        });
        messageProcessor.setMessageProcessor(PacketMessage.Head.ADD, msg -> {
            if (msg.getBody() instanceof CollectionElement) {
                database.addElement((CollectionElement) msg.getBody(),
                        database.getUserId(msg.getLogin(), msg.getToken()));
                sendMessage(msg.getAddress(), new PacketMessage(PacketMessage.Head.ADD_OK));
                ArrayList<CollectionElement> list = database.show(database.getUserId(msg.getLogin(), msg.getToken()));
                list.sort(CollectionElement::compareTo);
                for (SocketAddress current : users) {
                    sendMessage(current, new PacketMessage(PacketMessage.Head.SHOW, list));
                }
                return;
            }
            sendMessage(msg.getAddress(), new PacketMessage(PacketMessage.Head.ADD_ERROR));

        });
        messageProcessor.setMessageProcessor(PacketMessage.Head.REMOVE, msg -> {
            if (msg.getBody() instanceof CollectionElement) {
                if (database.removeElement((CollectionElement) msg.getBody(),
                        database.getUserId(msg.getLogin(), msg.getToken()))) {
                    sendMessage(msg.getAddress(), new PacketMessage(PacketMessage.Head.REMOVE_OK));
                    showMessage(msg);
                    return;
                }
            }
            sendMessage(msg.getAddress(), new PacketMessage(PacketMessage.Head.REMOVE_ERROR));
        });
        messageProcessor.setMessageProcessor(PacketMessage.Head.SHOW, this::showMessage);
        messageProcessor.setMessageProcessor(PacketMessage.Head.STOP, msg -> shouldRun = false);

        while (shouldRun) {
            ByteBuffer buffer = ByteBuffer.allocate(0x10000);
            SocketAddress remoteAddress;

            try {
                remoteAddress = channel.receive(buffer);
                addUser(remoteAddress);
            } catch (IOException e) {
                continue;
            }

            byte[] bytes = buffer.array();
            InputStream inputStream = new ByteArrayInputStream(bytes);

            PacketMessage request;

            try (ObjectInputStream oi = new ObjectInputStream(inputStream)) {
                Object obj = oi.readObject();
                if (obj instanceof PacketMessage) {
                    request = (PacketMessage) obj;
                    request.setAddress (remoteAddress);
                    messageProcessor.process(request);
                    System.out.println("Here --> " + request.toString());
                } else {
                    continue;
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    public void sendMessage(SocketAddress receiver, PacketMessage sendingMessage) {
        new Thread(() -> {
            MessageProcessor messageProcessor = new MessageProcessor();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (ObjectOutputStream oo = new ObjectOutputStream(outputStream)) {
                oo.writeObject(sendingMessage);
                channel.send(ByteBuffer.wrap(outputStream.toByteArray()), receiver);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).run();
    }

    private void addUser(SocketAddress remoteAddress) {
        for (SocketAddress address : users) {
            if (address.equals(remoteAddress)) {
                return;
            }
        }
        users.add(remoteAddress);
    }

    private void infoMessage(PacketMessage msg) {
//        sendMessage(msg.getAddress(),
//                    new PacketMessage(PacketMessage.Head.INFO, database.info(database.getUserId(msg.getLogin(), msg.getToken()))));
    }

    private void showMessage(PacketMessage msg) {
        ArrayList<CollectionElement> list = database.show(database.getUserId(msg.getLogin(), msg.getToken()));
        list.sort(CollectionElement::compareTo);
        for (SocketAddress user : users) {
            sendMessage(user, new PacketMessage(PacketMessage.Head.SHOW, list));
        }
    }

    private char getRndChar(Random rnd) {
        int base = rnd.nextInt(63);
        return (char) ('0' + base % 10);
    }

    // Generate random password for user
    private void genToken(PacketMessage msg) {
        String email = msg.getLogin();
        Random rnd = new Random();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            password.append(getRndChar(rnd));
        }
        if (database.consistsUser(email)) {
            sendMessage(msg.getAddress(),
                    new PacketMessage(PacketMessage.Head.EMAIL_OK));
            try {
                sendUserPassword(email, password.toString(), true);
            } catch (MessagingException e) {
                e.printStackTrace();
                sendMessage(msg.getAddress(),
                        new PacketMessage(PacketMessage.Head.EMAIL_ERROR, "Error with sending email, check it or try later", true));
            }
            return;
        }

        try {
            sendUserPassword(email, password.toString());
        } catch (MessagingException e) {
            e.printStackTrace();
            sendMessage(msg.getAddress(),
                    new PacketMessage(PacketMessage.Head.EMAIL_ERROR, "Error with sending email, check it or try later", true));
        }
        sendMessage(msg.getAddress(),
                new PacketMessage(PacketMessage.Head.EMAIL_OK));
    }

    private void sendPasswordMail(String passwordToSend, String receiverAddress) throws MessagingException {
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(prop,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                "itmop3113lab7bot@gmail.com",
                                "p3113lab7bot");
                    }
                });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress("from@gmail.com"));
        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(receiverAddress)
        );

        message.setSubject("Password for Lab7");
        message.setText(passwordToSend);
        try {
            Transport.send(message);
        } catch (Exception skip) {
            System.out.println("Password --> " + passwordToSend);
        }
    }

    // Send password to email from gmail
    private void sendUserPassword(String email, String password) throws MessagingException {
        sendPasswordMail(password, email);
        database.addUser(email, password);
    }


    private void sendUserPassword(String email, String password, boolean change) throws MessagingException {
        sendPasswordMail(password, email);
        database.setNewToken(email, password);
    }

    // Return true if user successfully authorized
    private void authorize(PacketMessage msg) {
        String email = msg.getLogin();
        String password = msg.getToken();
        if (database.checkUser(email, password)) {
            sendMessage(msg.getAddress(),
                        new PacketMessage(PacketMessage.Head.LOGIN_OK, msg.getToken()));
        } else {
            System.out.println(password);
            sendMessage(msg.getAddress(),
                        new PacketMessage(PacketMessage.Head.LOGIN_ERROR, "Invalid access token", true));
        }
    }
}
