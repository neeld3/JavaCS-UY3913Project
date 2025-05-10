import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;


public class Client{
    static JFrame jf;
    static Socket socket;
    static PrintStream sout;
    static Scanner sin;
    static String current_username = "";

    public static void main(String[] args){
        try {
            socket = new Socket("172.20.10.13", 5190);
            sout = new PrintStream(socket.getOutputStream());
            sin = new Scanner(socket.getInputStream());
        } catch (IOException ex) {
            System.out.println("IOException caught: " + ex.toString());
        }

        jf = new JFrame("client");
        jf.setSize(1000,500);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        change_panel(new login_page());        
        jf.setVisible(true);   
    }
    public static void change_panel(JPanel new_panel){
        jf.getContentPane().removeAll();
        jf.add(new_panel);
        jf.revalidate();
        jf.repaint();
    }
}

class login_page extends JPanel{
    static JTextField username_field;
    static JPasswordField password_field;

    public login_page(){
        setLayout(new BorderLayout());
        JTextPane info_box = new JTextPane();

        // Custom text styling
        StyledDocument doc = info_box.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);

        info_box.setBackground(new Color(220, 230, 245));  
        info_box.setBorder(BorderFactory.createEmptyBorder(60, 10, 20, 10));
        info_box.setText("Welcome to Java Bank!\n\nLog In or Create an Account:");
        info_box.setEditable(false);
        info_box.setFont(new Font("SansSerif", Font.BOLD, 20));
        add(info_box, BorderLayout.NORTH);
        
        // Customizing 'Username' Area
        JPanel username_box = new JPanel(new BorderLayout());
        JLabel username_label = new JLabel("Username:");
        username_label.setFont(new Font("SansSerif", Font.PLAIN, 16));
        username_field = new JTextField();
        username_box.add(username_label, BorderLayout.WEST);
        username_box.add(username_field, BorderLayout.CENTER);
        username_box.setBackground(new Color(220, 230, 245));
        username_box.setMaximumSize(new Dimension(400, 40));
        username_label.setPreferredSize(new Dimension(100, 40));

        // Customizing 'Password' Area
        JPanel password_box = new JPanel(new BorderLayout());
        JLabel password_label = new JLabel("Password:");
        password_label.setFont(new Font("SansSerif", Font.PLAIN, 16));
        password_field = new JPasswordField();
        password_box.add(password_label, BorderLayout.WEST);
        password_box.add(password_field, BorderLayout.CENTER);
        password_box.setBackground(new Color(220, 230, 245));
        password_box.setMaximumSize(new Dimension(400, 40));
        password_label.setPreferredSize(new Dimension(100, 40));

        JPanel login_place = new JPanel();
        login_place.setBackground(new Color(220, 230, 245));
        login_place.setLayout(new BoxLayout(login_place, BoxLayout.PAGE_AXIS));
        login_place.add(username_box);
        login_place.add(password_box);
        login_place.setBorder(BorderFactory.createEmptyBorder(40,0,10,0));
        add(login_place, BorderLayout.CENTER);

        // Creating 'Log In' and 'Create Account' Buttons
        JPanel button_panel = new JPanel();
        JButton login_button = new JButton("Log In");
        JButton create_acc_button = new JButton("Create Account");

        login_button.setPreferredSize(new Dimension(150, 30));
        create_acc_button.setPreferredSize(new Dimension(150, 30));

        button_panel.add(login_button);
        button_panel.add(create_acc_button);
        button_panel.setBorder(BorderFactory.createEmptyBorder(10,0,60,0));
        button_panel.setBackground(new Color(220, 230, 245));

        add(button_panel, BorderLayout.SOUTH);

