package client;

import collection.CollectionElement;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class MainCanvas extends JPanel {
    private Main main;

    public MainCanvas(Main main) {
        super(true);
        this.main = main;
        setPreferredSize(new Dimension(640, 640));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics;
        g.setBackground(Color.WHITE);
        g.clearRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.BLACK);
        int w2 = getWidth() / 2;
        int h2 = getHeight() / 2;
        g.translate(w2, h2);
        g.drawLine(-w2, 0, w2, 0);
        g.drawLine(w2, 0, w2 - 10, -5);
        g.drawLine(w2, 0, w2 - 10, 5);

        g.drawLine(0, -h2, 0, h2);
        g.drawLine(0, -h2, 5, 10 - h2);
        g.drawLine(0, -h2, -5, 10 - h2);

        int step = 50;
        Graphics2D stroked = (Graphics2D) g.create();
        stroked.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2}, 0));

        for (int i = -w2 / step * step; i < w2; i += step) {
            stroked.drawLine(i, -h2, i, h2);
            String str = String.format(getLocale(), "%d", i);
            Rectangle2D bounds = g.getFontMetrics().getStringBounds(str, null);
            g.drawString(str, (float) (i - bounds.getWidth() - 1), (float) (bounds.getHeight() - 1));
        }

        for (int i = -h2 / step * step; i < h2; i += step) {
            stroked.drawLine(-w2, i, w2, i);
            String str = String.format(getLocale(), "%d", i);
            Rectangle2D bounds = g.getFontMetrics().getStringBounds(str, null);
            g.drawString(str, (float) (-bounds.getWidth() - 1), (float) (-i + bounds.getHeight() - 1));
        }
        stroked.dispose();

        LocalDateTime now = LocalDateTime.now();

        List<CollectionElement> elements = main.getTableModel().getElements();
        synchronized (elements) {
            for (int i = 0; i < elements.size(); i++) {
                CollectionElement element = elements.get(i);
                LocalDateTime created = element.getCreationDate();
                long millis = ChronoUnit.MILLIS.between(created, now);
                double passed = Math.min(1, millis / (100 * element.getSize()));

                double estimatedSize = element.getSize() * passed;
                g.setColor(main.getMainWindow().getTable().isRowSelected(i) ? Color.GREEN : Color.CYAN);
                int x = (int) (element.getPosition().getX() - estimatedSize);
                int y = (int) (-element.getPosition().getY() - estimatedSize);
                int d = (int) (2 * estimatedSize);
                g.fillOval(x, y, d, d);
                g.setColor(Color.BLACK);
                g.drawOval(x, y, d, d);
            }
        }

        repaint();
    }
}
