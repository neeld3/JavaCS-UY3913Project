import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;

public class Server {
    public static Database shared_database;  
    public static void main(String[] args) {
        try {
            //initializes server
            ServerSocket server = new ServerSocket(5190);
            shared_database = new Database();
            //accepts clients
            while(true){
                Socket socket = server.accept(); 
                new ProcessConnection(socket, shared_database).start();
            }
        } catch (IOException | SQLException ex) {
            System.out.println("Exception caught: " + ex.toString());
        } 
    }
}

//database class for clients
class Database {
    public Connection conn;

    public Database () throws SQLException {
        conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/bank", "root", "");
    }

    // Handling loggin in
    public int login(String username, String password, PrintStream out) throws SQLException {
        //gets the information about username and password
        PreparedStatement query = conn.prepareStatement("SELECT user_id, password FROM user WHERE username = ?");
        query.setString(1, username);
        ResultSet result = query.executeQuery();
        //if the password matches logs in else error
        if (result.next()) {
            String stored_pass = result.getString("password");
          
            if (BCrypt.checkpw(password, stored_pass) ) {
                int session = result.getInt("user_id");
                out.println("SUCCESS");
                return session;
            } else {
                out.println("Login failed. Enter the correct password.");
                return -1;
            }
        } else {
            out.println("Login failed. Invalid username.");
            return -1;
        }
    }

    // Handling registering new user
    public int signup(String username, String password, PrintStream out) throws SQLException {
        //checks if username is unique
        username = username.toLowerCase();
        PreparedStatement check = conn.prepareStatement("SELECT * FROM User WHERE username = ?");
        check.setString(1, username);
        ResultSet result = check.executeQuery();
        if (result.next()){
            out.println("Username already exists");
            return -1;
        }
        //creates account
        PreparedStatement query = conn.prepareStatement("{CALL signUp(?, ?)}");
        String hashed_password = BCrypt.hashpw(password, BCrypt.gensalt());
        query.setString(1, username);
        query.setString(2, hashed_password);
        query.execute();
        out.println("SUCCESS");
        return login(username, password, out);
    }

    // Displaying all the accounts 
    public void displayAccounts(int session, PrintStream out) throws SQLException {
        //gets accounts from database
        PreparedStatement query = conn.prepareStatement("{CALL get_accounts(?)}");
        query.setInt(1, session);
        ResultSet result = query.executeQuery();

        //sends all accounts information to client that requested
        while (result.next()){
            int account_id = result.getInt("account_id");
            double balance = result.getDouble("balance");
            String shared = result.getString("shared_with");
            if (shared != null) {
                out.println("ACCOUNT " + account_id + " " + balance + " SHARED_WITH " + shared);
            } else {
                out.println("ACCOUNT " + account_id + " " + balance);
            }
        }
        out.println("END");
    }

    //for when client asks for the username
    public void send_name(int session, PrintStream out) throws SQLException {
        PreparedStatement query = conn.prepareStatement("SELECT username FROM User WHERE user_id = ?");
        query.setInt(1, session);
        ResultSet result = query.executeQuery();

        while (result.next()){
            String user_name = result.getString("username");
            out.println(user_name);
        }
    }
    
    //for when clients asks for all usernames
    public void view(int session, PrintStream out) throws SQLException {
        PreparedStatement query = conn.prepareStatement("{CALL view_transactions(?)}");
        query.setInt(1, session);
        ResultSet result = query.executeQuery();

        //gets all transactions from query, formates and sends it to client.
        while (result.next()){
            String type = result.getString("type");
            int sender_acc_id = result.getInt("sender_account_id");
            String receiver_name = result.getString("receiver_name");
            int receiver_acc_id = result.getInt("receiver_account_id");
            double amount = result.getDouble("amount");
            Timestamp timestamp = result.getTimestamp("timestamp");

            if (type.equals("Deposit")){
                out.println(timestamp + " | Depositing $" + amount + " to Account: " + sender_acc_id);
            } else if (type.equals("Withdraw")){
                out.println(timestamp + " | Withdrawing $" + amount + " to Account: " + sender_acc_id);
            } else if (type.equals("Send")) {
                out.println(timestamp + " | Sending $" + amount + " to User: " + receiver_name + " | Account ID: " + receiver_acc_id);
            }
        }
        out.println("END");
    }
 
