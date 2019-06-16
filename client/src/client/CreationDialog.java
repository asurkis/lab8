package client;

import javax.swing.*;
import java.awt.*;

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

        verticalBox.add(horizontalBox);
    }
}
