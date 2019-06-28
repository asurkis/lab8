package client;

import net.NetClient;
import net.PacketMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainWindow extends JFrame {
    private Main main;
    private JMenuBar menuBar = new JMenuBar();

    private JMenu modifyCollectionMenu = new JMenu();
    private JMenuItem createMenuItem = new JMenuItem();
    private JMenuItem deleteFirstMenuItem = new JMenuItem();
    private JMenuItem deleteLastMenuItem = new JMenuItem();
    private JMenu langMenu = new JMenu();
    private List<JMenuItem> langMenuItems = new ArrayList<>();

    private MainCanvas canvas;
    private JTable table;

    private String connectionErrorMessage = "";
    private String removeFirstErrorMessage = "";
    private String removeLastErrorMessage = "";
    private String removeFirstOkMessage = "";
    private String removeLastOkMessage = "";


    public MainWindow(Main main) {
        this.main = main;
        canvas = new MainCanvas(main);
        table = new JTable(main.getTableModel());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        modifyCollectionMenu.add(createMenuItem);
        modifyCollectionMenu.add(deleteFirstMenuItem);
        modifyCollectionMenu.add(deleteLastMenuItem);
        menuBar.add(modifyCollectionMenu);
        menuBar.add(langMenu);
        setJMenuBar(menuBar);

        createMenuItem.addActionListener(this::createMenuItemAction);
        deleteFirstMenuItem.addActionListener(this::deleteFirstMenuItemAction);

        setLayout(new GridLayout(1, 2));
        add(canvas);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane);
    }

    public void updateLocale(ResourceBundle bundle) {
        modifyCollectionMenu.setText(bundle.getString("menu.modifyCollection"));
        langMenu.setText(bundle.getString("menu.selectLanguage"));
        createMenuItem.setText(bundle.getString("menu.item.create"));
        deleteFirstMenuItem.setText(bundle.getString("menu.item.delete.first"));
        deleteLastMenuItem.setText(bundle.getString("menu.item.delete.last"));
        connectionErrorMessage = bundle.getString("message.error.connection");
        removeFirstErrorMessage = bundle.getString("message.error.removeFirst");
        removeLastErrorMessage = bundle.getString("message.error.removeLast");
        removeFirstOkMessage = bundle.getString("message.ok.removeFirst");
        removeLastOkMessage = bundle.getString("message.ok.removeLast");

        langMenu.removeAll();
        String[] langCodeKeys = bundle.getString("lang.list").split(",");
        for (String langCode : langCodeKeys) {
            JMenuItem item = new JMenuItem(bundle.getString("lang.name." + langCode));
            langMenu.add(item);
        }

        for (int i = 0; i < 5; i++) {
            table.getTableHeader().getColumnModel().getColumn(i).setHeaderValue(main.getTableModel().getColumnName(i));
        }
        table.getTableHeader().invalidate();
        pack();
    }

    public JTable getTable() {
        return table;
    }

    public void createMenuItemAction(ActionEvent event) {
        main.getCreationDialog().setVisible(true);
    }

    public void deleteFirstMenuItemAction(ActionEvent event) {
        NetClient client = main.getClient();
        client.sendMessage(PacketMessage.Head.REMOVE_FIRST, null);
        client.setSoTimeout(10_000);
        PacketMessage response;
        PacketMessage.Head head = null;
        do {
            response = client.awaitMessage();
            head = response != null ? response.getHead() : null;
        } while (head != null && head != PacketMessage.Head.REMOVE_FIRST_OK && head != PacketMessage.Head.REMOVE_FIRST_ERROR);

        if (head == null) {
            JOptionPane.showMessageDialog(this, connectionErrorMessage, "", JOptionPane.ERROR_MESSAGE);
        } else if (head == PacketMessage.Head.REMOVE_FIRST_ERROR) {
            JOptionPane.showMessageDialog(this, removeFirstErrorMessage, "", JOptionPane.ERROR_MESSAGE);
        } else if (head == PacketMessage.Head.REMOVE_FIRST_OK) {
            JOptionPane.showMessageDialog(this, removeFirstOkMessage, "", JOptionPane.PLAIN_MESSAGE);
        }
    }

    public void deleteLastMenuItemAction(ActionEvent event) {
        NetClient client = main.getClient();
        client.sendMessage(PacketMessage.Head.REMOVE_LAST, null);
        client.setSoTimeout(10_000);
        PacketMessage response;
        PacketMessage.Head head = null;
        do {
            response = client.awaitMessage();
            head = response != null ? response.getHead() : null;
        } while (head != null && head != PacketMessage.Head.REMOVE_LAST_OK && head != PacketMessage.Head.REMOVE_LAST_ERROR);

        if (head == null) {
            JOptionPane.showMessageDialog(this, connectionErrorMessage, "", JOptionPane.ERROR_MESSAGE);
        } else if (head == PacketMessage.Head.REMOVE_LAST_ERROR) {
            JOptionPane.showMessageDialog(this, removeLastErrorMessage, "", JOptionPane.ERROR_MESSAGE);
        } else if (head == PacketMessage.Head.REMOVE_LAST_OK) {
            JOptionPane.showMessageDialog(this, removeLastOkMessage, "", JOptionPane.PLAIN_MESSAGE);
        }
    }
}
