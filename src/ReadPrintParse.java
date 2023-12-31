import java.io.*;
import java.util.*;

public class ReadPrintParse {

    // Check syntax of each rule character by character
    public static void rules_parser(File[] files_list, ArrayList<String> data_order_list,
                                    ArrayList<String> attribute_order_list,
                                    LinkedHashMap<String, ArrayList<String>> attributes) throws IOException {
        for (File f : files_list){
            FileReader fr = new FileReader(f);              //Creation of File Reader object
            BufferedReader br = new BufferedReader(fr);     //Creation of BufferedReader object
            String m = "The syntax of the rule file " + f.getName() + " is MALFORMED!";
            StringBuilder error_reason = new StringBuilder();
            int c;
            int count = 0;
            int phase = 0;
            boolean inside_a_role = false;
            boolean if_block = false;
            boolean correct = false;
            boolean not_term = false;
            boolean type_c = false;
            String cipher_data = "";        // ONLY FOR ERROR OUTPUT
            StringBuilder temp = new StringBuilder();
            StringBuilder temp2 = new StringBuilder();


            while ((c = br.read()) != -1) {
                if (c != '\n' && c != '\r' && c != ' ') {
                    // Check if signature of set of rule is correct
                    // Check if the data in the rule signature exists
                    if (phase == 0) {
                        if (count == 0 && c == 'R') {
                            count = 1;
                        } else if (count == 1 && c == '_') {
                            count = 2;
                        } else if (count == 2 && c == 'o') {
                            count = 201;
                        } else if (count == 201 && c == '(') {
                            count = 4;
                        } else if (count == 2 && c == 'c') {
                            count = 3;
                            type_c = true;
                        } else if (count == 3 && Character.isDigit(c)) {
                            count = 301;
                        } else if (count == 301 && (Character.isDigit(c) || c == '(')) {
                            if (c == '(') {
                                count = 4;
                            }
                        } else if (count == 4 && (Character.isAlphabetic(c) || c == '*')) {
                            temp.append((char) c);
                        } else if (count == 4 && c == ',' && type_c) {
                            count = 401;
                        } else if (count == 401 && Character.isDigit(c)) {
                            count = 402;
                        } else if (count == 402 && (Character.isDigit(c) || c == ')')) {
                            if (c == ')') {
                                if (temp.substring(temp.toString().length() - 1).equals("*")) {
                                    temp = new StringBuilder(temp.substring(0, temp.toString().length() - 1));
                                    cipher_data = "*";
                                }
                                if (Main.is_a_data(temp.toString(), data_order_list))
                                    count = 5;
                                else {
                                    count = -1;
                                    error_reason.append("\n\t\"").append(temp).append(cipher_data).append("\"").append(" is not a data type!");
                                }
                            }
                        } else if (count == 4 && c == ')') {
                            if (temp.substring(temp.toString().length() - 1).equals("*")) {
                                temp = new StringBuilder(temp.substring(0, temp.toString().length() - 1));
                                cipher_data = "*";
                            }
                            if (Main.is_a_data(temp.toString(), data_order_list))
                                count = 5;
                            else {
                                count = -1;
                                error_reason.append("\n\t\"").append(temp).append(cipher_data).append("\"").append(" is not a data type!");
                            }
                        } else if (count == 5 && c == '=') {
                            count = 6;
                        } else if (count == 6 && c == '{') {
                            phase = 1;
                            count = 0;
                            temp = new StringBuilder();
                        } else {
                            Main.stop(m + error_reason);
                        }
                    // Check if attribute exist
                    // Check if value of attribute exist
                    // Check if each term value exists
                    // Check if name of rule exist
                    // Check if each rule is well-formed
                    // Special check for IF-THEN, AT_MOST and AT_LEAST
                    } else {
                        if (count == 0 && c == '!' && temp.toString().equals("")) {
                            if (!not_term)
                                not_term = true;
                            else {
                                count = -1;
                            }
                        } else if (count == 0 && (Character.isAlphabetic(c) || c == '_')) {
                            temp.append((char) c);
                            if (not_term)
                                not_term = false;
                        } else if (count == 0 && c == '(') {
                            if (Main.is_an_attribute(temp.toString(), attribute_order_list)) {
                                temp2 = temp;
                                temp = new StringBuilder();
                                count = 1;
                            } else if (Main.is_a_rule(temp.toString()) && !inside_a_role) {
                                if (temp.toString().equals("AT_LEAST") || temp.toString().equals("AT_MOST")) {
                                    count = 110;
                                } else {
                                    count = 101;
                                }
                                temp = new StringBuilder();
                                inside_a_role = true;
                            } else if (!inside_a_role && temp.toString().equals("IFALL")) {
                                temp = new StringBuilder();
                                inside_a_role = true;
                                if_block = true;
                                count = 101;
                            } else if (inside_a_role && if_block && temp.toString().equals("THENANY")) {
                                temp = new StringBuilder();
                                if_block = false;
                                count = 101;
                            } else {
                                count = -1;
                                error_reason.append("\n\t\"").append(temp).append("\"").append(" is not a rule!");
                            }
                        } else if (count == 101 && c == '{') {
                            count = 0;
                        } else if (count == 110 && (Character.isDigit(c) || c == ',')) {
                            if (c == ',') {
                                count = 101;
                            }
                        } else if (count == 1 && (Character.isAlphabetic(c) || c == '-' || Character.isDigit(c))) {
                            temp.append((char) c);
                        } else if (count == 1 && c == ',') {
                            if (Main.is_a_value_of_an_attribute(temp2.toString(), temp.toString(), attributes)) {
                                temp = new StringBuilder();
                            } else {
                                count = -1;
                                error_reason.append("\n\t\"").append(temp).append("\"").append(" is not present in the set of values for the attribute \"").append(temp2).append("\"!");
                            }
                        } else if (count == 1 && c == ')') {
                            if (Main.is_a_value_of_an_attribute(temp2.toString(), temp.toString(), attributes)) {
                                temp = new StringBuilder();
                                count = 2;
                            } else {
                                count = -1;
                                error_reason.append("\n\t\"").append(temp).append("\"").append(" is not present in the set of values for the attribute \"").append(temp2).append("\"!");
                            }
                        } else if (count == 2 && c == ',') {
                            count = 0;
                            temp = new StringBuilder();
                            temp2 = new StringBuilder();
                        } else if (count == 2 && c == '}' && !inside_a_role) {
                            correct = true;
                        } else if (count == 2 && c == '}'/* && inside_a_role*/) {
                            count = 3;
                        } else if (count == 3 && c == ')' && inside_a_role) {
                            if (if_block) {
                                count = 0;
                                temp = new StringBuilder();
                                temp2 = new StringBuilder();
                            } else {
                                count = 4;
                            }
                        } else if (count == 4 && c == ',' && inside_a_role) {
                            count = 0;
                            temp = new StringBuilder();
                            temp2 = new StringBuilder();
                            inside_a_role = false;
                        } else if (count == 4 && c == '}' && inside_a_role) {
                            correct = true;
                        } else {
                            Main.stop(m + error_reason);
                        }
                    }
                }
            }
            if(!correct) {
                Main.stop(m + error_reason);
            }
        }
    }

