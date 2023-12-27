package frames;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class PlaceholderTextField extends JTextField {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String placeholder;

    public PlaceholderTextField(String placeholder) {
        this.placeholder = placeholder;

        // Add focus listener to restore placeholder text when field loses focus and is empty
        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (getText().equals(placeholder)) {
                    setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (getText().isEmpty()) {
                    setText(placeholder);
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (getText().isEmpty() && !isFocusOwner()) {
            Font originalFont = g.getFont();
            Font newFont = originalFont.deriveFont(Font.ITALIC);
            g.setFont(newFont);
            g.setColor(Color.BLACK);
            int x = getInsets().left;
            int y = (getHeight() - g.getFontMetrics().getHeight()) / 2 + g.getFontMetrics().getAscent();
            g.drawString(placeholder, x, y);
            g.setFont(originalFont);
        }
    }
}