        login_button.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){sign_in();}});
        create_acc_button.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){create_account();}});
    }
    // Log in
    private void sign_in() {
        String username = username_field.getText();
        String password = new String(password_field.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(Client.jf, "Please enter both username and password.");
            return;
        }
        Client.sout.println("LOGIN " + username + " " + password);
        if (Client.sin.hasNextLine()) {
            String response = Client.sin.nextLine();
            if (response.equals("SUCCESS")) {
                Client.current_username = username;
                Client.change_panel(new account_page());
            } else {
                JOptionPane.showMessageDialog(Client.jf, response);
            }
        }
    }

    private void create_account() {
        String username = username_field.getText();
        String password = new String(password_field.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(Client.jf, "Please enter both username and password.");
            return;
        }
        Client.sout.println("CREATE " + username + " " + password);
        if (Client.sin.hasNextLine()) {
            String response = Client.sin.nextLine();
            if (response.equals("SUCCESS")) {
                Client.current_username = username;
                Client.change_panel(new account_page());
            }else {
                JOptionPane.showMessageDialog(Client.jf, response);
            }
        }
    }
}
class account_page extends JPanel{
    public account_page(){
        setLayout(new BorderLayout());
        setBackground(new Color(220, 230, 245));
        setBorder(BorderFactory.createEmptyBorder(10, 60, 20, 60));

        JLabel title = new JLabel("Account Information");
        title.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        add(title, BorderLayout.NORTH);

        JPanel account_list = new JPanel();
        account_list.setLayout(new BoxLayout(account_list, BoxLayout.Y_AXIS));
        account_list.setBackground(new Color(255, 255, 255));
        account_list.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        Client.sout.println("DISPLAY_ACCOUNTS");
        JLabel welcome_label = new JLabel("Welcome, " + Client.current_username + "!");
        welcome_label.setFont(new Font("SansSerif", Font.BOLD, 18));
        welcome_label.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcome_label.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0)); 

        account_list.add(welcome_label);

        // Displaying accounts' information
        while (Client.sin.hasNextLine()) {
            String line = Client.sin.nextLine();
            String shared_account = null;
            if (line.equals("END")) {
                break;
            }

            String[] info = line.split(" ");
            if (info[0].equals("ACCOUNT")) {
                String account_ID = info[1];
                String balance = info[2];
                if (info.length > 3){
                    String shared_accs = "";
                    for (int i = 4; i < info.length; i++) {
                        shared_accs += info[i];
                    }
                    shared_account = shared_accs;
                }

                JPanel account = new JPanel();
                account.setLayout(new GridLayout(2, 1));
                account.setBackground(new Color(200, 220, 235));
                Border border_line = BorderFactory.createLineBorder(Color.BLACK, 1);
                Border border_padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
                account.setBorder(BorderFactory.createCompoundBorder(border_line, border_padding));
                account.add(new JLabel("Account ID: " + account_ID));
                if (shared_account != null){
                    account.add(new JLabel("Account shared with: " + shared_account));
                }
                account.add(new JLabel("Balance: $" + balance));
                account.setMaximumSize(new Dimension(400, 60));
                account_list.add(Box.createRigidArea(new Dimension(0, 10)));
                account_list.add(account);
            }
        }

        JScrollPane scroll = new JScrollPane(account_list);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 60, 10, 60));
        add(scroll, BorderLayout.CENTER);

        // Creating action buttons
        JPanel buttons = new JPanel();
        buttons.setBackground(new Color(180, 200, 230));
        JButton deposit = new JButton("Deposit Money");
        JButton withdraw = new JButton("Withdraw Money");
        JButton send = new JButton("Send Money");
        JButton transactions = new JButton("View Transactions");

        // Adding action listeners
        deposit.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){deposit();}}); 
        withdraw.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){withdraw();}});
        send.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){send();}});
        transactions.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){transaction_log();}});

        buttons.add(deposit);
        buttons.add(withdraw);
        buttons.add(send);
        buttons.add(transactions);

        add(buttons, BorderLayout.SOUTH);
    }
    //deposit functionality
    private void deposit() {
        JPanel deposit_panel = new JPanel();
        deposit_panel.setLayout(new BoxLayout(deposit_panel, BoxLayout.Y_AXIS));
    
        JPanel account_box = new JPanel(new BorderLayout());
        JLabel account_label = new JLabel("Account ID:");
        JTextField account_field = new JTextField();
        account_label.setPreferredSize(new Dimension(80, 30));
        account_box.add(account_label, BorderLayout.WEST);
        account_box.add(account_field, BorderLayout.CENTER);
        account_box.setPreferredSize(new Dimension(100, 30));

        JPanel amount_box = new JPanel(new BorderLayout());
        JLabel amount_label = new JLabel("Amount:");
        JTextField amount_field = new JTextField();
        amount_label.setPreferredSize(new Dimension(80, 30));
        amount_box.add(amount_label, BorderLayout.WEST);
        amount_box.add(amount_field, BorderLayout.CENTER);
        amount_box.setPreferredSize(new Dimension(100, 30));

        deposit_panel.add(account_box);
        deposit_panel.add(amount_box);
    
        int result = JOptionPane.showConfirmDialog(Client.jf, deposit_panel, "Deposit Money", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    
        if (result == JOptionPane.OK_OPTION) {
            String account_ID = account_field.getText();
            String amount = amount_field.getText();
    
            if (!account_ID.isEmpty() && !amount.isEmpty()) {
                Client.sout.println("DEPOSIT " + account_ID + " " + amount);
                if (Client.sin.hasNextLine()) {
                    String response = Client.sin.nextLine();
                    if (response.equals("SUCCESS")) {
                        JOptionPane.showMessageDialog(Client.jf, "Deposit Successful!");
                    } else {
                        JOptionPane.showMessageDialog(Client.jf, response);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(Client.jf, "Please enter all the required information.");
            }
            Client.change_panel(new account_page());
        }
    }
    // Withdraw Money
    private void withdraw() {
        JPanel withdraw_panel = new JPanel();
        withdraw_panel.setLayout(new BoxLayout(withdraw_panel, BoxLayout.Y_AXIS));
    
        JPanel account_box = new JPanel(new BorderLayout());
        JLabel account_label = new JLabel("Account ID:");
        JTextField account_field = new JTextField();
        account_label.setPreferredSize(new Dimension(80, 30));
        account_box.add(account_label, BorderLayout.WEST);
        account_box.add(account_field, BorderLayout.CENTER);
        account_box.setPreferredSize(new Dimension(100, 30));

        JPanel amount_box = new JPanel(new BorderLayout());
        JLabel amount_label = new JLabel("Amount:");
        JTextField amount_field = new JTextField();
        amount_label.setPreferredSize(new Dimension(80, 30));
        amount_box.add(amount_label, BorderLayout.WEST);
        amount_box.add(amount_field, BorderLayout.CENTER);
        amount_box.setPreferredSize(new Dimension(100, 30));
    
        withdraw_panel.add(account_box);
        withdraw_panel.add(amount_box);
    
        int result = JOptionPane.showConfirmDialog(Client.jf, withdraw_panel, 
            "Withdraw Money", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    
        if (result == JOptionPane.OK_OPTION) {
            String account_ID = account_field.getText();
            String amount = amount_field.getText();
    
            if (!account_ID.isEmpty() && !amount.isEmpty()) {
                Client.sout.println("WITHDRAW " + account_ID + " " + amount);
                if (Client.sin.hasNextLine()) {
                    String response = Client.sin.nextLine();
                    if (response.equals("SUCCESS")) {
                        JOptionPane.showMessageDialog(Client.jf, "Withdraw Successful!");
                    } else {
                        JOptionPane.showMessageDialog(Client.jf, response);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(Client.jf, "Please enter all the required information.");
            }
            Client.change_panel(new account_page());
        }
    }

    // Send Money
    private void send() {
        JPanel send_panel = new JPanel();
        send_panel.setLayout(new BoxLayout(send_panel, BoxLayout.Y_AXIS));

        // Signature Portion
        MyPanel jp = new MyPanel();
        jp.setLayout(new BorderLayout());

        jp.addMouseListener(new MouseListener(){
            public void mouseClicked(MouseEvent e) {
                jp.addPoint(e.getX(), e.getY());
                jp.repaint();
            }
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        });

        jp.addMouseMotionListener(new MouseMotionListener(){
            public void mouseDragged(MouseEvent e) {                
                jp.addPoint(e.getX(), e.getY());
                jp.repaint();
            }
            public void mouseMoved(MouseEvent e) {}
        });

        jp.setPreferredSize(new Dimension(300, 160));
        JButton clear = new JButton("Clear");
        clear.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                jp.clear();
            }
        });

        jp.add(clear, BorderLayout.SOUTH);
        jp.add(new JLabel("Add Signature Below: "), BorderLayout.NORTH);

        JPanel from_box = new JPanel(new BorderLayout());
        JLabel from_label = new JLabel("Your Account ID:");
        JTextField from_field = new JTextField();
        from_label.setPreferredSize(new Dimension(150, 30));
        from_box.add(from_label, BorderLayout.WEST);
        from_box.add(from_field, BorderLayout.CENTER);
        from_box.setMaximumSize(new Dimension(300, 40));
    
        JPanel receiver_ID = new JPanel(new BorderLayout());
        JLabel receiver_label = new JLabel("Recipient User ID:");
        JTextField receiver_field = new JTextField();
        receiver_label.setPreferredSize(new Dimension(150, 30));
        receiver_ID.add(receiver_label, BorderLayout.WEST);
        receiver_ID.add(receiver_field, BorderLayout.CENTER);
        receiver_ID.setMaximumSize(new Dimension(300, 40));
    
        JPanel receiver_acc = new JPanel(new BorderLayout());
        JLabel acc_label = new JLabel("Recipient Account ID:");
        JTextField acc_field = new JTextField();
        acc_label.setPreferredSize(new Dimension(150, 30));
        receiver_acc.add(acc_label, BorderLayout.WEST);
        receiver_acc.add(acc_field, BorderLayout.CENTER);
        receiver_acc.setMaximumSize(new Dimension(300, 40));
    
        JPanel amount_box = new JPanel(new BorderLayout());
        JLabel amount_label = new JLabel("Amount:");
        JTextField amount_field = new JTextField();
        amount_label.setPreferredSize(new Dimension(150, 30));
        amount_box.add(amount_label, BorderLayout.WEST);
        amount_box.add(amount_field, BorderLayout.CENTER);
        amount_box.setMaximumSize(new Dimension(400, 40));
    
        send_panel.add(from_box);
        send_panel.add(receiver_ID);
        send_panel.add(receiver_acc);
        send_panel.add(amount_box);
        send_panel.add(jp);
    
        int result = JOptionPane.showConfirmDialog(Client.jf, send_panel, "Send Money", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    
        if (result == JOptionPane.OK_OPTION) {
            if (!jp.hasSignature()) {
                JOptionPane.showMessageDialog(Client.jf, "Please draw your signature before sending.");
                return;
            }
            String from_acc = from_field.getText();
            String to_ID = receiver_field.getText();
            String to_acc = acc_field.getText();
            String amount = amount_field.getText();
    
            if (!from_acc.isEmpty() && !to_ID.isEmpty() && !to_acc.isEmpty() && !amount.isEmpty()) {
                Client.sout.println("SEND " + from_acc + " " + to_ID + " " + to_acc + " " + amount);
                if (Client.sin.hasNextLine()) {
                    String response = Client.sin.nextLine();
                    if (response.equals("SUCCESS")) {
                        JOptionPane.showMessageDialog(Client.jf, "Transfer Successful!");
                    } else {
                        JOptionPane.showMessageDialog(Client.jf, response);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(Client.jf, "Please enter all the required information.");
            }
            Client.change_panel(new account_page());
        }
    }
    private void transaction_log(){
        Client.change_panel(new transaction_page());
    }
}
class transaction_page extends JPanel{
    public transaction_page(){
        Client.sout.println("VIEW");
        String all_transactions = "";
        while (Client.sin.hasNextLine()) {
            String line = Client.sin.nextLine();
            if (line.equals("END")) {
                break;
            } 
            all_transactions += line + "\n";
        }

        final String toFileTransactions = all_transactions;
        setLayout(new BorderLayout());

        JPanel button_area = new JPanel(new BorderLayout());
        JButton to_file = new JButton("To File");
        button_area.add(to_file, BorderLayout.CENTER);
        JButton back = new JButton("Back");
        back.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){Client.change_panel(new account_page());}});
        to_file.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                try{
                    Client.sout.println("NAME");
                    String name = Client.sin.nextLine();
                    String file_name = name + "Transactions"+ ".txt";                    
                    PrintStream out_file = new PrintStream(file_name);
                    out_file.println(toFileTransactions);
                    out_file.close();
                } catch(Exception ex){ 
                    System.out.println("Exception caught: " + ex.toString());
                }
            }
        });

        button_area.add(back, BorderLayout.EAST);
        add(button_area, BorderLayout.SOUTH);
        JTextArea list = new JTextArea();
        list.setEditable(false);

        JScrollPane scroll = new JScrollPane(list);
        list.setText(all_transactions);
        add(scroll, BorderLayout.CENTER);

    }
}

class MyPanel extends JPanel{
    class Point{
        int x;
        int y;
        Point(int newx, int newy){x = newx; y = newy;}
    }
    ArrayList<Point> al;
    MyPanel(){
        super();
        al = new ArrayList();
    }
    public void addPoint(int x, int y){
        al.add(new Point(x,y));
    }
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        for (Point p: al){
            g.fillOval(p.x-2, p.y-2, 4, 4);
        }
    }
    public boolean hasSignature() {
        return al.size() > 0;
    }
    public void clear(){
        al.clear();
        repaint();
    }
}