    // Reads attributes from files and puts them into the Hashmap
    public static void read_attributes(HashMap<String,ArrayList<String>> attributes,
                                       ArrayList<String> attribute_order_list,
                                       File file_attributes) throws IOException {
        FileReader fr = new FileReader(file_attributes);
        BufferedReader br = new BufferedReader(fr);
        int c;
        int phase = 0;
        String temp_data = "";
        String temp = "";

        while ((c = br.read()) != -1){
            if (c != '\n' && c != '\r') {
                switch (phase) {
                    // Read attribute
                    case 0 -> {
                        if (c != '=') {
                            temp_data = temp_data.concat(String.valueOf((char) c));
                        } else {
                            phase = 1;
                            attributes.put(temp_data, new ArrayList<>());
                            attribute_order_list.add(temp_data);
                        }
                    }
                    // Read values
                    case 1 -> {
                        if (c != '{' && c != ',' && c != '}') {
                            temp = temp.concat(String.valueOf((char) c));
                        } else if (c == ',' || c == '}') {
                            attributes.get(temp_data).add(temp);
                            temp = "";
                        }
                        if (c == '}') {
                            phase = 0;
                            temp_data = "";
                        }
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + phase);
                }
            }
        }
        br.close();
        fr.close();
    }

    // Reads plans from the file and puts them into the Hashmap
    public static void read_plans(HashMap<String,ArrayList<String>> plans, File f) throws IOException {
        FileReader fr = new FileReader(f);              //Creation of File Reader object
        BufferedReader br = new BufferedReader(fr);     //Creation of BufferedReader object
        int c;
        int phase = 0;
        String temp_plan = "";
        String temp = "";

        while ((c = br.read()) != -1){                  //Read char by Char
            if (c != '\n' && c != '\r') {
                switch (phase) {
                    // Read plan name
                    case 0 -> {
                        if (c != '=') {
                            temp_plan = temp_plan.concat(String.valueOf((char) c));
                        } else {
                            phase = 1;
                            plans.put(temp_plan, new ArrayList<>());
                        }
                    }
                    // Read each value of the plan
                    case 1 -> {
                        if (c != '{' && c != ',' && c != '}') {
                            temp = temp.concat(String.valueOf((char) c));
                        } else if (c == ',' || c == '}') {
                            plans.get(temp_plan).add(temp);
                            temp = "";
                        }
                        if (c == '}') {
                            phase = 0;
                            temp_plan = "";
                        }
                    }
                }
            }
        }
        br.close();
        fr.close();
    }

