package models;

public class End {
    public int endNumber;
    public String a1, a2, a3;
    public int total;
    
    public End() {
    }
    
    public End(String a, String b, String c, int e, int t) {
        endNumber = e;
        a1 = a;
        a2 = b;
        a3 = c;
        total = t;
    }

//  FOR TESTING
    public String toString() {
        return("End " + endNumber + ": " + a1 + " " + a2 + " " + a3 + ".");
    }
//
}