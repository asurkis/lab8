package client;

import collection.CollectionElement;
import net.NetClient;
import net.PacketMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

public class CreationDialog extends JDialog {
    private Main main;

    private JLabel nameLabel = new JLabel();
    private JLabel sizeLabel = new JLabel();
    private JLabel posXLabel = new JLabel();
    private JLabel posYLabel = new JLabel();
    private JTextField nameField = new JTextField();
    private JTextField sizeField = new JTextField();
    private JTextField posXField = new JTextField();
    private JTextField posYField = new JTextField();

    private JLabel createLabel = new JLabel();
    private JButton addButton = new JButton();
    private JButton resetButton = new JButton();

    private String connectionErrorMessage = "";
    private String addErrorMessage = "";

    public CreationDialog(Main main) {
        super(main.getMainWindow(), true);
        this.main = main;

        Box verticalBox = Box.createVerticalBox();
        verticalBox.add(createLabel);

        JPanel panel = new JPanel(new GridLayout(2, 4));
        panel.add(nameLabel);
        panel.add(nameField);
        panel.add(sizeLabel);
        panel.add(sizeField);
        panel.add(posXLabel);
        panel.add(posXField);
        panel.add(posYLabel);
        panel.add(posYField);
        verticalBox.add(panel);

        Box horizontalBox = Box.createHorizontalBox();
        horizontalBox.add(addButton);
        horizontalBox.add(Box.createHorizontalGlue());
        horizontalBox.add(resetButton);

        resetButton.addActionListener(this::resetButtonAction);
        addButton.addActionListener(this::addButtonAction);

        verticalBox.add(horizontalBox);

        add(verticalBox);
    }

    public void updateLocale(ResourceBundle bundle) {
        nameLabel.setText(bundle.getString("table.model.name"));
        sizeLabel.setText(bundle.getString("table.model.size"));
        posXLabel.setText(bundle.getString("table.model.posX"));
        posYLabel.setText(bundle.getString("table.model.posY"));
        addButton.setText(bundle.getString("table.model.createButton"));
        resetButton.setText(bundle.getString("table.model.resetButton"));
        connectionErrorMessage = bundle.getString("message.error.connection");
        addErrorMessage = bundle.getString("message.error.add");

        pack();
        setLocationRelativeTo(getOwner());
        setLocale(bundle.getLocale());
    }

    public void resetButtonAction(ActionEvent event) {
        nameField.setText("");
        sizeField.setText("");
        posXField.setText("");
        posYField.setText("");
    }

    public void addButtonAction(ActionEvent event) {
        nameField.setEnabled(false);
        sizeField.setEnabled(false);
        posXField.setEnabled(false);
        posYField.setEnabled(false);
        String name = nameField.getText();
        double size = Double.parseDouble(sizeField.getText());
        double posX = Double.parseDouble(posXField.getText());
        double posY = Double.parseDouble(posYField.getText());
        CollectionElement toAdd = new CollectionElement(name, size, posX, posY);
        NetClient client = main.getClient();
        client.sendMessage(PacketMessage.Head.ADD, toAdd);
        client.setSoTimeout(10_000);
        PacketMessage response;
        PacketMessage.Head head = null;
        do {
            response = client.awaitMessage();
            head = response != null ? response.getHead() : null;
        } while (head != null && head != PacketMessage.Head.ADD_OK && head != PacketMessage.Head.ADD_ERROR);

        if (head == null) {
            JOptionPane.showMessageDialog(this, connectionErrorMessage, "", JOptionPane.ERROR_MESSAGE);
        } else if (head == PacketMessage.Head.ADD_ERROR) {
            JOptionPane.showMessageDialog(this, addErrorMessage, "", JOptionPane.ERROR_MESSAGE);
        } else if (head == PacketMessage.Head.ADD_OK) {
            nameField.setText("");
            sizeField.setText("");
            posXField.setText("");
            posYField.setText("");
        }
        nameField.setEnabled(true);
        sizeField.setEnabled(true);
        posXField.setEnabled(true);
        posYField.setEnabled(true);
    }
}
