package models;

import java.util.List;

public class Round {
    public String description, date;
    public int score, id;
    public List<End> ends;
    
    public Round() {
    }
    
    public Round(int round_id, int sc, String desc, String d, List<End> e) {
        description = desc;
        date = d;
        score = sc;
        id = round_id;
        ends = e;
    }
    
//  FOR TESTING
    public String toString() {
        return("Round " + id + ": " + score + ", " + description + ", " + date + ".");
    }
//    
}