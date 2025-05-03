import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;


public class client{
    static JFrame jf;
    static JPanel login;
    static JTextField usernameField;
    static JPasswordField passwordField;
    static Socket socket;
    static PrintStream sout;
    static Scanner sin;

    public static void main(String[] args){

        try {
            socket = new Socket("127.0.0.1", 5190);
            sout = new PrintStream(socket.getOutputStream());
            sin = new Scanner(socket.getInputStream());
        } catch (IOException e) {}

        jf = new JFrame("client");
        jf.setSize(1000,500);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        loggingin();
        jf.add(login);
        jf.setVisible(true);   
    }
    static void loggingin(){
        login = new JPanel(new BorderLayout());
        JTextPane infoBox = new JTextPane();
        infoBox.setText(" Welcome to Java Bank\n\nLog in or create an account:");

        StyledDocument doc = infoBox.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);
        infoBox.setText("Welcome to Java Bank\n\nLog in or create an account:");

        infoBox.setEditable(false);
        infoBox.setFont(new Font("SansSerif", Font.BOLD, 20));
        infoBox.setBackground(new Color(238, 238, 238)); 

        login.add(infoBox, BorderLayout.NORTH);
        infoBox.setEditable(false);
        
        JPanel userRow = new JPanel(new BorderLayout());
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        usernameField = new JTextField();
        userRow.add(userLabel, BorderLayout.WEST);
        userRow.add(usernameField, BorderLayout.CENTER);

        JPanel passRow = new JPanel(new BorderLayout());
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        passwordField = new JPasswordField();
        passRow.add(passLabel, BorderLayout.WEST);
        passRow.add(passwordField, BorderLayout.CENTER);

        JPanel loginCenter = new JPanel();
        loginCenter.setLayout(new BoxLayout(loginCenter, BoxLayout.PAGE_AXIS));
        userRow.setMaximumSize(new Dimension(400, 40));
        passRow.setMaximumSize(new Dimension(400, 40));
        loginCenter.add(userRow);
        loginCenter.add(passRow);
        login.add(loginCenter, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton loginButton = new JButton("Log In");
        JButton createAccountButton = new JButton("Create Account");
        loginButton.setPreferredSize(new Dimension(150, 30));
        createAccountButton.setPreferredSize(new Dimension(150, 30));
        buttonPanel.add(loginButton);
        buttonPanel.add(createAccountButton);
        login.add(buttonPanel, BorderLayout.SOUTH);

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                if (username.isEmpty() || password.isEmpty()) {
                    return;
                }
                sout.println("LOGIN" + username + " " + password);
                if (sin.hasNextLine()) {
                    String response = sin.nextLine();
                    if (response.equals("SUCCESS")) {
                        //GO NEXT PAGE
                    }
                }
            }
        });

        createAccountButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                if (username.isEmpty() || password.isEmpty()) {
                    return;
                }
                sout.println("CREATE" + username + " " + password);
                if (sin.hasNextLine()) {
                    String response = sin.nextLine();
                    if (response.equals("SUCCESS")) {
                        //GO NEXT PAGE
                    }
                }
            }
        });


    }

}