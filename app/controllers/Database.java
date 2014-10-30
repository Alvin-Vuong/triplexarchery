package controllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
//import javax.json.*;
import java.util.List;
import java.util.ArrayList;
import models.End;
import models.Round;
import models.User;

public class Database {

    // Checks email with existing accounts in the database.
    // Returns true if email is available, false if taken.
    // (Comment Blocked: Returns JsonArray: {"valid": true} if email is available, false if taken.)
    public static boolean registerCheck(String em) throws SQLException {
        
        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        String url = DB.URL();
        String user = DB.user();
        String password = DB.pass();

        try {
            
            con = DriverManager.getConnection(url, user, password);
            
            String query = "SELECT account_id FROM account WHERE email = ?";
            pst = con.prepareStatement(query);
            pst.setString(1, em);
            rs = pst.executeQuery();

            if (rs.next() == false)
            {
                return true;
            /* Json Return Implementation
            //    System.out.println("Account available!");
                JsonArray array = Json.createArrayBuilder()
                .add(Json.createObjectBuilder()
                    .add("valid", true))
                .build();
                
                // FOR TESTING
                JsonObject value = array.getJsonObject(0);
                boolean truth = value.getBoolean("valid");
                
                if (truth)
                    System.out.println("Yep, available truth"); // SHOULD PRINT THIS
                else
                    System.out.println("Nope, available else");
                //
                
                return array;
            */
            }
            else
            {
                return false;
            /*
            //    System.out.println("Account already exists!");
                JsonArray array = Json.createArrayBuilder()
                .add(Json.createObjectBuilder()
                    .add("valid", false))
                .build();
                
                // FOR TESTING
                JsonObject value = array.getJsonObject(0);
                boolean truth = value.getBoolean("valid");
                
                if (truth)
                    System.out.println("Yep, exists truth");
                else
                    System.out.println("Nope, exists else");    // SHOULD PRINT THIS
                //
                
                return array;
            */
            }

        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(Database.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
            throw new SQLException();

        } finally {

            try {
                if (rs != null) {
                    rs.close();
                }
                if (pst != null) {
                    pst.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {

                Logger lgr = Logger.getLogger(Database.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }
    
    // Inserts a new account into the database.
    // Returns true if successful, false if not.
    public static boolean registerNew(String em, String pw, String fn, String ln) {
        
        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        String url = DB.URL();
        String user = DB.user();
        String password = DB.pass();

        try {
            
            con = DriverManager.getConnection(url, user, password);

            String stm = "INSERT INTO account(email, password, first_name, last_name, isCoach) VALUES(?, ?, ?, ?, false)";
            pst = con.prepareStatement(stm);
            pst.setString(1, em);
            pst.setString(2, pw);
            pst.setString(3, fn);
            pst.setString(4, ln);
            pst.executeUpdate();
            
            return true;

        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(Database.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
            return false;

        } finally {

            try {
                if (rs != null) {
                    rs.close();
                }
                if (pst != null) {
                    pst.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {

                Logger lgr = Logger.getLogger(Database.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }
    
    // Checks database to see if account exists, and that password is correct.
    // Returns true if entries are valid, false if not.
    // (Comment Blocked: Returns JsonArray: {"valid": true} if login successful, false if not.)
    public static boolean login(String em, String pw) throws SQLException {
        
        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        String url = DB.URL();
        String user = DB.user();
        String password = DB.pass();

        try {
            
            con = DriverManager.getConnection(url, user, password);
            
            String query = "SELECT email, password FROM account WHERE email = ? AND password = ?";
            pst = con.prepareStatement(query);
            pst.setString(1, em);
            pst.setString(2, pw);
            rs = pst.executeQuery();

            if (rs.next() == false)
            {
                return false;
            /* Json Return Implementation
            //    System.out.println("No account match"); // TODO: Redirect user back to login: "Either email or password is incorrect."
                JsonArray array = Json.createArrayBuilder()
                .add(Json.createObjectBuilder()
                    .add("valid", false))
                .build();
                
                // FOR TESTING
                JsonObject value = array.getJsonObject(0);
                boolean truth = value.getBoolean("valid");
                
                if (truth)
                    System.out.println("Yep, no account match?!");
                else
                    System.out.println("Nope, no account else");    // SHOULD PRINT THIS
                //
                
                return array;
            */
            }
            else
            {
                return true;
            /* Json Return Implementation
            //    System.out.println("Logged in!"); // TODO: This is where a session would be implemented...
                JsonArray array = Json.createArrayBuilder()
                .add(Json.createObjectBuilder()
                    .add("valid", true))
                .build();
                
                // FOR TESTING
                JsonObject value = array.getJsonObject(0);
                boolean truth = value.getBoolean("valid");
                
                if (truth)
                    System.out.println("Yep, logged in"); // SHOULD PRINT THIS
                else
                    System.out.println("Nope, logged else?");
                //
                
                return array;
            */
            }

        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(Database.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
            throw new SQLException();

        } finally {

            try {
                if (rs != null) {
                    rs.close();
                }
                if (pst != null) {
                    pst.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {

                Logger lgr = Logger.getLogger(Database.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }
    
    // Looks up an email in the database.
    // Returns encrypted password of account associated with email, SQLException/Empty string if not found.
    public static String getPW(String em) throws SQLException {
        
        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        String url = DB.URL();
        String user = DB.user();
        String password = DB.pass();

        try {
            
            con = DriverManager.getConnection(url, user, password);
            String pw = "";
            
            User userQuery = new User();
            
            String query = "SELECT password FROM account WHERE email = ?";
            pst = con.prepareStatement(query);
            pst.setString(1, em);
            rs = pst.executeQuery();
            
            while (rs.next()) {
                pw = rs.getString(1);
            }
            
            return pw;
            
        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(Database.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
            throw new SQLException();

        } finally {

            try {
                if (rs != null) {
                    rs.close();
                }
                if (pst != null) {
                    pst.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {

                Logger lgr = Logger.getLogger(Database.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
                
            }
        }
    }
    
    // Looks up an email in the database.
    // Returns User object with account_id and first_name associated with email, SQLException if not found.
    public static User getInfo(String em) {
        
        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        String url = DB.URL();
        String user = DB.user();
        String password = DB.pass();

        try {
            
            con = DriverManager.getConnection(url, user, password);
            
            User userQuery = new User();
            
            String query = "SELECT account_id, first_name FROM account WHERE email = ?";
            pst = con.prepareStatement(query);
            pst.setString(1, em);
            boolean isResult = pst.execute();
            do {
                rs = pst.getResultSet();

                while (rs.next()) {
                    userQuery.id = rs.getInt(1);
                    userQuery.firstname = rs.getString(2);
                }
                isResult = pst.getMoreResults();
            } while (isResult);

            return userQuery;
            
        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(Database.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
            return new User();

        } finally {

            try {
                if (rs != null) {
                    rs.close();
                }
                if (pst != null) {
                    pst.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {

                Logger lgr = Logger.getLogger(Database.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
                
            }
        }
    }
    
    // Looks up an account_id in the database.
    // Returns User object with email and first_name associated with id, SQLException if not found.
    public static User getInfo(int acc_id) {
        
        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        String url = DB.URL();
        String user = DB.user();
        String password = DB.pass();

        try {
            
            con = DriverManager.getConnection(url, user, password);
            
            User userQuery = new User();
            
            String query = "SELECT email, first_name FROM account WHERE account_id = ?";
            pst = con.prepareStatement(query);
            pst.setInt(1, acc_id);
            boolean isResult = pst.execute();
            do {
                rs = pst.getResultSet();

                while (rs.next()) {
                    userQuery.email = rs.getString(1);
                    userQuery.firstname = rs.getString(2);
                }
                isResult = pst.getMoreResults();
            } while (isResult);

            return userQuery;
            
        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(Database.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
            return new User();

        } finally {

            try {
                if (rs != null) {
                    rs.close();
                }
                if (pst != null) {
                    pst.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {

                Logger lgr = Logger.getLogger(Database.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }
    
    // Looks up a round in the database.
    // Returns the account_id linked to that round.
    public static int getAccount(int rnd_id) throws SQLException {
            
        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        String url = DB.URL();
        String user = DB.user();
        String password = DB.pass();

        try {
            
            con = DriverManager.getConnection(url, user, password);
            int acct_id = -1;
            
            String query = "SELECT account_id FROM round WHERE round_id = ?";
            pst = con.prepareStatement(query);
            pst.setInt(1, rnd_id);
            rs = pst.executeQuery();
            
            while (rs.next()) {
                acct_id = rs.getInt(1);
            }
            
            return acct_id;

        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(Database.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
            throw new SQLException();

        } finally {

            try {
                if (rs != null) {
                    rs.close();
                }
                if (pst != null) {
                    pst.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {

                Logger lgr = Logger.getLogger(Database.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }
    
    // Adds a new round in the database.
    // Returns the round_id if successful, 0 if not.
    public static int addRound(int acc_id, String desc) throws SQLException {
        
        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        String url = DB.URL(); 
        String user = DB.user();
        String password = DB.pass();
        
        try {
            
            con = DriverManager.getConnection(url, user, password);
            
            int rnd_id = 0; // Zero means unsuccessful query.
            
            String stm = "INSERT INTO round (account_id, description, score, date_created) VALUES (?, ?, 0, DEFAULT) RETURNING round_id";
            pst = con.prepareStatement(stm);
            pst.setInt(1, acc_id);
            pst.setString(2, desc);
            boolean isResult = pst.execute();
            do {
                rs = pst.getResultSet();

                while (rs.next()) {
                    rnd_id = rs.getInt(1);
                }
                isResult = pst.getMoreResults();
            } while (isResult);
            
            return rnd_id;

        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(Database.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
            throw new SQLException();

        } finally {

            try {
                if (rs != null) {
                    rs.close();
                }
                if (pst != null) {
                    pst.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {

                Logger lgr = Logger.getLogger(Database.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }
    
    // Overloaded version of previous addRound(), except with date parameter.
    public static int addRound(int acc_id, String desc, String date) throws SQLException {
        
        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        String url = DB.URL(); 
        String user = DB.user();
        String password = DB.pass();
        
        try {
            
            con = DriverManager.getConnection(url, user, password);
            
            int rnd_id = 0; // Zero means unsuccessful query.
            
            String stm = "INSERT INTO round (account_id, description, score, date_created) VALUES (?, ?, 0, ?) RETURNING round_id";
            pst = con.prepareStatement(stm);
            pst.setInt(1, acc_id);
            pst.setString(2, desc);
            pst.setString(3, date);
            boolean isResult = pst.execute();
            do {
                rs = pst.getResultSet();

                while (rs.next()) {
                    rnd_id = rs.getInt(1);
                }
                isResult = pst.getMoreResults();
            } while (isResult);
            
            return rnd_id;

        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(Database.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
            throw new SQLException();

        } finally {

            try {
                if (rs != null) {
                    rs.close();
                }
                if (pst != null) {
                    pst.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {

                Logger lgr = Logger.getLogger(Database.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }
    
    // Adds an end to the round table in the database.
    // If unsuccessful, should throw a SQLException.
    public static void addEnd(int acc_id, int rnd_id, int end, String arrow1, String arrow2, String arrow3) throws SQLException {
        
        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        String url = DB.URL();
        String user = DB.user();
        String password = DB.pass();
        
        try {
            
            String[] arrows = {arrow1, arrow2, arrow3};
            int endTotal = Application.sumArrows(arrows, true);      // TODO: true parameter for outer 10, false parameter for inner 10.
            int curTotal = getScore(acc_id, rnd_id) + endTotal;
            
            con = DriverManager.getConnection(url, user, password);
            
            String stm = "UPDATE round SET end_" + end + " = '{" + arrow1 + ", " + arrow2 + ", " + arrow3 + "}', score = ?, end_" + end + "_total = " + endTotal + " WHERE account_id = ? AND round_id = ?";
            pst = con.prepareStatement(stm);
            pst.setInt(1, curTotal);
            pst.setInt(2, acc_id);
            pst.setInt(3, rnd_id);
            pst.executeUpdate();

        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(Database.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
            throw new SQLException();

        } finally {

            try {
                if (rs != null) {
                    rs.close();
                }
                if (pst != null) {
                    pst.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {

                Logger lgr = Logger.getLogger(Database.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }

    // Edits an end in the round table in the database.
    // If unsuccessful, should throw a SQLException.
    public static void editEnd(int acc_id, int rnd_id, int end, String arrow1, String arrow2, String arrow3, int newTotal) throws SQLException {
        
        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        String url = DB.URL();
        String user = DB.user();
        String password = DB.pass();
        
        try {
            
            String[] arrows = {arrow1, arrow2, arrow3};
            int endTotal = Application.sumArrows(arrows, true);
            
            con = DriverManager.getConnection(url, user, password);
            
            String stm = "UPDATE round SET end_" + end + " = '{" + arrow1 + ", " + arrow2 + ", " + arrow3 + "}', score = ?, end_" + end + "_total = " + endTotal + " WHERE account_id = ? AND round_id = ?";
            pst = con.prepareStatement(stm);
            pst.setInt(1, newTotal);
            pst.setInt(2, acc_id);
            pst.setInt(3, rnd_id);
            pst.executeUpdate();

        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(Database.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
            throw new SQLException();

        } finally {

            try {
                if (rs != null) {
                    rs.close();
                }
                if (pst != null) {
                    pst.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {

                Logger lgr = Logger.getLogger(Database.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }
    
    // Retrieves all ends of a given round in the database.
    // Returns a List containing these ends, throws SQLException otherwise.
    public static List<End> getAllEnds(int acc_id, int rnd_id) throws SQLException {
        
        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        String url = DB.URL();
        String user = DB.user();
        String password = DB.pass();

        try {
            
            con = DriverManager.getConnection(url, user, password);
            
            List<End> ends = new ArrayList<End>();
            String[] x = new String[3];
            
            for ( int i = 1; i < 11; i++)       // TODO: For other than 10 ends: i < numEnds + 1
            {
                String query = "SELECT end_" + i + " FROM round WHERE account_id = ? AND round_id = ?";
                pst = con.prepareStatement(query);
                pst.setInt(1, acc_id);
                pst.setInt(2, rnd_id);
                rs = pst.executeQuery();
                
                while (rs.next()) {
                    x = (String[]) rs.getArray(1).getArray();
                }
                ends.add(new End(x[0], x[1], x[2], i, Application.sumArrows(x, true)));     // TODO: true parameter for outer 10, false parameter for inner 10.
            }
            /* FOR TESTING            
            for (End e : ends)
            {
                System.out.println(e);
            }
            */            
            return ends;

        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(Database.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
            throw new SQLException();

        } finally {

            try {
                if (rs != null) {
                    rs.close();
                }
                if (pst != null) {
                    pst.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {

                Logger lgr = Logger.getLogger(Database.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }
    
    // Retrieves a certain ten rounds of a given account in the database, using a set #.
    // Returns a List containing these rounds, throws SQLException otherwise.
    public static List<Round> getTenRounds(int acc_id, int setNum) throws SQLException {
        
        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        String url = DB.URL();
        String user = DB.user();
        String password = DB.pass();

        try {
            
            con = DriverManager.getConnection(url, user, password);
            
            List<Round> rounds = new ArrayList<Round>();
            int id = 0;
            int score = -1;
            String desc = "";
            String date = "";
            
            String query = "SELECT round_id, score, description, date_created FROM round WHERE account_id = ? ORDER BY date_created DESC, round_id DESC LIMIT 10 OFFSET ?";
            pst = con.prepareStatement(query);
            pst.setInt(1, acc_id);
            pst.setInt(2, ((setNum-1)*10));
            boolean isResult = pst.execute();
            do {
                rs = pst.getResultSet();
                
                while (rs.next()) {
                    id = rs.getInt(1);
                    score = rs.getInt(2);
                    desc = rs.getString(3);
                    date = rs.getString(4);
                    rounds.add(new Round(id, score, desc, date, null));
                }
                isResult = pst.getMoreResults();
            } while (isResult);
            
            return rounds;

        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(Database.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
            throw new SQLException();

        } finally {

            try {
                if (rs != null) {
                    rs.close();
                }
                if (pst != null) {
                    pst.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {

                Logger lgr = Logger.getLogger(Database.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }
    
    // Retrieves current score of a round in the database.
    // Returns this value if successful, throws SQLException if not.
    public static int getScore(int acc_id, int rnd_id) throws SQLException {
        
        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        String url = DB.URL();
        String user = DB.user();
        String password = DB.pass();

        try {
            
            con = DriverManager.getConnection(url, user, password);
            int score = 0;
            
            String query = "SELECT score FROM round WHERE account_id = ? AND round_id = ?";
            pst = con.prepareStatement(query);
            pst.setInt(1, acc_id);
            pst.setInt(2, rnd_id);
            rs = pst.executeQuery();
            
            while (rs.next()) {
                score = rs.getInt(1);
            }
            
            return score;

        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(Database.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
            throw new SQLException();

        } finally {

            try {
                if (rs != null) {
                    rs.close();
                }
                if (pst != null) {
                    pst.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {

                Logger lgr = Logger.getLogger(Database.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }
    
    // Retrieves the information associated with a certain round from the database.
    // Returns a Round object containing this, throws SQLException otherwise.
    public static Round getRoundInfo(int rnd_id) throws SQLException {
        
        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        String url = DB.URL();
        String user = DB.user();
        String password = DB.pass();

        try {
            
            con = DriverManager.getConnection(url, user, password);
            
            List<End> ends = new ArrayList<End>();
            String[] x = new String[3];
            
            for ( int i = 1; i < 11; i++)       // TODO: For other than 10 ends: i < numEnds + 1
            {
                String query = "SELECT end_" + i + " FROM round WHERE round_id = ?";
                pst = con.prepareStatement(query);
                pst.setInt(1, rnd_id);
                rs = pst.executeQuery();
                
                while (rs.next()) {
                    x = (String[]) rs.getArray(1).getArray();
                }
                ends.add(new End(x[0], x[1], x[2], i, Application.sumArrows(x, true)));     // TODO: true parameter for outer 10, false parameter for inner 10.
            }
            /* FOR TESTING            
            for (End e : ends)
            {
                System.out.println(e);
            }
            */
            
            int score = -1;
            String desc = "";
            String date = "";
            Round round = new Round();
            
            String query = "SELECT score, description, date_created FROM round WHERE round_id = ?";
            pst = con.prepareStatement(query);
            pst.setInt(1, rnd_id);
            boolean isResult = pst.execute();
            do {
                rs = pst.getResultSet();
                
                while (rs.next()) {
                    score = rs.getInt(1);
                    desc = rs.getString(2);
                    date = rs.getString(3);
                    round.id = rnd_id;
                    round.score = score;
                    round.description = desc;
                    round.date = date;
                    round.ends = ends;
                }
                isResult = pst.getMoreResults();
            } while (isResult);
            
            return round;

        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(Database.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
            throw new SQLException();

        } finally {

            try {
                if (rs != null) {
                    rs.close();
                }
                if (pst != null) {
                    pst.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {

                Logger lgr = Logger.getLogger(Database.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }
    
    // TODO: Finish implementing this and test.
    // Given new data for an existing round, updates the round information in the database.
    // If unsuccessful, show throw a SQLException.
    public static void editRound(int rnd_id, String desc, List<End> ends) throws SQLException {       // TODO: Add date parameter.
        
        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        String url = DB.URL(); 
        String user = DB.user();
        String password = DB.pass();
        
        try {
            
            con = DriverManager.getConnection(url, user, password);
            
            String stm = "?";   // TODO: Use UPDATE query to edit previous data, given round_id.    // TODO: Update ends
            pst = con.prepareStatement(stm);
            pst.setString(1, desc);
            pst.executeUpdate();

        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(Database.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
            throw new SQLException();

        } finally {

            try {
                if (rs != null) {
                    rs.close();
                }
                if (pst != null) {
                    pst.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {

                Logger lgr = Logger.getLogger(Database.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }
    
    // Deletes a round given a round_id from the database.
    // Returns true if successful, false if not.
    public static boolean deleteRound(int rnd_id) {
        
        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        String url = DB.URL();
        String user = DB.user();
        String password = DB.pass();

        try {
            
            con = DriverManager.getConnection(url, user, password);

            String stm = "DELETE FROM round WHERE round_id = ?";
            pst = con.prepareStatement(stm);
            pst.setInt(1, rnd_id);
            
            pst.executeUpdate();
            
            return true;


        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(Database.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
            return false;

        } finally {

            try {
                if (rs != null) {
                    rs.close();
                }
                if (pst != null) {
                    pst.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {

                Logger lgr = Logger.getLogger(Database.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }
    
    // Deletes an account from the database.
    // Returns true if successful, false if not.
    public static boolean deleteUser(int user_id) {
        
        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        String url = DB.URL();
        String user = DB.user();
        String password = DB.pass();

        try {
            
            con = DriverManager.getConnection(url, user, password);
            
            // Delete rounds associated with account first.
            String stm = "DELETE FROM round WHERE account_id = ?";              //TODO: Test this function. Pretty sure it works.
            pst = con.prepareStatement(stm);
            pst.setInt(1, user_id);
            
            pst.executeUpdate();
            
            // Now delete the account itself.
            stm = "DELETE FROM account WHERE account_id = ?";
            pst = con.prepareStatement(stm);
            pst.setInt(1, user_id);
            
            pst.executeUpdate();
            
            return true;


        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(Database.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
            return false;

        } finally {

            try {
                if (rs != null) {
                    rs.close();
                }
                if (pst != null) {
                    pst.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {

                Logger lgr = Logger.getLogger(Database.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }
}