    // Reads data type from the file and puts them into the ArrayList
    public static void read_data(ArrayList<String> data_order_list, File file_data) throws IOException {
        FileReader fr = new FileReader(file_data);      //Creation of File Reader object
        BufferedReader br = new BufferedReader(fr);     //Creation of BufferedReader object
        int c;
        String temp_data = "";

        while ((c = br.read()) != -1) {                 //Read char by Char
            if (c != '{') {
                if (c != ',' && c != '}') {
                    temp_data = temp_data.concat(String.valueOf((char) c));
                } else {
                    data_order_list.add(temp_data);
                    temp_data = "";
                }
            }
        }
        br.close();
        fr.close();
    }

    // Reads SetOfRules from the file list and puts them into the LinkedHashMap
    public static void read_rules(File[] files_list,
                                  LinkedHashMap<String,ArrayList<SetOfRules>> setOfRequirements,
                                  ArrayList<String> attribute_order_list) throws IOException {
        for (File f : files_list){
            FileReader fr = new FileReader(f);              //Creation of File Reader object
            BufferedReader br = new BufferedReader(fr);     //Creation of BufferedReader object
            int c;
            int phase = 0;
            SetOfRules temp_set_of_rule = new SetOfRules();
            boolean cipher_data = false;     // True if the data to be released is encrypted (es. SENSITIVE*)
            String temp = "";
            String temp_data = "";
            boolean temp_bool = true;
            String temp_type = "";

            while ((c = br.read()) != -1){
                if (c != '\n' && c != '\r' && c != ' ') {
                    switch (phase) {
                        // Read the rule set signature
                        case 0 -> {
                            if (c == 'c') {
                                temp_set_of_rule.setType("c");
                                temp_type = "c";
                            } else if (c == 'o') {
                                temp_set_of_rule.setType("o");
                                temp_type = "o";
                            } else if (Character.isDigit(c) && temp_type.equals("c")) {
                                temp = temp + (char) c;
                            } else if (c == '(') {
                                if (temp_type.equals("c")) {
                                    temp_set_of_rule.setIndex(Integer.parseInt(temp));
                                    temp = "";
                                }
                                phase = 1;
                            }
                        }
                        // Read the data type and number of copy from the rule set signature
                        case 1 -> {
                            if (c != ',' && !Character.isDigit(c) && c != ')') {
                                temp_data = temp_data + (char) c;
                                if (c == '*')
                                    cipher_data = true;
                            } else if (Character.isDigit(c) && temp_type.equals("c")) {
                                temp = temp + (char) c;
                            } else if (c == ')') {
                                if (temp_type.equals("c")) {
                                    temp_set_of_rule.setNumberOfCopy(1);
                                    temp = "";
                                }
                                temp_set_of_rule.setData(temp_data);
                                phase = 2;
                            }
                        }
                        // Read '{'
                        case 2 -> {
                            if (c == '{') {
                                phase = 3;
                            }
                        }
                        // Read attribute and rule
                        case 3 -> {
                            if (c != '(' && c != ',' && c != '!') {
                                temp = temp + (char) c;
                            } else if (c == '(') {
                                if(temp.equals("IFALL"))
                                    temp = "IF-THEN";
                                if (Main.is_an_attribute(temp,attribute_order_list)) {
                                    temp_set_of_rule.addTerm(read_term(temp, temp_bool, br));
                                } else if (Main.is_a_rule(temp)) {
                                    temp_set_of_rule.addRule(read_rule(temp, br, attribute_order_list));
                                }
                            } else if (c == '!') {
                                temp_bool = false;
                            } else {    // c == ','
                                temp_bool = true;
                                temp = "";
                            }
                        }
                    }
                }
            }
            // Save the data
            if (cipher_data)
                temp_data = temp_data.substring(0, temp_data.length() - 1);
            if(setOfRequirements.containsKey(temp_data))
                setOfRequirements.get(temp_data).add(temp_set_of_rule);
            else {
                setOfRequirements.put(temp_data, new ArrayList<>());
                setOfRequirements.get(temp_data).add(temp_set_of_rule);
            }
            br.close();
            fr.close();

            for (String data : setOfRequirements.keySet()) {
                setOfRequirements.get(data).sort((a, b) -> a.printRuleName().compareTo(b.printRuleName()));
                setOfRequirements.get(data).sort((a, b) -> -a.getType().compareTo("c"));
            }
        }
    }

