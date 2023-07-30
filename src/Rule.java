import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Rule {
    // Name of the rule "ALL", "ANY", "IF-THEN", "FORBIDDEN", "AT_MOST" or "AT_LEAST"
    private String ruleName;
    // Terms of the rule
    private ArrayList<Term> terms;
    // Second part of the terms of the rule (for the "IF-THEN" rule only)
    private ArrayList<Term> terms2;
    // quantity (for the rules "AT_MOST" and "AT_LEAST" only)
    private int quantity;

    public Rule(){
        ruleName = "";
        terms = new ArrayList<>();
        terms2 = new ArrayList<>();
        quantity = 0;
    }

    // Return list of attribute required for verify the rule
    public ArrayList<String> arrayList_list_for_verify(){
        ArrayList<String> result = new ArrayList<>();
        if(Objects.equals(ruleName, "IF-THEN")){
            for(Term t1 : terms){
                result.add(t1.getAttribute());
            }
            for(Term t2 : terms2){
                result.add(t2.getAttribute());
            }
        } else {
            for(Term t : terms){
                result.add(t.getAttribute());
            }
        }
        return result;
    }

    // This method returns true if the arraylist of v values matches all terms of the rule, false otherwise
    public boolean verify (ArrayList<String> v){
        boolean result = false;
        boolean result2 = false;
        int count = 0;
        assert (v.size() == terms.size()) || (v.size() == terms.size() + terms2.size());
        switch (ruleName){
            // Verify for "ALL" rule
            // Logical formula : term_1 AND ... AND term_h
            case "ALL" -> {
                result = true;
                for (int i=0; i<terms.size(); i++){
                    result = result && terms.get(i).verify(v.get(i));
                }
            }
            // Verify for "ANY" rule
            // Logical formula : term_1 OR ... OR term_h
            case "ANY" -> {
                result = false;
                for (int i=0; i<terms.size(); i++){
                    result = result || terms.get(i).verify(v.get(i));
                }
            }
            // Verify for "FORBIDDEN" rule
            // Logical formula : !term_1 AND ... AND !term_h
            case "FORBIDDEN" -> {
                result = false;
                for (int i=0; i<terms.size(); i++){
                    result = result || !terms.get(i).verify(v.get(i));
                }
            }
            // Verify for "AT_MOST" rule
            // Logical formula : |T| <= quantity, with T the set of terms satisfied
            case "AT_MOST" -> {
                count = 0;
                for (int i=0; i<terms.size(); i++){
                    if (terms.get(i).verify(v.get(i)))
                        count = count + 1;
                }
                return count <= quantity;
            }
            // Verify for "AT_LEAST" rule
            // Logical formula : |T| >= quantity, with T the set of terms satisfied
            case "AT_LEAST" -> {
                count = 0;
                for (int i=0; i<terms.size(); i++){
                    if (terms.get(i).verify(v.get(i)))
                        count = count + 1;
                }
                return count >= quantity;
            }
            // verify for "IF-THEN" rule
            // Logical formula : (term_1 AND ... AND term_h) --> (term_1 OR ... OR term_k)
            case "IF-THEN" -> {
                result = true;
                int i;
                for (i=0; i<terms.size(); i++){
                    result = result && terms.get(i).verify(v.get(i));
                }
                result2 = false;
                for (int j=0; j<terms2.size(); j++){
                    result2 = result2 || terms2.get(j).verify(v.get(i));
                    i = i + 1;
                }
                result = !result || result2; // result -> result2 is equal to !result or result2
            }
        }
        return result;
    }

    // Add term to ArrayList term
    public void addTerm(Term t){
        terms.add(t);
    }

    // Add term to ArrayList term2
    public void addTerm2(Term t){
        terms2.add(t);
    }

    // Set methods
    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Get methods
    public String getRuleName() {
        return ruleName;
    }

    public ArrayList<Term> getTerms() {
        return terms;
    }

    public ArrayList<Term> getTerms2() {
        return terms2;
    }

    public int getQuantity() {
        return quantity;
    }

    // Print the rule
    @Override
    public String toString() {
        StringBuilder result;
        switch (this.ruleName) {
            case "ANY", "FORBIDDEN", "ALL" -> {
                result = new StringBuilder(this.ruleName + "({");
                for (int i = 0; i < terms.size(); i++) {
                    if (i != terms.size() - 1)
                        result.append(terms.get(i).toString()).append(", ");
                    else
                        result.append(terms.get(i).toString()).append("})");
                }
            }
            case "AT_MOST", "AT_LEAST" -> {
                result = new StringBuilder(this.ruleName + "(" + quantity + ", {");
                for (int i = 0; i < terms.size(); i++) {
                    if (i != terms.size() - 1)
                        result.append(terms.get(i).toString()).append(", ");
                    else
                        result.append(terms.get(i).toString()).append("})");
                }
            }
            case "IF-THEN" -> {
                result = new StringBuilder("IF ALL({");
                for (int i = 0; i < terms.size(); i++) {
                    if (i != terms.size() - 1)
                        result.append(terms.get(i).toString()).append(", ");
                    else
                        result.append(terms.get(i).toString()).append("})");
                }
                result.append(" THEN ANY({");
                for (int i = 0; i < terms2.size(); i++) {
                    if (i != terms2.size() - 1)
                        result.append(terms2.get(i).toString()).append(", ");
                    else
                        result.append(terms2.get(i).toString()).append("})");
                }
            }
            default -> {
                return "the rule is malformed";
            }
        }
        return result.toString();
    }
}
