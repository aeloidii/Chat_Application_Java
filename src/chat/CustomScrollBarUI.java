package chat;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

// Create a custom ScrollBarUI
public class CustomScrollBarUI extends BasicScrollBarUI {

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return createEmptyButton();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return createEmptyButton();
    }

    private JButton createEmptyButton() {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(0, 0));
        button.setMinimumSize(new Dimension(0, 0));
        button.setMaximumSize(new Dimension(0, 0));
        return button;
    }

    @Override
    protected void configureScrollBarColors() {
        this.thumbColor = new Color(0x121212); // Red
        this.thumbDarkShadowColor = new Color(0x121212); // Dark Red
        this.thumbHighlightColor = new Color(0x121212); // Orange
        this.thumbLightShadowColor = new Color(0x121212); // Orange
        this.trackColor = Color.white; // Blue
    }

    @Override
    protected void installComponents() {
        super.installComponents();
        scrollbar.setLayout(new BorderLayout());
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        Graphics2D g2d = (Graphics2D) g;
        int w = thumbBounds.width;
        int h = thumbBounds.height;

        // Fill the thumb with the thumbColor
        g2d.setColor(thumbColor);
        g2d.fillRect(thumbBounds.x, thumbBounds.y, w, h);

        // Draw the thumb border with the trackColor (to remove the default border)
        g2d.setColor(trackColor);
        g2d.drawRect(thumbBounds.x, thumbBounds.y, w - 1, h - 1);
    }
}