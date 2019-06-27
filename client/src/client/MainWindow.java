package client;

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

    public void createMenuItemAction(ActionEvent e) {
        main.get
    }
}