    // Read a term. This method is to support the read_rule method and read_rule method.
    public static Term read_term(String t, boolean b,BufferedReader br) throws IOException {
        int c;
        StringBuilder temp = new StringBuilder();
        Term temp_term = new Term();

        temp_term.setAttribute(t);
        temp_term.setIncludeValues(b);

        // Read each value of the term
        while ((c = br.read()) != ')') {
            if (c != '\n' && c != '\r' && c != ' ') {
                if (c == ',') {
                    temp_term.addValue(temp.toString());
                    temp = new StringBuilder();
                } else {
                    temp.append((char) c);
                }
            }
        }
        temp_term.addValue(temp.toString());

        return temp_term;
    }

    // Read a rule. This method is to support the read_rule method.
    public static Rule read_rule(String r, BufferedReader br, ArrayList<String> a) throws IOException {
        int c;
        boolean phase_if_then = false;                  // false (phase IF) true (phase THEN)
        boolean flag_if_then = r.equals("IF-THEN");     //
        String temp = "";
        StringBuilder temp_int = new StringBuilder();
        boolean temp_bool = true;
        Rule temp_rule = new Rule();
        temp_rule.setRuleName(r);

        while ((c = br.read()) != '}' || flag_if_then) {
            if (c != ' ') {
                switch (r) {
                    // Read rule ALL, ANY and FORBIDDEN
                    case "ALL", "ANY", "FORBIDDEN" -> {
                        if (c != '(' && c != ',' && c != '!' && c != '{') {
                            temp = temp + (char) c;
                        } else if (c == '(') {
                            if (Main.is_an_attribute(temp, a))
                                temp_rule.addTerm(read_term(temp, temp_bool, br));
                        } else if (c == '!') {
                            temp_bool = false;
                        } else {    // c == ','
                            temp_bool = true;
                            temp = "";
                        }
                    }
                    // Read rule AT_MOST and AT_LEAST
                    case "AT_MOST", "AT_LEAST" -> {
                        if (c != '(' && c != ',' && c != '!' && c != '{' && !Character.isDigit(c)) {
                            temp = temp + (char) c;
                        } else if (c == '(') {
                            if (Main.is_an_attribute(temp, a))
                                temp_rule.addTerm(read_term(temp, temp_bool, br));
                        } else if (c == '!') {
                            temp_bool = false;
                        } else if (Character.isDigit(c)) {
                            temp_int.append((char) c);
                        } else if (c == ',') {    // c == ','
                            if (!temp_int.toString().equals("")) {
                                temp_rule.setQuantity(Integer.parseInt(temp_int.toString()));
                                temp_int = new StringBuilder();
                            }
                            temp_bool = true;
                            temp = "";
                        }
                    }
                    // Read rule IF-THEN
                    case "IF-THEN" -> {
                        if (!phase_if_then) {
                            if (!temp.equals("THENANY")) {
                                if (c != '(' && c != ',' && c != '!' && c != '}' && c != ')' && c != '{') {
                                    temp = temp + (char) c;
                                } else if (c == '(') {
                                    if (Main.is_an_attribute(temp, a))
                                        temp_rule.addTerm(read_term(temp, temp_bool, br));
                                } else if (c == '!') {
                                    temp_bool = false;
                                } else if (c == '}') {
                                    flag_if_then = false;
                                } else {    // c == ','
                                    temp_bool = true;
                                    temp = "";
                                }
                            } else {
                                phase_if_then = true;
                            }
                        } else {
                            if (c != '(' && c != ',' && c != '!' && c != '{') {
                                temp = temp + (char) c;
                            } else if (c == '(') {
                                if (Main.is_an_attribute(temp, a))
                                    temp_rule.addTerm2(read_term(temp, temp_bool, br));
                            } else if (c == '!') {
                                temp_bool = false;
                            } else {    // c == ','
                                temp_bool = true;
                                temp = "";
                            }
                        }
                    }
                }
            }
        }
        return temp_rule;
    }