    //when client deposits money
    public synchronized void deposit(int account_id, double amount, int session, PrintStream out) throws SQLException {
        //checks for valid amount
        if (amount <= 0){
            out.println("Deposit amount has to be greater than $0");
            return;
        }
        //updates database, checks matching account, and then updates values
        PreparedStatement check_user = conn.prepareStatement("SELECT * FROM user_accounts WHERE user_id = ? AND account_id = ?");
        check_user.setInt(1, session);
        check_user.setInt(2, account_id);
        ResultSet result = check_user.executeQuery();
        if (result.next()) {
            PreparedStatement query = conn.prepareStatement("{CALL deposit(?, ?)}");
            query.setInt(1, account_id);
            query.setDouble(2, amount);
            query.execute();
            record_transaction(session, account_id, null, null, amount, "Deposit");
            out.println("SUCCESS");
        } else {
            out.println("Deposit failed.");
        }
    }

    //when client withdraws money
    public synchronized void withdraw(int account_id, double amount, int session, PrintStream out) throws SQLException {
        //checks for valid amount
        if (amount <= 0){
            out.println("Withdraw amount has to be greater than $0");
            return;
        }
        //updates database, checks matching account, and then updates values
        PreparedStatement check_user = conn.prepareStatement("SELECT * FROM user_accounts WHERE user_id = ? AND account_id = ?");
        check_user.setInt(1, session);
        check_user.setInt(2, account_id);
        ResultSet result = check_user.executeQuery();
        if (result.next()) {
            PreparedStatement check_funds = conn.prepareStatement("SELECT balance FROM user_accounts JOIN account ON account.account_id = user_accounts.account_id WHERE user_accounts.user_id = ? AND user_accounts.account_id = ?");
                check_funds.setInt(1, session);
                check_funds.setInt(2, account_id);
                ResultSet fund_result = check_funds.executeQuery();
                if (fund_result.next()){
                    double sender_balance = fund_result.getDouble("balance");
                    if (sender_balance < amount){
                        out.println("Withdraw amount has to be less than the available balance.");
                        return;
                    }
                } else {
                    return;
                }
            PreparedStatement query = conn.prepareStatement("{CALL withdraw(?, ?)}");
            query.setInt(1, account_id);
            query.setDouble(2, amount);
            query.execute();
            record_transaction(session, account_id, null, null, amount, "Withdraw");
            out.println("SUCCESS");
        } else {
            out.println("Withdraw failed.");
        }
    }
    //when client sends money to another client
    public synchronized void send(int sender_account_id, int receiver_id, int receiver_account_id, double amount, int session, PrintStream out) throws SQLException {
        //ensuring a valid amount is sent
        if (amount <= 0){
            out.println("Send amount has to be greater than $0");
            return;
        }
        conn.setAutoCommit(false);
        
        //sends all updates to the database
        try {
            PreparedStatement check_sender = conn.prepareStatement("SELECT * FROM user_accounts WHERE user_id = ? AND account_id = ?");
            check_sender.setInt(1, session);
            check_sender.setInt(2, sender_account_id);
            ResultSet sender_result = check_sender.executeQuery();

            PreparedStatement check_receiver = conn.prepareStatement("SELECT * FROM user_accounts WHERE user_id = ? AND account_id = ?");
            check_receiver.setInt(1, receiver_id);
            check_receiver.setInt(2, receiver_account_id);
            ResultSet receiver_result = check_receiver.executeQuery();
         
            boolean sender_exists = sender_result.next();
            boolean receiver_exists = receiver_result.next();

            if (sender_exists && receiver_exists) {
                PreparedStatement check_funds = conn.prepareStatement("SELECT balance FROM user_accounts JOIN account ON account.account_id = user_accounts.account_id WHERE user_accounts.user_id = ? AND user_accounts.account_id = ?");
                check_funds.setInt(1, session);
                check_funds.setInt(2, sender_account_id);
                ResultSet fund_result = check_funds.executeQuery();
                if (fund_result.next()){
                    double sender_balance = fund_result.getDouble("balance");
                    if (sender_balance < amount){
                        out.println("Send amount has to be less than the available balance.");
                        conn.rollback();
                        return;
                    }
                } else {
                    conn.rollback();
                    return;
                }
                
                PreparedStatement withdraw_query = conn.prepareStatement("{CALL withdraw(?, ?)}");
                withdraw_query.setInt(1, sender_account_id);
                withdraw_query.setDouble(2, amount);
                withdraw_query.execute();

                PreparedStatement deposit_query = conn.prepareStatement("{CALL deposit(?, ?)}");
                deposit_query.setInt(1, receiver_account_id);
                deposit_query.setDouble(2, amount);
                deposit_query.execute();
                conn.commit();
                record_transaction(session, sender_account_id, receiver_id, receiver_account_id, amount, "Send");
                out.println("SUCCESS");
            } else {
                conn.rollback();
                out.println("Send failed.");
            }
        } catch (SQLException ex) {
            conn.rollback();
            out.println("Transaction failed.");
        } finally {
            conn.setAutoCommit(true);
        }
        
    }
    //inserts valid transactions to data base
    public void record_transaction(int user1_id, int user1_account_id, Integer user2, Integer user2_account_id, double amount, String type) throws SQLException {
        if (user2 != null && user2_account_id != null) {
            PreparedStatement insert_info = conn.prepareStatement("INSERT INTO transaction (Type, sender_id, sender_account_id, receiver_id, receiver_account_id, amount, timestamp) VALUES (?, ?, ?, ?, ?, ?, NOW())");
            insert_info.setString(1, type);
            insert_info.setInt(2, user1_id);
            insert_info.setInt(3, user1_account_id);
            insert_info.setInt(4, user2);
            insert_info.setInt(5, user2_account_id);
            insert_info.setDouble(6, amount);
            insert_info.execute();
        } else {
            PreparedStatement insert_info = conn.prepareStatement("INSERT INTO transaction (Type, sender_id, sender_account_id, receiver_id, receiver_account_id, amount, timestamp) VALUES (?, ?, ?, ?, ?, ?, NOW())");
            insert_info.setString(1, type);
            insert_info.setInt(2, user1_id);
            insert_info.setInt(3, user1_account_id);
            insert_info.setNull(4, java.sql.Types.INTEGER);
            insert_info.setNull(5, java.sql.Types.INTEGER);
            insert_info.setDouble(6, amount);
            insert_info.execute();
        }
    }
}



