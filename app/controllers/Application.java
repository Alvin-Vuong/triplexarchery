package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.data.Form;
import views.html.*;
import models.*;
import java.util.List;
import java.util.ArrayList;
import java.sql.SQLException;
import org.jasypt.util.digest.*;
import org.jasypt.util.password.*;
import play.libs.Json;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Application extends Controller {
    
    final static Form<User> userForm = Form.form(User.class);
    final static Form<End> endForm = Form.form(End.class);
    final static Form<Round> roundForm = Form.form(Round.class);
    
    // Redirect to dashboard if logged in; homepage if not.
    @Security.Authenticated(Secured.class)
    public static Result index() {
        request().username();
        return redirect(routes.Application.dashboard());
    }
    
    // Renders homepage.
    public static Result home() {
        return ok(index.render(userForm));
    }
    
    // Handle login, redirects to dashboard or renders login.
    public static Result login() {
        Form<User> filledForm = userForm.bindFromRequest();
        User user = filledForm.get();

        try {
            StrongPasswordEncryptor encryptor = new StrongPasswordEncryptor();
            String retrievedPW = Database.getPW(user.email);

            if (!retrievedPW.equals("") && encryptor.checkPassword(user.password, retrievedPW)) {
                User userQuery = Database.getInfo(user.email);
                userQuery.email = user.email;
                userQuery.password = user.password;
                // TODO: 911
                if (userQuery.id == 0) {
                    return internalServerError(login.render(filledForm, "Something is wrong. Try again."));
                } else {
                    session().clear();
                    session("email", userQuery.email);
                    return redirect(routes.Application.dashboard());
                }
            } else {
                return unauthorized(login.render(filledForm, "Incorrect email or password."));
                // TODO: fix navbar on failed login -- says "logout" when not logged in
            }
        } catch (SQLException e) {
            return internalServerError(login.render(filledForm, "Something is wrong. Try again."));
        }
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
        User user = Database.getInfo(request().username());
        user.email = request().username();
        return ok(dash.render(user));
    }
    
    // Renders registration form.
    public static Result register() {
        return ok(register.render(userForm, ""));
    }
    
    // Handles registration, redirects to login or renders re-register.
    public static Result verify() {
        Form<User> filledForm = userForm.bindFromRequest();
        User created = filledForm.get();
        
        try {
            boolean log = Database.registerCheck(created.email);
            // Valid email
            if (log) {
                StrongPasswordEncryptor encryptor = new StrongPasswordEncryptor();
                String hashedPassword = encryptor.encryptPassword(created.password);
                Database.registerNew(created.email, hashedPassword, created.firstname, created.lastname);
                return redirect(routes.Application.registrationSuccess());
            }
            // Redirect to register again
            else {
                return unauthorized(register.render(filledForm, created.email + " already has an account."));
            }
        } catch (SQLException e) {
            return internalServerError(register.render(filledForm, "Something is wrong. Try again."));
        }
    }
    
    // Renders about page.
    public static Result about() {
        return ok(about.render());
    }
    
    // Handles round description entry and creation.
    // Renders beginning of ends entry.
    @Security.Authenticated(Secured.class)
    public static Result create() {
        Form<Round> filledForm = Form.form(Round.class).bindFromRequest();
        Round r = filledForm.get();
        String d = r.description;
        
        User user = Database.getInfo(request().username());
        user.email = request().username();
        
        try {
            int r_id = Database.addRound(user.id, d);
            return redirect(routes.Application.endSubmission(r_id, 1));
        } catch (SQLException e){
            return internalServerError(login.render(userForm, "Something is wrong. Try again."));
        }
    }

    // Renders end submission page in end-by-end round entry.
    // for POST-redirect-GET design
    @Security.Authenticated(Secured.class)
    public static Result endSubmission(int r_id, int end) {
        User user = Database.getInfo(request().username());
        user.email = request().username();

        int curScore = -1;
        try {
            curScore = Database.getScore(user.id, r_id);
        } catch (SQLException e) {
            return internalServerError(login.render(userForm, "Something is wrong. Try again."));
        }

        return ok(create.render(user, r_id, endForm, end, curScore));
    }

    // Renders round entry form for round that's completed.
    @Security.Authenticated(Secured.class)
    public static Result createDesktop() {
        User user = Database.getInfo(request().username());
        user.email = request().username();
        
        return ok(createDesktopRound.render(user, roundForm));
    }
    
    // Handles end entry, renders next end entry. (Loop)
    // Renders dashboard when complete.
    @Security.Authenticated(Secured.class)
    public static Result submit(int roundid, int end) {
        Form<End> filledForm = endForm.bindFromRequest();
        End created = filledForm.get();
        created.endNumber = end;
        String[] x = {created.a1, created.a2, created.a3};
        
        User user = Database.getInfo(request().username());
        user.email = request().username();
        
        try {
            Database.addEnd(user.id, roundid, end, created.a1, created.a2, created.a3);    
        } catch (SQLException e) {
            return internalServerError(login.render(userForm, "Something is wrong. Try again."));
        }
        
        if (end == 10) {                                                // TODO: 10 can be replaced with numEnds
            return redirect(routes.Application.roundsList());
        } else {
            return redirect(routes.Application.endSubmission(roundid, end+1));
        }
    }

    // Renders page for entering completed round.
    @Security.Authenticated(Secured.class)
    public static Result submitDesktop() {
        User user = Database.getInfo(request().username());
        user.email = request().username();
        int id = user.id;
        Form<Round> filledForm = Form.form(Round.class).bindFromRequest();
        Round r = filledForm.get();

        try {
         
             int rnd_id = Database.addRound(id, r.description);
             // Database.addRound(id, r.description, r.date);                                                // TODO: When date is implemented.
             
             for (int i = 1; i <= 10; i++) {                                                                  // TODO: Replace 10 with numEnds.
                 Database.addEnd(id, rnd_id, i, r.rawEnds[(i*3)-3], r.rawEnds[(i*3)-2], r.rawEnds[(i*3)-1]);
             }
             
        } catch (SQLException e) {
            return internalServerError(login.render(userForm, "Something is wrong. Try again."));
        }

        return redirect(routes.Application.roundsList());
    }

	// Renders page for entering edited round
    @Security.Authenticated(Secured.class)
    public static Result submitEdit(int rnd_id) {
        User user = Database.getInfo(request().username());
        user.email = request().username();
        int id = user.id;
        Form<Round> filledForm = Form.form(Round.class).bindFromRequest();
        Round r = filledForm.get();
		List<Round> rounds = new ArrayList<Round>();

/*		for (int i = 0; i <= 29; i++) {                                                               
        		if (r.rawEnds[i].equals("m"))
			{}
			else if (r.rawEnds[i].equals("X"))
			{}
			else if (r.rawEnds[i].equals("1"))
			{}
			else if (r.rawEnds[i].equals("2"))
			{}
			else if (r.rawEnds[i].equals("3"))
			{}
			else if (r.rawEnds[i].equals("4"))
			{}
			else if (r.rawEnds[i].equals("5"))
			{}
			else if (r.rawEnds[i].equals("6"))
			{}
			else if (r.rawEnds[i].equals("7"))
			{}
			else if (r.rawEnds[i].equals("8"))
			{}
			else if (r.rawEnds[i].equals("9"))
			{}
			else if (r.rawEnds[i].equals("10"))
			{}
			else
				try {
         		   rounds = Database.getTenRounds(user.id, 1);
				} catch (SQLException e) {
            			return internalServerError(login.render(userForm, "Something is wrong. Try again."));
       			}
				return unauthorized(roundslist.render(user, rounds));
		}  */
        try {
			int total = 0;
			for (int i = 0; i <= 29; i++) {
				total += toValue(r.rawEnds[i], true);
			}			

            for (int i = 1; i <= 10; i++) {                                                                  
                Database.editEnd(id, rnd_id, i, r.rawEnds[(i*3)-3], r.rawEnds[(i*3)-2], r.rawEnds[(i*3)-1], total);
            }
             
        } catch (SQLException e) {
            return internalServerError(login.render(userForm, "Something is wrong. Try again."));
        }

        return redirect(routes.Application.roundsList());
    }
    
    // Renders list of rounds, with links to view each round specifically.
    @Security.Authenticated(Secured.class)
    public static Result roundsList() {
        User user = Database.getInfo(request().username());
        user.email = request().username();        
        List<Round> rounds = new ArrayList<Round>();
        
        try {
            rounds = Database.getTenRounds(user.id, 1);
        } catch (SQLException e) {
            return internalServerError(login.render(userForm, "Something is wrong. Try again."));
        }
        
        return ok(roundslist.render(user, rounds));
    }

    // Renders list of rounds for infinite scroll, to be appended to rounds list page.
    @Security.Authenticated(Secured.class)
    public static Result loadRounds(Integer set) {
        User user = Database.getInfo(request().username());
        user.email = request().username();
        List<Round> rounds = new ArrayList<Round>();

        try {
            rounds = Database.getTenRounds(user.id, set);
        } catch (SQLException e) {
            return internalServerError(login.render(userForm, "Something is wrong. Try again."));
        }

        List<JsonNode> rlist = new ArrayList<JsonNode>();
        for (Round r : rounds) {
            ObjectNode round = Json.newObject();
            round.put("id", r.id);
            round.put("description", r.description);
            round.put("date", r.date);
            round.put("score", r.score);
            rlist.add(round);
        }

        JsonNode roundSet = Json.toJson(rlist);
        ObjectNode result = Json.newObject();
        if (rounds.size() < 10) {
            result.put("end", true);
        } else {
            result.put("end", false);
        }
        result.put("rounds", roundSet);
        return ok(result);
    }
    
    // Handles deletion of chosen round and renders the resulting success page.
    @Security.Authenticated(Secured.class)
    public static Result deleteRound(int roundid) {
        User user = Database.getInfo(request().username());
        user.email = request().username();

        if (Database.deleteRound(roundid))
            return redirect(routes.Application.roundsList());
        else
            return internalServerError(login.render(userForm, "Something is wrong. Try again."));
    }
    
    // Renders list of ends of a round.
    @Security.Authenticated(Secured.class)
    public static Result viewAllEnds(int roundid) {
        User user = Database.getInfo(request().username());
        user.email = request().username();
        
        Round round = new Round();
        
        try {
            round = Database.getRoundInfo(roundid);
            
            // Prevents URL Injection by verifying the logged in user is accessing his/her own data.
            if (user.id != Database.getAccount(roundid))
                return unauthorized(error.render(user));
            
        } catch (SQLException e) {
            return internalServerError(login.render(userForm, "Something is wrong. Try again."));
        }
        
        return ok(end.render(user, round.score, round.ends, round.description, round.date));       // TODO: Redirect back to round list.
    }
    
	//Renders edit page for a round
	@Security.Authenticated(Secured.class)
    public static Result viewEndEditor(int roundid) {
        User user = Database.getInfo(request().username());
        user.email = request().username();
        
        Round round = new Round();
        
        try {
            round = Database.getRoundInfo(roundid);
            
            // Prevents URL Injection by verifying the logged in user is accessing his/her own data.
            if (user.id != Database.getAccount(roundid))
                return unauthorized(error.render(user));
            
        } catch (SQLException e) {
            return internalServerError(login.render(userForm, "Something is wrong. Try again."));
        }
        
        return ok(editRound.render(user, roundid, round.ends, round.description, round.date, roundForm)); 	//TODO: EVERYTHINGGGGGGGGGGGGGGGGGGG
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

    public static Result loadActivity(String pageNumber) {

        /*try {
            Thread.sleep(2000);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        String fakeActivity = "<tr>" + 
                                  "<td class=\"col-xs-1\"><a href=\"#\"><span class=\"glyphicon glyphicon-plus black\"></span></a></td>" +
                                  "<td><a href=\"#\">You shot a new round, with score: 260.</a></td>" +
                                  "<td class=\"activity-time\"><a href=\"#\">5 days ago</a></td>" +
                              "</tr>" +
                              "<tr>" + 
                                  "<td class=\"col-xs-1\"><a href=\"#\"><span class=\"glyphicon glyphicon-plus black\"></span></a></td>" +
                                  "<td><a href=\"#\">You shot a new round, with score: 260.</a></td>" +
                                  "<td class=\"activity-time\"><a href=\"#\">5 days ago</a></td>" +
                              "</tr>" +
                              "<tr>" + 
                                  "<td class=\"col-xs-1\"><a href=\"#\"><span class=\"glyphicon glyphicon-plus black\"></span></a></td>" +
                                  "<td><a href=\"#\">You shot a new round, with score: 260.</a></td>" +
                                  "<td class=\"activity-time\"><a href=\"#\">5 days ago</a></td>" +
                              "</tr>" +
                              "<tr>" + 
                                  "<td class=\"col-xs-1\"><a href=\"#\"><span class=\"glyphicon glyphicon-plus black\"></span></a></td>" +
                                  "<td><a href=\"#\">You shot a new round, with score: 260.</a></td>" +
                                  "<td class=\"activity-time\"><a href=\"#\">5 days ago</a></td>" +
                              "</tr>" +
                              "<tr>" + 
                                  "<td class=\"col-xs-1\"><a href=\"#\"><span class=\"glyphicon glyphicon-plus black\"></span></a></td>" +
                                  "<td><a href=\"#\">You shot a new round, with score: 260.</a></td>" +
                                  "<td class=\"activity-time\"><a href=\"#\">5 days ago</a></td>" +
                              "</tr>" +
                              "<tr>" + 
                                  "<td class=\"col-xs-1\"><a href=\"#\"><span class=\"glyphicon glyphicon-plus black\"></span></a></td>" +
                                  "<td><a href=\"#\">You shot a new round, with score: 260.</a></td>" +
                                  "<td class=\"activity-time\"><a href=\"#\">5 days ago</a></td>" +
                              "</tr>" +
                              "<tr>" + 
                                  "<td class=\"col-xs-1\"><a href=\"#\"><span class=\"glyphicon glyphicon-plus black\"></span></a></td>" +
                                  "<td><a href=\"#\">You shot a new round, with score: 260.</a></td>" +
                                  "<td class=\"activity-time\"><a href=\"#\">5 days ago</a></td>" +
                              "</tr>" +
                              "<tr>" + 
                                  "<td class=\"col-xs-1\"><a href=\"#\"><span class=\"glyphicon glyphicon-plus black\"></span></a></td>" +
                                  "<td><a href=\"#\">You shot a new round, with score: 260.</a></td>" +
                                  "<td class=\"activity-time\"><a href=\"#\">5 days ago</a></td>" +
                              "</tr>" +
                              "<tr>" + 
                                  "<td class=\"col-xs-1\"><a href=\"#\"><span class=\"glyphicon glyphicon-plus black\"></span></a></td>" +
                                  "<td><a href=\"#\">You shot a new round, with score: 260.</a></td>" +
                                  "<td class=\"activity-time\"><a href=\"#\">5 days ago</a></td>" +
                              "</tr>" +
                              "<tr>" + 
                                  "<td class=\"col-xs-1\"><a href=\"#\"><span class=\"glyphicon glyphicon-plus black\"></span></a></td>" +
                                  "<td><a href=\"#\">You shot a new round, with score: 260.</a></td>" +
                                  "<td class=\"activity-time\"><a href=\"#\">5 days ago</a></td>" +
                              "</tr>";

        ObjectNode result = Json.newObject();
        result.put("end", true);
        result.put("html", "<tr><td colspan=\"3\"><p class=\"text-muted\">No more news.</p></td></tr>");
        //result.put("end", false);
        //result.put("html", fakeActivity);
        return ok(result);*/
        return ok("to be implemented");
    }

}
