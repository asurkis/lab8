package client;

import net.NetClient;
import net.PacketMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

public class AccessDialog extends JDialog {
    private Main main;
    private String emailToSend = "";

    private JLabel selectedLoginLabel = new JLabel();
    private JTextField selectedLoginField = new JTextField();
    private JLabel codeLabel = new JLabel();
    private JTextField codeField = new JTextField();
    private JButton validateButton = new JButton();
    private JButton resendCodeButton = new JButton();
    private JButton langButton = new JButton();

    private String connectionErrorMessage = "";
    private String tokenErrorMessage = "";

    AccessDialog(Main main) {
        super(main.getMainWindow(), true);

        this.main = main;
        selectedLoginLabel.setHorizontalAlignment(SwingConstants.CENTER);
        selectedLoginField.setPreferredSize(new Dimension(200, selectedLoginField.getPreferredSize().height));
        selectedLoginField.setHorizontalAlignment(SwingConstants.CENTER);
        selectedLoginField.setEditable(false);

        Box verticalBox = Box.createVerticalBox();
        verticalBox.add(selectedLoginLabel);
        verticalBox.add(selectedLoginField);
        verticalBox.add(codeLabel);
        verticalBox.add(codeField);

        Box horizontalBox = Box.createHorizontalBox();
        horizontalBox.add(validateButton);
        horizontalBox.add(langButton);
        verticalBox.add(horizontalBox);

        validateButton.addActionListener(this::validateButtonAction);

        add(verticalBox);
    }

    public void updateLocale(ResourceBundle bundle) {
        selectedLoginLabel.setText(bundle.getString("label.login"));
        codeLabel.setText(bundle.getString("label.code"));
        validateButton.setText(bundle.getString("button.validate"));
        langButton.setText(bundle.getString("button.language"));
        connectionErrorMessage = bundle.getString("message.error.connection");
        tokenErrorMessage = bundle.getString("message.error.email");

        pack();
        setLocationRelativeTo(getOwner());
        setLocale(bundle.getLocale());

    }

    public void setSelectedEmail(String email) {
        selectedLoginField.setText(email);
    }

    private void validateButtonAction(ActionEvent event) {
        validateButton.setEnabled(false);
        langButton.setEnabled(false);
        NetClient client = main.getClient();
        client.setToken(codeField.getText());
        client.sendMessage(PacketMessage.Head.TOKEN_LOGIN, null);
        client.setSoTimeout(10_000);
        PacketMessage response;
        PacketMessage.Head head = null;
        do {
            response = client.awaitMessage();
            head = response != null ? response.getHead() : null;
        } while (head != null && head != PacketMessage.Head.LOGIN_OK && head != PacketMessage.Head.LOGIN_ERROR);

        if (head == null) {
            JOptionPane.showMessageDialog(this, connectionErrorMessage, "", JOptionPane.ERROR_MESSAGE);
        } else if (head == PacketMessage.Head.LOGIN_ERROR) {
            JOptionPane.showMessageDialog(this, tokenErrorMessage, "", JOptionPane.ERROR_MESSAGE);
        } else if (head == PacketMessage.Head.LOGIN_OK) {
            setVisible(false);
        }
        validateButton.setEnabled(true);
        langButton.setEnabled(true);
    }
}
