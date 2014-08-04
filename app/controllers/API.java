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

import play.libs.Json;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class API extends Controller {

	public static Result sayHello() {
		// for testing from CLI
		// curl --header "Content-type: application/json" --request POST --data '{"name": "NAME"}' http://localhost:9000/api/hello
		JsonNode json = request().body().asJson();

		if (json == null) {
			return badRequest("Expecting Json data");
		} else {
			String name = json.findPath("name").textValue();

			if (name == null) {
				return badRequest("Missing name");
			}

			ObjectNode result = Json.newObject();
			result.put("status", "OK");
			result.put("message", "Hello " + name);
			return ok(result);
		}
	}

	// Returns Json of user info: first name, email, ID
	public static Result getUser(String key) {
		// for testing from CLI
		// curl --header "Content-type: application/json" --request POST --data '{"email": "me@me", "password": "me"}' http://localhost:9000/api_key={key}/get-user

		//TODO: create database table of API keys, implement a real checker
		if (!key.equals("1234")) {
			return badRequest("Invalid API key");
		}

		JsonNode json = request().body().asJson();
		
		if (json == null) {
			return badRequest("Expecting Json data");
		} else {
			String em = json.findPath("email").textValue();
			String pw = json.findPath("password").textValue();

			if (em == null || pw == null) {
				return badRequest("Missing params");
			} else {
				boolean log = false;

				try {
					log = Database.login(em, pw);
				} catch (SQLException e) {
					return badRequest("Internal Server Error 500");
				}

				ObjectNode result = Json.newObject();
				if (log) {
					User userQuery = Database.getInfo(em);
                	userQuery.email = em;
					result.put("status", "OK");
					result.put("firstname", userQuery.firstname);
					result.put("email", em);
					result.put("ID", userQuery.id);
				} else {
					result.put("status", "Failed");
					result.put("message", "Incorrect email or password");
				}
				return ok(result);
			}
		}
	}

}