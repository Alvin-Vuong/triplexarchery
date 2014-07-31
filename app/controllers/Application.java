package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.*;
import play.data.*;
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
    final static Form<Round> roundForm = Form.form(Round.class);
    
    // Render homepage.
    @Security.Authenticated(Secured.class)
    public static Result index() {
        request().username();
        System.out.println("1a");
        return redirect(routes.Application.dashboard());
    }
    
    public static Result home() {
        System.out.println("1");
        return ok(index.render(userForm));
    }
    
    // Handle login, redirects to dashboard or renders login.
    public static Result login() {
        System.out.println("2");
        Form<User> filledForm = userForm.bindFromRequest();
        User user = filledForm.get();
        // System.out.print(user.email + " " + user.password);
        
        // TODO: check DB to see if user is coach

        try {
            boolean log = Database.login(user.email, user.password);
            if (log) {
                User userQuery = Database.getInfo(user.email);
                userQuery.email = user.email;
                userQuery.password = user.password;
                // TODO: 911
                if (userQuery.id == 0) {
                    return badRequest(login.render(filledForm, "Something is wrong. Try again."));
                } else {
                    session().clear();
                    session("email", userQuery.email);
                    System.out.println("3");
                    System.out.println(userQuery.email);
                    return redirect(routes.Application.dashboard());
                }
            } else {
                return badRequest(login.render(filledForm, "Incorrect email or password."));
                // TODO: fix navbar on failed login -- says "logout" when not logged in
            }
        } catch (SQLException e) {
            return badRequest(login.render(filledForm, "Something is wrong. Try again."));
        }
    }
    
    // Handles login after unauthorized user attempts to an account page
    public static Result loginRedirect() {
        return badRequest(login.render(userForm, ""));
    }
    
    // Handles logging out by clearing current sessions; redirects to homepage.
    public static Result logout() {
        session().clear();
        return redirect(routes.Application.index());
    }
    
    // After successful registration, user is redirected to login page.
    public static Result registrationSuccess() {
        return ok(login.render(userForm, ""));
    }
    
    // After successful login, user is redirected to dashboard page.
    @Security.Authenticated(Secured.class)
    public static Result dashboard() {
        System.out.println("4");
        User user = Database.getInfo(request().username());
        user.email = request().username();
        System.out.println(request().username());
        return ok(dash.render(user, false));
    }
    
    // Renders registration form.
    public static Result register() {
        return ok(register.render(userForm, ""));
    }
    
    // Handles registration, redirects to login or renders re-register.
    public static Result verify() {
        Form<User> filledForm = userForm.bindFromRequest();
        User created = filledForm.get();
        // System.out.print("Registered new user " + created.email + created.firstname + created.lastname);
        
        try {
            boolean log = Database.registerCheck(created.email);
            // Valid email
            if (log) {
                Database.registerNew(created.email, created.password, created.firstname, created.lastname);
                return redirect(routes.Application.registrationSuccess());
            }
            // Redirect to register again
            else {
                return badRequest(register.render(filledForm, created.email + " already has an account."));
            }
        } catch (SQLException e) {
            return badRequest(register.render(filledForm, "Something is wrong. Try again."));
        }
    }
    
    public static Result learnMore() {
        return ok(more.render());
    }
    
    // Handles round description entry and creation.
    // Renders beginning of ends entry.
    @Security.Authenticated(Secured.class)
    public static Result create() {
        Form<Round> filledForm = Form.form(Round.class).bindFromRequest();
        Round r = filledForm.get();
        String d = r.description;
        // System.out.println(d);
        
        User user = Database.getInfo(request().username());
        user.email = request().username();
        System.out.println("10");
        System.out.println(request().username());
        System.out.println(user.id);
        
        try {
            int r_id = Database.addRound(user.id, d);
            System.out.println("in create(), user's info: " + user.email + user.firstname);
            return ok(create.render(user, r_id, endForm, 1, 0));
        } catch (SQLException e){
            return badRequest(login.render(userForm, "Something is wrong. Try again."));
        }
    }

    @Security.Authenticated(Secured.class)
    public static Result createDesktop() {
        User user = Database.getInfo(request().username());
        user.email = request().username();
        System.out.println("5");
        System.out.println(request().username());
        System.out.println(user.id);
        
        return ok(createDesktopRound.render(user, roundForm));
    }
    
    // Handles end entry, renders next end entry. (Loop)
    // Renders dashboard when complete.
    @Security.Authenticated(Secured.class)
    public static Result submit(int id, int roundid, int end) {
        Form<End> filledForm = endForm.bindFromRequest();
        End created = filledForm.get();
        created.endNumber = end;
        String[] x = {created.a1, created.a2, created.a3};
        System.out.print(created.a1 + " " + created.a2 + " " + created.a3); // THIS LINE CANT BE REMOVED FOR SOME REASON
        
        try {
            Database.addEnd(id, roundid, end, created.a1, created.a2, created.a3);    
        } catch (SQLException e) {
            return badRequest(login.render(userForm, "Something is wrong. Try again."));
        }
        
        User user = Database.getInfo(request().username());
        user.email = request().username();
        System.out.println("11");
        System.out.println(request().username());
        System.out.println(user.id);
        
        if (end == 10) {                                                // TODO: 10 can be replaced with numEnds
        // TODO: check DB to see if user is coach
            return redirect(routes.Application.dashboard());
        } else {
            int curScore = -1;
            try {
                curScore = Database.getScore(id, roundid);
            } catch (SQLException e) {
                return badRequest(login.render(userForm, "Something is wrong. Try again."));
            }
            return ok(create.render(user, roundid, endForm, end+1, curScore));
        }
    }

    @Security.Authenticated(Secured.class)
    public static Result submitDesktop(int id) {
        Form<Round> filledForm = Form.form(Round.class).bindFromRequest();
        Round r = filledForm.get();

        try {
         
             int rnd_id = Database.addRound(id, r.description);
             // Database.addRound(id, r.description, r.date);                                                // TODO: When date is implemented.
             
             for (int i = 1; i <= 10; i++) {                                                                  // TODO: Replace 10 with numEnds.
                 Database.addEnd(id, rnd_id, i, r.rawEnds[(i*3)-3], r.rawEnds[(i*3)-2], r.rawEnds[(i*3)-1]);
             }
             
        } catch (SQLException e) {
            return badRequest(login.render(userForm, "Something is wrong. Try again."));
        }
        
        User user = Database.getInfo(request().username());
        user.email = request().username();
        System.out.println("6");
        System.out.println(request().username());

        return redirect(routes.Application.dashboard());
    }
    
    // Renders list of rounds, with links to view each round specifically.
    @Security.Authenticated(Secured.class)
    public static Result roundsList() {
        User user = Database.getInfo(request().username());
        user.email = request().username();
        System.out.println("12");
        System.out.println(request().username());
        System.out.println(user.id);
        
        List<Round> rounds = new ArrayList<Round>();
        
        try {
            rounds = Database.getTenRounds(user.id, 1);                      // TODO: Make "Load More" button that increments second parameter.
        } catch (SQLException e) {
            return badRequest(login.render(userForm, "Something is wrong. Try again."));
        }
        
        return ok(roundslist.render(user, rounds));
    }
    
    // Handles deletion of chosen round and renders the resulting success page.
    @Security.Authenticated(Secured.class)
    public static Result deleteRound(int roundid) {
        User user = Database.getInfo(request().username());
        user.email = request().username();
        System.out.println("13");
        System.out.println(request().username());
        System.out.println(user.id);

        if (Database.deleteRound(roundid))
            return redirect(routes.Application.roundsList());
        else
            return badRequest(login.render(userForm, "Something is wrong. Try again."));
    }
    
    // Renders list of ends of a round.
    @Security.Authenticated(Secured.class)
    public static Result viewAllEnds(int roundid) {
        User user = Database.getInfo(request().username());
        user.email = request().username();
        System.out.println("14");
        System.out.println(request().username());
        System.out.println(user.id);
        
        Round round = new Round();
        
        try {
            round = Database.getRoundInfo(roundid);
        } catch (SQLException e) {
            return badRequest(login.render(userForm, "Something is wrong. Try again."));
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
