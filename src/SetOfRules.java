import java.util.ArrayList;
import java.util.Objects;

public class SetOfRules {
    // Indicates the type of SetOfRule "o" or "c"
    private String type;
    // Indicates the index of SetOfRule (for the type "c" SetOfRule only)
    private int index;
    // Indicates the category of data referenced by SetOfRule
    private String data;
    // Indicates the number of copies referenced by SetOfRule (for the type "c" SetOfRule only)
    private int numberOfCopy;
    // Terms of the SetOfRule
    private ArrayList<Term> terms;
    // Rules of the SetOfRule
    private ArrayList<Rule> rules;

    public SetOfRules(){
        type = "";
        data = "";
        numberOfCopy = 0;
        terms = new ArrayList<>();
        rules = new ArrayList<>();
    }

    // This method returns true if every arraylist of v that contains the values
    // to be checked for a rule or term meets the rule or term, false otherwise.
    // Set of Rule satisfaction according Definition 3.3.2 at pag.38
    // Logical formula : term_1 AND ... AND term_h AND rule_1 AND ... AND rule_k
    public boolean verify(ArrayList<ArrayList<String>> v){
        boolean result = true;
        if (terms.size() > 0 || rules.size() > 0){
            for (int i = 0; i<terms.size(); i++) {
                result = result && terms.get(i).verify(v.get(i).get(0));
            }
            for (int j = 0; j<rules.size(); j++) {
                result = result && rules.get(j).verify(v.get(j+terms.size()));
            }
            return result;
        } else {
            return false;
        }
    }

    // Returns an ArrayList of ArrayLists that contains the values needed for the "verify" methods of rules and terms.
    // Each of these arraylists refers to a rule or term of this SetOfRule.
    public ArrayList<ArrayList<String>> arrayList_list_for_verify(){
        ArrayList<ArrayList<String>> result = new ArrayList<>();
        for (Term t : terms) {
            result.add(new ArrayList<>(){{add(t.getAttribute());}});
        }
        for (Rule r : rules) {
            result.add(r.arrayList_list_for_verify());
        }
        return result;
    }

    // Add rule to ArrayList rules
    public void addRule(Rule r) {
        rules.add(r);
    }

    // Add term to ArrayList terms
    public void addTerm(Term t) {
        terms.add(t);
    }

    // Set methods
    public void setType(String type) {
        this.type = type;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setNumberOfCopy(int np) {
        this.numberOfCopy = np;
    }

    // Get methods
    public String getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }

    public String getData() {
        return data;
    }

    public int getNumberOfCopy() {
        return numberOfCopy;
    }

    public ArrayList<Term> getTerms() {
        return terms;
    }

    public ArrayList<Rule> getRules() {
        return rules;
    }

    // Print only the rule name
    public String printRuleName(){
        StringBuilder result;
        if (Objects.equals(this.type, "o"))
            result = new StringBuilder("R_" + this.type + "(" + this.data + ")");
        else
            result = new StringBuilder("R_" + this.type + this.index + "(" + this.data + ")");
        return result.toString();
    }

    // Print the SetOfRules
    @Override
    public String toString() {
        StringBuilder result;
        if (Objects.equals(this.type, "o"))
            result = new StringBuilder("R_" + this.type + "(" + this.data + ")" + " = {");
        else
            result = new StringBuilder("R_" + this.type + this.index + "(" + this.data + ")" + " = {");
        for (Term t : this.terms)
            result.append("\n\t\t").append(t.toString());
        for (Rule r : this.rules)
            result.append("\n\t\t").append(r.toString());
        result.append("\n\t}");
        return result.toString();
    }
}
