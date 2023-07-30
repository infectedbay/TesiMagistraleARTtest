import java.util.ArrayList;

public class Preference {
    // Preference relation (set of sets of value)
    ArrayList<ArrayList<String>> preferenceRelation;
    // Score function for all set of value
    ArrayList<Double> score;
    // Weight function for the attribute
    int weight;

    public Preference(){
        this.preferenceRelation = new ArrayList<>();
        this.score = new ArrayList<>();
        this.weight = 1;
    }

    // Add set of value to preferenceRelation
    public void addSetOfPreference(ArrayList<String> preference) {
        this.preferenceRelation.add(preference);
    }

    // Add score value to score list
    public void addScore(double score) {
        this.score.add(score);
    }


    // Set methods
    public void setPreferenceRelation(ArrayList<ArrayList<String>> preferenceRelation) {
        this.preferenceRelation = preferenceRelation;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    // Get methods
    public ArrayList<ArrayList<String>> getPreferenceRelation() {
        return preferenceRelation;
    }

    public ArrayList<Double> getScore() {
        return score;
    }

    public int getWeight() {
        return weight;
    }

    // Support the "getScoreFromValue" method
    private int getIndexOfValue(String value) {
        int i=0;
        for (ArrayList<String> setOfValue: preferenceRelation) {
            if (setOfValue.contains(value))
                return i;
            i=i+1;
        }
        return -1;
    }

    private double getScoreFromIndex(int index) {
        return score.get(index);
    }

    // Get score from value
    public double getScoreFromValue (String value) {
        return getScoreFromIndex(getIndexOfValue(value));
    }

    // Get top value
    public String getTopValue () {
        return this.preferenceRelation.get(0).get(0);
    }

    // Get top score
    public double getTopScore () {
        return this.score.get(0);
    }
}
