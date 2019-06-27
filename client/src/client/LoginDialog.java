package client;

import net.NetClient;
import net.PacketMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

public class LoginDialog extends JDialog {
    private Main main;

    private String ipErrorMessage = "";
    private String portErrorMessage = "";
    private String emailErrorMessage = "";
    private String connectionErrorMessage = "";

    private JLabel loginLabel = new JLabel();
    private JTextField loginField = new JTextField();
    private JLabel serverAddressLabel = new JLabel();
    private JTextField serverAddressField = new JTextField();
    private JLabel serverPortLabel = new JLabel();
    private JTextField serverPortField = new JTextField();
    private JButton loginButton = new JButton();
    private JButton langButton = new JButton();

    LoginDialog(Main main) {
        super(main.getMainWindow(), true);

        this.main = main;
        loginLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loginField.setPreferredSize(new Dimension(200, loginField.getPreferredSize().height));
        loginField.setHorizontalAlignment(SwingConstants.CENTER);

        loginButton.addActionListener(this::loginAction);
        loginField.addActionListener(this::loginAction);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                main.getMainWindow().dispose();
            }
        });

        serverAddressField.setHorizontalAlignment(SwingConstants.CENTER);
        serverPortField.setHorizontalAlignment(SwingConstants.CENTER);

        loginButton.setFocusable(false);
        langButton.setFocusable(false);

        Box verticalBox = Box.createVerticalBox();
        verticalBox.add(loginLabel);
        verticalBox.add(loginField);

        verticalBox.add(serverAddressLabel);
        verticalBox.add(serverAddressField);
        verticalBox.add(serverPortLabel);
        verticalBox.add(serverPortField);

        Box horizontalBox = Box.createHorizontalBox();
        horizontalBox.add(loginButton);
        horizontalBox.add(Box.createHorizontalGlue());
        horizontalBox.add(langButton);
        verticalBox.add(horizontalBox);
        add(verticalBox);
    }

    public void updateLocale(ResourceBundle bundle) {
        loginLabel.setText(bundle.getString("label.login"));
        loginButton.setText(bundle.getString("button.login"));
        langButton.setText(bundle.getString("button.language"));
        serverAddressLabel.setText(bundle.getString("label.server.address"));
        serverPortLabel.setText(bundle.getString("label.server.port"));

        ipErrorMessage = bundle.getString("message.error.ip");
        portErrorMessage = bundle.getString("message.error.port");
        connectionErrorMessage = bundle.getString("message.error.connection");
        emailErrorMessage = bundle.getString("message.error.email");

        pack();
        setLocationRelativeTo(getOwner());
        setLocale(bundle.getLocale());
    }

    private void loginAction(ActionEvent event) {
        loginButton.setEnabled(false);
        loginField.setEnabled(false);
        serverAddressField.setEnabled(false);
        serverPortField.setEnabled(false);

        String email = loginField.getText();
        NetClient client = main.getClient();

        try {
            client.setAddress(InetAddress.getByName(serverAddressField.getText()));
            client.setPort(Integer.parseInt(serverPortField.getText()));
        } catch (UnknownHostException e) {
            JOptionPane.showMessageDialog(this, ipErrorMessage, "", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, portErrorMessage, "", JOptionPane.ERROR_MESSAGE);
        }
        client.setLogin(email);
        client.sendMessage(PacketMessage.Head.EMAIL_LOGIN, null);
        client.setSoTimeout(10_000);
        PacketMessage response;
        PacketMessage.Head head = null;
        do {
            response = client.awaitMessage();
            head = response != null ? response.getHead() : null;
        } while (head != null && head != PacketMessage.Head.SET_ADDRESS && head != PacketMessage.Head.EMAIL_ERROR);

        if (head == null) {
            JOptionPane.showMessageDialog(this, connectionErrorMessage, "", JOptionPane.ERROR_MESSAGE);
        } else if (head == PacketMessage.Head.EMAIL_ERROR) {
            JOptionPane.showMessageDialog(this, emailErrorMessage, "", JOptionPane.ERROR_MESSAGE);
        } else if (head == PacketMessage.Head.EMAIL_OK) {
            main.getAccessDialog().setVisible(true);
            setVisible(false);
        }

        loginButton.setEnabled(true);
        loginField.setEnabled(true);
        serverAddressField.setEnabled(true);
        serverPortField.setEnabled(true);
    }
}