    // Read global rules defined by the user end put them in an ArrayList
    public static void read_global_rules(File f, ArrayList<GlobalRule> globalRules,
                                         LinkedHashMap<String,ArrayList<SetOfRules>> setOfRequirements) throws IOException {
        FileReader fr = new FileReader(f);              //Creation of File Reader object
        BufferedReader br = new BufferedReader(fr);     //Creation of BufferedReader object
        int c;
        int count = 0;
        String temp = "";
        String temptype = "";
        int tempindex = 0;
        int count_internal = 0;

        GlobalRule temp_global_rule = new GlobalRule();

        while ((c = br.read()) != -1) {
            if (c != '\n' && c != '\r' && c != ' ') {

                // Read global rule's name
                if (count == 0 && (Character.isAlphabetic(c) || c == '_' || c == '*')) {
                    temp = temp + (char) c;
                } else if (count == 0 && c == '(') {
                    if (Main.is_a_global_rule(temp)) {
                        temp_global_rule = new GlobalRule();
                        temp_global_rule.setGlobalRule(temp);
                        temp = "";
                        count = 1;
                    } else {
                        Main.stop(temp + " is not a global rule!");
                    }

                } else if (count == 1) {
                    switch (temp_global_rule.getGlobalRule()) {
                        case "SPLIT", "ALL_SPLIT":
                            if (Character.isAlphabetic(c)) {
                                temp = temp + (char) c;
                            } else if (c == ')') {
                                count = 2;
                                temp_global_rule.setDataType(temp);
                                temp = "";
                            }
                            break;
                        case "ALONE":
                            if (count_internal == 0) {
                                if (c == 'o') {
                                    temptype = "o";
                                    count_internal = 2;
                                } else if (c == 'c') {
                                    temptype = "c";
                                    count_internal = 1;
                                }
                            } else if (count_internal == 1) {
                                if (Character.isDigit(c)) {
                                    temp = temp + (char) c;
                                } else if (c == '_') {
                                    tempindex = Integer.parseInt(temp);
                                    temp = "";
                                    count_internal = 2;
                                }
                            } else if (count_internal == 2) {
                                if (Character.isAlphabetic(c)) {
                                    temp = temp + (char) c;
                                } else if (c == ')') {
                                    for (SetOfRules setR : setOfRequirements.get(temp)) {
                                        if (temptype.equals("c") && setR.getIndex() == tempindex && setR.getType().equals("c"))
                                            temp_global_rule.setSet1(setR);
                                        if (temptype.equals("o") && setR.getType().equals("o"))
                                            temp_global_rule.setSet1(setR);
                                    }
                                    count = 2;
                                    temp = "";
                                    tempindex = 0;
                                    temptype = "";
                                    count_internal = 0;
                                }
                            }
                            break;
                        case "TOGETHER*", "ALL_TOGETHER*", "NOT_TOGETHER*":
                            if (Character.isAlphabetic(c)) {
                                temp = temp + (char) c;
                            } else if (c == ',') {
                                temp_global_rule.setDataType(temp);
                                temp = "";
                            } else if (c == ')') {
                                temp_global_rule.setDataType2(temp);
                                temp = "";
                                count = 2;
                            }
                            break;
                        case "TOGETHER", "NOT_TOGETHER":
                            if (count_internal == 0) {
                                if (c == 'o') {
                                    temptype = "o";
                                    count_internal = 2;
                                } else if (c == 'c') {
                                    temptype = "c";
                                    count_internal = 1;
                                }
                            } else if (count_internal == 1) {
                                if (Character.isDigit(c)) {
                                    temp = temp + (char) c;
                                } else if (c == '_') {
                                    tempindex = Integer.parseInt(temp);
                                    temp = "";
                                    count_internal = 2;
                                }
                            } else if (count_internal == 2) {
                                if (Character.isAlphabetic(c)) {
                                    temp = temp + (char) c;
                                } else if (c == ',') {
                                    for (SetOfRules setR : setOfRequirements.get(temp)) {
                                        if (temptype.equals("c") && setR.getIndex() == tempindex && setR.getType().equals("c"))
                                            temp_global_rule.setSet1(setR);
                                        if (temptype.equals("o") && setR.getType().equals("o"))
                                            temp_global_rule.setSet1(setR);
                                    }
                                    temp = "";
                                    tempindex = 0;
                                    temptype = "";
                                    count_internal = 0;
                                } else if (c == ')') {
                                    for (SetOfRules setR : setOfRequirements.get(temp)) {
                                        if (temptype.equals("c") && setR.getIndex() == tempindex && setR.getType().equals("c"))
                                            temp_global_rule.setSet2(setR);
                                        if (temptype.equals("o") && setR.getType().equals("o"))
                                            temp_global_rule.setSet2(setR);
                                    }
                                    temp = "";
                                    tempindex = 0;
                                    temptype = "";
                                    count_internal = 0;
                                    count = 2;
                                }
                            }
                            break;
                        default:
                            Main.stop(temp_global_rule.getGlobalRule() + " is not a global rule!");
                    }

                // Add global rules
                } else if (count == 2 && c == ';') {
                    globalRules.add(temp_global_rule);
                    count = 0;
                }
            }
        }
    }

