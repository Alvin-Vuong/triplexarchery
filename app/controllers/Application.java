package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import play.data.Form;
import views.html.*;
import models.User;
import models.End;
import models.Round;
import java.util.List;
import java.util.ArrayList;
import java.sql.SQLException;

public class Application extends Controller {
    
    final static Form<User> userForm = Form.form(User.class);
    final static Form<End> endForm = Form.form(End.class);
    
    // Render homepage.
    public static Result index() {
        return ok(index.render(userForm));
    }
    
    // Handle login, render dashboard.
    public static Result login() {
        Form<User> filledForm = userForm.bindFromRequest();
        User user = filledForm.get();
        System.out.print(user.email + " " + user.password);
        
        // TODO: check DB to see if user is coach

        try {
            boolean log = Database.login(user.email, user.password);
            if (log) {
                User userQuery = Database.getInfo(user.email);
                userQuery.email = user.email;
                userQuery.password = user.password;
                // TODO: 911
                if (userQuery.id == 0) {
                    return ok(login.render("Something is wrong. Try again."));
                } else {
                    return ok(dash.render(userQuery, false));
                }
            } else {
                return ok(login.render("Incorrect email or password."));
                
                // TODO: fix navbar on failed login -- says "logout" when not logged in
                
            }
        } catch (SQLException e) {
            return ok(login.render("Something is wrong. Try again."));
        }
    }
    
    // Renders registration form.
    public static Result register() {
        return ok(register.render(""));
    }
    
    // Handles registration, renders login or re-register.
    public static Result verify() {
        Form<User> filledForm = userForm.bindFromRequest();
        User created = filledForm.get();
        System.out.print("Registered new user " + created.email + created.firstname + created.lastname);
        
        try {
            boolean log = Database.registerCheck(created.email);
            if (log) { //valid username
                Database.registerNew(created.email, created.password, created.firstname, created.lastname);
                return ok(login.render(""));
            } else { //redirect to register again
                return ok(register.render(created.email + " already has an account."));
            }
        } catch (SQLException e) {
            return ok(register.render("Something is wrong. Try again."));
        }
    }
    
    public static Result learnMore() {
        return ok(more.render());
    }
    
    // Handles round description entry and creation.
    // Renders beginning of ends entry.
    public static Result create(int id) {
        Form<Round> filledForm = Form.form(Round.class).bindFromRequest();
        Round r = filledForm.get();
        String d = r.description;
        System.out.println(d);
        
        User user = Database.getInfo(id);
        user.id = id;
        
        try {
            int r_id = Database.addRound(id, d);
            System.out.println("in create(), user's info: " + user.email + user.firstname);
            return ok(create.render(user, r_id, endForm, 1));
        } catch (SQLException e){
            return ok(login.render("Something is wrong. Try again."));
        }
    }
    
    // Handles end entry, renders next end entry. (Loop)
    // Renders dashboard when complete.
    public static Result submit(int id, int roundid, int end) {
        Form<End> filledForm = endForm.bindFromRequest();
        End created = filledForm.get();
        created.endNumber = end;
        String[] x = {created.a1, created.a2, created.a3};
        created.total = sumArrows(x, true);                                 // TODO: true parameter for outer 10, false parameter for inner 10.
        System.out.print(created.a1 + " " + created.a2 + " " + created.a3);
        
        try {
            Database.addEnd(id, roundid, end, created.a1, created.a2, created.a3, created.total);    
        } catch (SQLException e) {
            return ok(login.render("Something is wrong. Try again."));
        }
        User user = Database.getInfo(id);
        user.id = id;
        
        if (end == 10) {                                                // TODO: 10 can be replaced with numEnds
        // TODO: check DB to see if user is coach
            return ok(dash.render(user, false));
        } else {
            return ok(create.render(user, roundid, endForm, end+1));
        } 
        // TODO: shows current round cumulative score
    }
    
    // Renders list of rounds, with links to view each round specifically.
    public static Result roundsList(int id) {
        User user = Database.getInfo(id);
        user.id = id;
        
        List<Round> rounds = new ArrayList<Round>();
        
        try {
            rounds = Database.getTenRounds(id, 1);                      // TODO: Make "Load More" button that increments second parameter.
        } catch (SQLException e) {
            return ok(login.render("Something is wrong. Try again."));
        }
        
        return ok(roundslist.render(user, rounds));
    }
    
    // Renders list of ends of a round.
    public static Result viewAllEnds(int id, int roundid) {
        User user = Database.getInfo(id);
        user.id = id;
        
        Round round = new Round();
        
        try {
            round = Database.getRoundInfo(roundid);
        } catch (SQLException e) {
            return ok(login.render("Something is wrong. Try again."));
        }
        
        return ok(end.render(user, round.score, round.ends, round.description, round.date));       // TODO: Redirect back to round list.
    }
    
    // Takes in an arrow value String.
    // Returns the integer value of the String.
    public static int toValue(String s, boolean isOuter) {
        if (s.equals("m")) {
            return 0;
        } else if (s.equals("X")) {
            return 10;
        } else if (s.equals("10") && !isOuter) {
            return 9;
        } else {
            return Integer.parseInt(s);
        }
    }
    
    // Computes the sum value of a String array of arrows.
    // Returns this value.
    public static int sumArrows(String[] arrows, boolean isOuter) {
        int total = 0;
        for (int i = 0; i < arrows.length; i++)
        {
            total += toValue(arrows[i], isOuter);
        }
        return total;
    }
}
