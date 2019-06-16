package client;

import javax.swing.*;
import java.awt.*;
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
        horizontalBox.add(resendCodeButton);
        horizontalBox.add(langButton);
        verticalBox.add(horizontalBox);

        add(verticalBox);
    }

    public void updateLocale(ResourceBundle bundle) {
        selectedLoginLabel.setText(bundle.getString("label.login"));
        codeLabel.setText(bundle.getString("label.code"));
        validateButton.setText(bundle.getString("button.validate"));
        resendCodeButton.setText(bundle.getString("button.resendCode"));
        langButton.setText(bundle.getString("button.language"));

        pack();
        setLocationRelativeTo(getOwner());
        setLocale(bundle.getLocale());

    }

    public void setSelectedEmail(String email) {
        selectedLoginField.setText(email);
    }
}