    // Read plan cost
    public static void read_plan_cost(File f, LinkedHashMap<String,Integer> planCost) throws IOException {
        FileReader fr = new FileReader(f);              //Creation of File Reader object
        BufferedReader br = new BufferedReader(fr);     //Creation of BufferedReader object
        int c;
        int phase = 0;
        String temp_plan = "";
        String temp = "";

        while ((c = br.read()) != -1) {                  //Read char by Char
            if (c != '\n' && c != '\r') {
                switch (phase) {
                    // Read plan name
                    case 0 -> {
                        if (Character.isDigit(c) || Character.isAlphabetic(c) || c == '_') {
                            temp_plan = temp_plan.concat(String.valueOf((char) c));
                        } else {
                            phase = 1;
                        }
                    }
                    // Read each value of the plan
                    case 1 -> {
                        if (Character.isDigit(c)) {
                            temp = temp.concat(String.valueOf((char) c));
                        } else if (c == '}') {
                            planCost.put(temp_plan, Integer.valueOf(temp));
                            temp = "";
                            temp_plan = "";
                            phase = 0;
                        }
                    }
                }
            }
        }
        br.close();
        fr.close();
    }

    // Read data space
    public static void read_data_space(File f, LinkedHashMap<String,Integer> dataSpace) throws IOException {
        FileReader fr = new FileReader(f);              //Creation of File Reader object
        BufferedReader br = new BufferedReader(fr);     //Creation of BufferedReader object
        int c;
        int phase = 0;
        String temp_data = "";
        String temp = "";

        while ((c = br.read()) != -1) {                  //Read char by Char
            if (c != '\n' && c != '\r') {
                switch (phase) {
                    // Read plan name
                    case 0 -> {
                        if (Character.isDigit(c) || Character.isAlphabetic(c) || c == '_') {
                            temp_data = temp_data.concat(String.valueOf((char) c));
                        } else {
                            phase = 1;
                        }
                    }
                    // Read each value of the plan
                    case 1 -> {
                        if (Character.isDigit(c)) {
                            temp = temp.concat(String.valueOf((char) c));
                        } else if (c == '}') {
                            dataSpace.put(temp_data, Integer.valueOf(temp));
                            temp = "";
                            temp_data = "";
                            phase = 0;
                        }
                    }
                }
            }
        }
        br.close();
        fr.close();
    }

    public static void print_plan_cost_data_space(String m, LinkedHashMap<String,Integer> planCost) {
        System.out.println(m);
        for (String plan : planCost.keySet()) {
            System.out.println("\t" + plan + " : " + planCost.get(plan));
        }
    }


