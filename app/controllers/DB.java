package controllers;

public class DB {
    /* FOR HEROKU DEPLOYMENT:
        final static String url = System.getenv("DB_URL");
        final static String user = System.getenv("USERNAME");
        final static String pass = System.getenv("PASSWORD");
      
       FOR LOCAL TESTING:
        final static String url = "jdbc:postgresql://localhost:_Port#_/_DatabaseName_"; DEFAULT PORT:       5432
        final static String user = "_username_";                                        DEFAULT USERNAME:   postgres
        final static String pass = "_password_";
    */
    
    final static String url = System.getenv("DB_URL");
    final static String user = System.getenv("USERNAME");
    final static String pass = System.getenv("PASSWORD");

    
    public static String URL() {
        return url;
    }
    
    public static String user() {
        return user;
    }
    
    public static String pass() {
        return pass;
    }
}