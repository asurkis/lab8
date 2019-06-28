package client;

import collection.CollectionElement;
import net.ConnectionHandler;
import net.MessageProcessor;
import net.NetClient;

import javax.swing.*;
import java.awt.*;
import java.net.SocketException;
import java.util.*;
import java.util.List;

public class Main implements Runnable {
    public static void main(String[] args) {
        new Main().run();
    }

    private NetClient client;
    private LabTableModel tableModel = new LabTableModel();
    private MainWindow mainWindow = new MainWindow(this);
    private LoginDialog loginDialog = new LoginDialog(this);
    private AccessDialog accessDialog = new AccessDialog(this);
    private CreationDialog creationDialog = new CreationDialog(this);
    private List<Object> collection = new ArrayList<>();
    private MessageProcessor messageProcessor = new MessageProcessor();
    private ConnectionHandler handler = new ConnectionHandler(this, messageProcessor);

    public void updateLocale(Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle("client/text", locale);
        tableModel.updateLocale(bundle);
        mainWindow.updateLocale(bundle);
        mainWindow.invalidate();
        loginDialog.updateLocale(bundle);
        accessDialog.updateLocale(bundle);
        creationDialog.updateLocale(bundle);
    }

    @Override
    public void run() {
        try {
            client = new NetClient(this);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        updateLocale(Locale.getDefault());

        Thread thread = new Thread(handler);
        thread.setDaemon(true);
        thread.start();

        mainWindow.setLocationRelativeTo(null);
        mainWindow.setVisible(true);
        loginDialog.setVisible(true);
    }

    public MainWindow getMainWindow() {
        return mainWindow;
    }

    public LoginDialog getLoginDialog() {
        return loginDialog;
    }

    public AccessDialog getAccessDialog() {
        return accessDialog;
    }

    public List<Object> getCollection() {
        return collection;
    }

    public LabTableModel getTableModel() {
        return tableModel;
    }

    public NetClient getClient() {
        return client;
    }

    public CreationDialog getCreationDialog() { return creationDialog; }

    public ConnectionHandler getHandler() {
        return handler;
    }

    public MessageProcessor getMessageProcessor() {
        return messageProcessor;
    }
}
