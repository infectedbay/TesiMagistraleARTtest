import java.util.ArrayList;

public class Term {
    // Attribute on which the term is defined
    private String attribute;
    // Values
    private ArrayList<String> values;
    // False if the term is denied, true otherwise
    private boolean includeValues;

    public Term(){
        attribute = "";
        values = new ArrayList<>();
        includeValues = true;
    }

    // This method returns true if the value v meets the term, false otherwise
    // Term satisfaction according Definition 3.3.1 at pag.38
    // Logical formula : term
    public boolean verify(String v)  {
        if (includeValues)
            return values.contains(v);
        else
            return !(values.contains(v));
    }

    // Add value to ArrayList
    public void addValue(String value){
        values.add(value);
    }

    // Set methods
    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public void setIncludeValues(boolean include_values) {
        this.includeValues = include_values;
    }

    // Get methods
    public String getAttribute() {
        return attribute;
    }

    public ArrayList<String> getValues() {
        return values;
    }

    public boolean getIncludeValues () {
        return includeValues;
    }

    // Print the term
    @Override
    public String toString() {
        StringBuilder result;
        if (includeValues)
            result = new StringBuilder(this.attribute + "(");
        else
            result = new StringBuilder("!" + this.attribute + "(");
        for (int i=0; i<values.size(); i++){
            if (i != values.size() - 1)
                result.append(values.get(i)).append(",");
            else
                result.append(values.get(i)).append(")");
        }
        return result.toString();
    }
}