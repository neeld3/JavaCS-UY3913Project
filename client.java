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
            socket = new Socket("10.18.185.61", 5190);
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
        JTextArea infoBox = new JTextArea();
        infoBox.setText(" Welcome to Java Bank\n\nLog in or create an account:");

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
                sout.println("LOGIN " + username + " " + password);
                if (sin.hasNextLine()) {
                    String response = sin.nextLine();
                    if (response.equals("SUCCESS")) {
                        accountPage();
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
                sout.println("CREATE " + username + " " + password);
                if (sin.hasNextLine()) {
                    String response = sin.nextLine();
                    if (response.equals("SUCCESS")) {
                        accountPage();
                    }
                }
            }
        });


    }
    
    static void accountPage () {
        jf.getContentPane().removeAll();

        JPanel account_info = new JPanel(new BorderLayout());
        JLabel welcome_mesg = new JLabel("Welcome to your account!");
        welcome_mesg.setFont(new Font("SansSerif", Font.PLAIN, 16));
        account_info.add(welcome_mesg, BorderLayout.NORTH);

        JTextArea display_accounts = new JTextArea();
        display_accounts.setEditable(false);
        display_accounts.setFont(new Font("SansSerif", Font.PLAIN, 16));
        JScrollPane scroll_pane = new JScrollPane(display_accounts);
        account_info.add(scroll_pane, BorderLayout.CENTER);

        sout.println("DISPLAY_ACCOUNTS");
        String all_accounts = "";
        while (sin.hasNextLine()) {
            String line = sin.nextLine();
            if (line.equals("END")) {
                break;
            } 
            all_accounts += line + "\n";
        }

        display_accounts.setText(all_accounts);
        JPanel buttons = new JPanel();
        JButton deposit = new JButton("Deposit Money");
        JButton withdraw = new JButton("Withdraw Money");
        JButton send = new JButton("Send Money");
        JButton transactions = new JButton("View Transactions");
        deposit.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                JPanel depositPanel = new JPanel();
                depositPanel.setLayout(new BoxLayout(depositPanel, BoxLayout.Y_AXIS));
            
                // Account ID
                JPanel accRow = new JPanel(new BorderLayout());
                JLabel accLabel = new JLabel("Account ID:");
                JTextField accField = new JTextField();
                accRow.add(accLabel, BorderLayout.WEST);
                accRow.add(accField, BorderLayout.CENTER);
                accRow.setMaximumSize(new Dimension(300, 40));
            
                // Amount
                JPanel amountRow = new JPanel(new BorderLayout());
                JLabel amountLabel = new JLabel("Amount:");
                JTextField amountField = new JTextField();
                amountRow.add(amountLabel, BorderLayout.WEST);
                amountRow.add(amountField, BorderLayout.CENTER);
                amountRow.setMaximumSize(new Dimension(300, 40));
            
                depositPanel.add(accRow);
                depositPanel.add(amountRow);
            
                int result = JOptionPane.showConfirmDialog(jf, depositPanel, 
                    "Deposit Money", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
                if (result == JOptionPane.OK_OPTION) {
                    String accId = accField.getText();
                    String amount = amountField.getText();
            
                    if (!accId.isEmpty() && !amount.isEmpty()) {
                        sout.println("DEPOSIT " + accId + " " + amount);
                        if (sin.hasNextLine()) {
                            String response = sin.nextLine();
                            if (response.equals("SUCCESS")) {
                                JOptionPane.showMessageDialog(jf, "Deposit successful!");
                                accountPage();
                            } else {
                                JOptionPane.showMessageDialog(jf, "Deposit failed.");
                                accountPage();
                            }
                        }
                    }
                }
            }
        }); 
        withdraw.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                JPanel depositPanel = new JPanel();
                depositPanel.setLayout(new BoxLayout(depositPanel, BoxLayout.Y_AXIS));
            
                // Account ID
                JPanel accRow = new JPanel(new BorderLayout());
                JLabel accLabel = new JLabel("Account ID:");
                JTextField accField = new JTextField();
                accRow.add(accLabel, BorderLayout.WEST);
                accRow.add(accField, BorderLayout.CENTER);
                accRow.setMaximumSize(new Dimension(300, 40));
            
                // Amount
                JPanel amountRow = new JPanel(new BorderLayout());
                JLabel amountLabel = new JLabel("Amount:");
                JTextField amountField = new JTextField();
                amountRow.add(amountLabel, BorderLayout.WEST);
                amountRow.add(amountField, BorderLayout.CENTER);
                amountRow.setMaximumSize(new Dimension(300, 40));
            
                depositPanel.add(accRow);
                depositPanel.add(amountRow);
            
                int result = JOptionPane.showConfirmDialog(jf, depositPanel, 
                    "Withdraw Money", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
                if (result == JOptionPane.OK_OPTION) {
                    String accId = accField.getText();
                    String amount = amountField.getText();
            
                    if (!accId.isEmpty() && !amount.isEmpty()) {
                        sout.println("WITHDRAW " + accId + " " + amount);
                        if (sin.hasNextLine()) {
                            String response = sin.nextLine();
                            if (response.equals("SUCCESS")) {
                                JOptionPane.showMessageDialog(jf, "WITHDRAW successful!");
                                accountPage(); 
                            } else {
                                JOptionPane.showMessageDialog(jf, "WITHDRAW failed.");
                                accountPage();
                            }
                        }
                    }
                }
            }
        });
        send.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                JPanel sendPanel = new JPanel();
                sendPanel.setLayout(new BoxLayout(sendPanel, BoxLayout.Y_AXIS));
            
                // From Account ID
                JPanel fromRow = new JPanel(new BorderLayout());
                JLabel fromLabel = new JLabel("Your Account ID:");
                JTextField fromField = new JTextField();
                fromRow.add(fromLabel, BorderLayout.WEST);
                fromRow.add(fromField, BorderLayout.CENTER);
                fromRow.setMaximumSize(new Dimension(300, 40));
            
                // Recipient User ID
                JPanel userRow = new JPanel(new BorderLayout());
                JLabel userLabel = new JLabel("Recipient User ID:");
                JTextField userField = new JTextField();
                userRow.add(userLabel, BorderLayout.WEST);
                userRow.add(userField, BorderLayout.CENTER);
                userRow.setMaximumSize(new Dimension(300, 40));
            
                // Recipient Account ID
                JPanel toRow = new JPanel(new BorderLayout());
                JLabel toLabel = new JLabel("Recipient Account ID:");
                JTextField toField = new JTextField();
                toRow.add(toLabel, BorderLayout.WEST);
                toRow.add(toField, BorderLayout.CENTER);
                toRow.setMaximumSize(new Dimension(300, 40));
            
                // Amount
                JPanel amountRow = new JPanel(new BorderLayout());
                JLabel amountLabel = new JLabel("Amount:");
                JTextField amountField = new JTextField();
                amountRow.add(amountLabel, BorderLayout.WEST);
                amountRow.add(amountField, BorderLayout.CENTER);
                amountRow.setMaximumSize(new Dimension(300, 40));
            
                sendPanel.add(fromRow);
                sendPanel.add(userRow);
                sendPanel.add(toRow);
                sendPanel.add(amountRow);
            
                int result = JOptionPane.showConfirmDialog(jf, sendPanel, 
                    "Send Money", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
                if (result == JOptionPane.OK_OPTION) {
                    String fromAcc = fromField.getText();
                    String userId = userField.getText();
                    String toAcc = toField.getText();
                    String amount = amountField.getText();
            
                    if (!fromAcc.isEmpty() && !userId.isEmpty() && !toAcc.isEmpty() && !amount.isEmpty()) {
                        sout.println("SEND " + fromAcc + " " + userId + " " + toAcc + " " + amount);
                        if (sin.hasNextLine()) {
                            System.out.println("Here after if next line");
                            String response = sin.nextLine();
                            if (response.equals("SUCCESS")) {
                                System.out.println("Here after success");
                                JOptionPane.showMessageDialog(jf, "Transfer successful!");
                                accountPage(); // Refresh screen
                            } else {
                                System.out.println("Here in else after success");
                                JOptionPane.showMessageDialog(jf, "Transfer failed.");
                                accountPage();
                            }
                        }
                    }
                }
            }
        });

        buttons.add(deposit);
        buttons.add(withdraw);
        buttons.add(send);
        buttons.add(transactions);

        account_info.add(buttons, BorderLayout.SOUTH);
        jf.add(account_info);
        jf.revalidate();
        jf.repaint();
    }
}

