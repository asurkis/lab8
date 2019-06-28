package client;

import collection.CollectionElement;
import net.NetClient;

import javax.swing.*;
import java.net.SocketException;
import java.util.*;

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

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        updateLocale(Locale.getDefault());

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
}