class ProcessConnection extends Thread{
    Socket socket;
    String username;
    Database database;
    int session;
    ProcessConnection(Socket newSocket, Database db){
        this.socket = newSocket;
        this.database = db;
    }
    @Override
    public void run() {
        try {
            Scanner sin = new Scanner(socket.getInputStream());
            PrintStream sout = new PrintStream(socket.getOutputStream()); 
            //reads from the client
            while (sin.hasNextLine()) {
                String input = sin.nextLine();
                String[] user_input = input.split(" ");
                // if its login command from user
                if (user_input[0].equals("LOGIN")) {
                    int user_id = database.login(user_input[1], user_input[2], sout);
                    if (user_id != -1) {
                        session = user_id;
                    }
                //if its create account command from user
                } else if (user_input[0].equals("CREATE")) {
                    int user_id = database.signup(user_input[1], user_input[2], sout);
                    if (user_id != -1) {
                        session = user_id;
                    }
                //if user asks for accounts
                } else if (user_input[0].equals("DISPLAY_ACCOUNTS")) {
                    database.displayAccounts(session, sout);
                //if user is depositing
                } else if (user_input[0].equals("DEPOSIT")) {
                    int account_id = Integer.parseInt(user_input[1]);
                    double amount = Double.parseDouble(user_input[2]);
                    database.deposit(account_id, amount, session, sout);
                //if user is withdrawing
                } else if (user_input[0].equals("WITHDRAW")) {
                    int account_id = Integer.parseInt(user_input[1]);
                    double amount = Double.parseDouble(user_input[2]);
                    database.withdraw(account_id, amount, session, sout);
                //if user is sending to another 
                } else if (user_input[0].equals("SEND")) {
                    int sender_account_id = Integer.parseInt(user_input[1]);
                    int receiver_id = Integer.parseInt(user_input[2]);
                    int receiver_account_id = Integer.parseInt(user_input[3]);
                    double amount = Double.parseDouble(user_input[4]);
                    database.send(sender_account_id, receiver_id, receiver_account_id, amount, session, sout);
                //if user wants to view transactions
                } else if (user_input[0].equals("VIEW")) {
                    database.view(session, sout);
                //gets the users name
                } else if (user_input[0].equals("NAME")) {
                    database.send_name(session, sout);
                }
            }
        } catch (Exception ex) {
            System.out.println("Error in ProcessConnection: " + ex.toString());
        }
    }
}



