import java.util.ArrayList;

public class ScoreVector {
    // List of attribute (necessary for printing)
    ArrayList<String> attributes;
    // The plan value for each attribute in the requirements set preferences
    ArrayList<String> valuesOfInterest;
    // Score vector or weighed score vector
    ArrayList<Double> vector;
    // Distance from p_top
    double distance;

    public  ScoreVector() {
        this.valuesOfInterest = new ArrayList<>();
        this.vector = new ArrayList<>();
        this.distance = 0.0;
        this.attributes = new ArrayList<>();
    }

    // Calculates the distance from another plan and puts the result in distance
    public void calculate_distance(ScoreVector ideal_plan){
        double temp = 0.0;
        for (int i=0; i< ideal_plan.getVector().size(); i++) {
            temp = temp + Math.pow((ideal_plan.getVector().get(i) - this.vector.get(i)),2);
        }
        this.distance = Math.sqrt(temp);
    }

    // Add methods
    public void add_attribute(String v) {
        this.attributes.add(v);
    }

    public void add_value(String v) {
        this.valuesOfInterest.add(v);
    }

    public void add_score(Double s) {
        this.vector.add(s);
    }

    // Get methods
    public ArrayList<Double> getVector() {
        return this.vector;
    }

    public ArrayList<String> getValuesOfInterest() {
        return this.valuesOfInterest;
    }

    public Double getDistance() {
        return this.distance;
    }

    public ArrayList<String> getAttributes() {
        return attributes;
    }
}
