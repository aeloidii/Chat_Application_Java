package frames;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.border.EmptyBorder;

public class NicknameDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private JTextField nicknameField;
    private boolean nicknameEntered;

    public NicknameDialog(Frame parent) {
        super(parent, "Enter Your username", true);

        nicknameField = new PlaceholderTextField("  username");
        nicknameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                	String enteredNickname = nicknameField.getText().trim();
                    if (!enteredNickname.isEmpty() && !enteredNickname.equalsIgnoreCase("username")) {
                        nicknameEntered = true;
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(NicknameDialog.this, "Nickname cannot be empty. Please enter a valid nickname.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        nicknameField.setFont(new Font("Arial", Font.PLAIN, 14));
        nicknameField.setBorder(BorderFactory.createLineBorder(new Color(52, 152, 219), 1));
        nicknameField.setPreferredSize(new Dimension(200, 30));

        JButton okButton = new JButton("OK");
        okButton.setFont(new Font("Arial", Font.BOLD, 14));
        okButton.setForeground(Color.WHITE);
        okButton.setBackground(new Color(52, 152, 219));
        okButton.setFocusPainted(false);
        okButton.setBorderPainted(false);
        ImageIcon emojiIcon = new ImageIcon("C:\\Users\\ABDESSAMAD EL OIDII\\eclipse-workspace\\Chat_Application\\src\\imgs\\icons8-user-100.png");
        setIconImage(emojiIcon.getImage());

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String enteredNickname = nicknameField.getText().trim();
                if (!enteredNickname.isEmpty() && !enteredNickname.equalsIgnoreCase("username")) {
                    nicknameEntered = true;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(NicknameDialog.this, "Nickname cannot be empty. Please enter a valid nickname.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        inputPanel.add(nicknameField);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(inputPanel, BorderLayout.CENTER);
        panel.add(okButton, BorderLayout.SOUTH);

        add(panel);

        setSize(300, 150);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
    }

    public String getNickname() {
        return nicknameField.getText().trim();
    }

    public boolean isNicknameEntered() {
        return nicknameEntered;
    }
}
