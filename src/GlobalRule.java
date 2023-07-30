import java.util.Set;

public class GlobalRule {
    // Name of the global rule
    private String globalRule;
    // Data type referred to global rule
    private String dataType;
    // Second data type referred to global rule (only for TOGETHER, NOT_TOGETHER and TOGETHER*)
    private String dataType2;
    //
    private SetOfRules set1;
    //
    private SetOfRules set2;

    public GlobalRule(){
        globalRule = "";
        dataType = "";
        dataType2 = "";
    }

    // Set methods
    public void setGlobalRule(String globalRule) {
        this.globalRule = globalRule;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public void setDataType2(String dataType2) {
        this.dataType2 = dataType2;
    }

    public void setSet1(SetOfRules set1) {
        this.set1 = set1;
    }

    public void setSet2(SetOfRules set2) {
        this.set2 = set2;
    }

    // Get methods
    public String getGlobalRule() {
        return globalRule;
    }

    public String getDataType() {
        return dataType;
    }

    public String getDataType2() {
        return dataType2;
    }

    public SetOfRules getSet1() {
        return set1;
    }

    public SetOfRules getSet2() {
        return set2;
    }
}