    // Print attributes
    public static void print_attributes(HashMap<String,ArrayList<String>> attributes_or_plans,
                                        String m) {
        System.out.println(m);
        for (String d : attributes_or_plans.keySet()) {
            System.out.print("\t" + d + " : {");
            for (int i=0; i<attributes_or_plans.get(d).size(); i++) {
                if (i != attributes_or_plans.get(d).size() - 1)
                    System.out.print(attributes_or_plans.get(d).get(i) + ", ");
                else
                    System.out.println(attributes_or_plans.get(d).get(i) + "}");
            }
        }
    }

    // Print plans
    public static void print_plans(HashMap<String,ArrayList<String>> attributes_or_plans,
                                        String m) {
        System.out.println(m);
        for (String d : attributes_or_plans.keySet()) {
            System.out.print("\t" + d + " = {");
            for (int i=0; i<attributes_or_plans.get(d).size(); i++) {
                if (i != attributes_or_plans.get(d).size() - 1)
                    System.out.print(attributes_or_plans.get(d).get(i) + ", ");
                else
                    System.out.println(attributes_or_plans.get(d).get(i) + "}");
            }
        }
    }

    // Print data
    public static void print_data(ArrayList<String> data_order_list, String m) {
        System.out.print(m + "\n\t{");
        for (int i=0; i<data_order_list.size(); i++) {
            if (i != data_order_list.size() - 1)
                System.out.print(data_order_list.get(i) + ", ");
            else
                System.out.println(data_order_list.get(i) + "}");
        }
    }

    // Print all SetOfRule.
    // For each set of rules print the rules and terms that are contained therein.
    // For each rule print all the terms it contains.
    public static void print_set_of_rules(HashMap<String,ArrayList<SetOfRules>> setOfRequirements,
                                          String m){
        System.out.println(m);
        for (String d : setOfRequirements.keySet()) {
            for (SetOfRules s : setOfRequirements.get(d)){
                System.out.println("\t" + s.toString());
            }
        }
    }

    // For each rule set print acceptable plans
    public static void print_acceptable_plans(LinkedHashMap<SetOfRules,ArrayList<String>> setOfAcceptablePlans) {
        System.out.println("For each Set of Requirements (R) its acceptable plans (P) :");
        for (SetOfRules setR : setOfAcceptablePlans.keySet()) {
            System.out.print("\t\t" + Main.ANSI_PURPLE + setR.printRuleName() + Main.ANSI_RESET + " has as acceptable plans : " + Main.ANSI_CYAN +"P_");
            if (setR.getType().equals("o")){
                System.out.print("o(" + setR.getData() + ") = {");
            } else {
                System.out.print("c"+ setR.getIndex() + "(" + setR.getData() + ") = {");
            }
            for (int i=0; i<setOfAcceptablePlans.get(setR).size(); i++){
                if (i < setOfAcceptablePlans.get(setR).size() - 1){
                    System.out.print(setOfAcceptablePlans.get(setR).get(i) + ", ");
                } else {
                    System.out.print(setOfAcceptablePlans.get(setR).get(i) + "}");
                }
            }
            System.out.println(Main.ANSI_RESET);
        }
    }

    // Print global rules
    public static void print_global_rules(ArrayList<GlobalRule> globalRules){
        System.out.println("Set of Global Rules (R_G) includes : ");
        for (GlobalRule rule : globalRules) {
            switch (rule.getGlobalRule()) {
                case "TOGETHER*", "NOT_TOGETHER*", "ALL_SPLIT", "SPLIT", "ALL_TOGETHER*" :
                    System.out.print("\t" + rule.getGlobalRule() + "(" + rule.getDataType() +
                            ((rule.getGlobalRule().equals("ALL_SPLIT") || rule.getGlobalRule().equals("SPLIT"))
                                    ? ")\n" : ("," + rule.getDataType2() + ")\n")));
                    break;
                case "TOGETHER", "NOT_TOGETHER", "ALONE" :
                    System.out.print("\t" + rule.getGlobalRule() + "(" + rule.getSet1().printRuleName() +
                            (rule.getGlobalRule().equals("ALONE") ? ")\n" : ("," + rule.getSet2().printRuleName() + ")\n")));
                    break;
            }

        }
    }
}
