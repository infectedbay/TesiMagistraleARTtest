import java.io.File;
import java.io.IOException;
import java.util.*;

public class Main {

    // Input files
    static final File file_attributes = new File("./src/data/attributes.txt");
    static final File file_plans = new File("./src/data/plans.txt");
    static final File file_data = new File("./src/data/data.txt");
    static final File folder_rules = new File("./src/data/rules");
    static final File file_global_rules = new File("./src/data/global_rules.txt");
    static final File file_plan_cost = new File("./src/data/price.txt");
    static final File file_data_space = new File("./src/data/space.txt");

    // Python file for CP-SAT
    static final File file_python = new File("./python/result.py");

    // Colors for System.out.print("")
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    // Rule list
    static final ArrayList<String> rules_list = new ArrayList<>() {{
        add("ALL");
        add("ANY");
        add("IF-THEN");
        add("FORBIDDEN");
        add("AT_MOST");
        add("AT_LEAST");
    }};

    // Global rule list
    static final ArrayList<String> global_rules_list = new ArrayList<>() {{
        add("TOGETHER");
        add("TOGETHER*");
        add("ALL_TOGETHER*");
        add("NOT_TOGETHER");
        add("NOT_TOGETHER*");
        add("SPLIT");
        add("ALL_SPLIT");
        add("ALONE");
    }};

    public static void main(String[] args) throws IOException {
    // Attribute -> Values
        LinkedHashMap<String,ArrayList<String>> attributes = new LinkedHashMap<>();
        ArrayList<String> attributeOrderList = new ArrayList<>();
    // Plan name -> Values
    // Plan Definition according Definition 3.1.1 at pag.28
        LinkedHashMap<String,ArrayList<String>> plans = new LinkedHashMap<>();
    // List of data type
        ArrayList<String> dataOrderList = new ArrayList<>();
    // Data Type -> List of SetOfRules on the attribute
        LinkedHashMap<String,ArrayList<SetOfRules>> setOfRequirements = new LinkedHashMap<>();
    // Acceptable plans : SetOfRules -> List of acceptable plans
        LinkedHashMap<SetOfRules,ArrayList<String>> setOfAcceptablePlans = new LinkedHashMap<>();
    // For each set of requirements, score vector of the plans
    // SetOfRules -> Plan -> ScoreVector (p_top is ideal plans)
    // Cost of al plan for a GB
        LinkedHashMap<String,Integer> planCost = new LinkedHashMap<>();
    // Storage (in GB) required by each resource
        LinkedHashMap<String,Integer> dataSpace = new LinkedHashMap<>();
    // List of global rules
        ArrayList<GlobalRule> globalRules = new ArrayList<>();

    // Legend
        System.out.println();
        System.out.println("Legend : ");
        System.out.println(ANSI_PURPLE + "\tPurple is to highlight parts of text" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "\tCyan is for processing results" + ANSI_RESET);
        System.out.println(ANSI_BLUE + "\tBlue is also for processing results" + ANSI_RESET);
        System.out.println(ANSI_GREEN + "\tGreen is for passed tests" + ANSI_RESET);
        System.out.println(ANSI_YELLOW + "\tYellow is for breaks or warnings" + ANSI_RESET);
        System.out.println(ANSI_RED + "\tRed is for critical errors" + ANSI_RESET);
        System.out.println();

    // Read data from attributes.txt
        ReadPrintParse.read_attributes(attributes, attributeOrderList, file_attributes);
        ReadPrintParse.print_attributes(attributes, "Set of attributes (A) includes :" );
        System.out.println();

    // Read data from plans.txt
        ReadPrintParse.read_plans(plans, file_plans);
        ReadPrintParse.print_plans(plans, "Set of Plans (P) includes :");
        System.out.println();

    // Read plan cost
        ReadPrintParse.read_plan_cost(file_plan_cost, planCost);
        ReadPrintParse.print_plan_cost_data_space("Cost in euro of one gigabyte for each plan: ", planCost);
        System.out.println();

    // Read data from da data.txt
        ReadPrintParse.read_data(dataOrderList, file_data);
        ReadPrintParse.print_data(dataOrderList, "Set of Data (D) includes :");
        System.out.println();

    // Read space required by data
        ReadPrintParse.read_data_space(file_data_space, dataSpace);
        ReadPrintParse.print_plan_cost_data_space("Storage space required by data", dataSpace);
        System.out.println();

    // Check if rules syntax is correct
        ReadPrintParse.rules_parser(Objects.requireNonNull(folder_rules.listFiles()),
                dataOrderList,
                attributeOrderList,
                attributes);
        System.out.println(ANSI_GREEN + "Syntax of rules files is correct."  + ANSI_RESET);

    // Read data from the folder ./rules
        ReadPrintParse.read_rules(Objects.requireNonNull(folder_rules.listFiles()), setOfRequirements, attributeOrderList);
        System.out.println();
        ReadPrintParse.print_set_of_rules(setOfRequirements,"Set of Requirements (R) includes :");

    // Check if set of requirements is well-defined as definition 3.2.1 pag.33
    // In each requirement's set there is at most 1 occurrence of an attribute for all terms and rules "ALL".
    // Each rule must contain at most 1 occurrence of an attribute.
        System.out.println();
        check_if_requirements_are_well_defined(setOfRequirements);

    // Search for acceptable plans
    // Identifies acceptable plans according to definition 3.3.3 at pag.40
        acceptable_plans(setOfRequirements, plans, attributeOrderList, setOfAcceptablePlans);
        System.out.println();
        ReadPrintParse.print_acceptable_plans(setOfAcceptablePlans);
        check_if_there_enough_acceptable_plans(setOfAcceptablePlans,setOfRequirements);
        System.out.println();
        System.out.println(ANSI_GREEN + "Set of acceptable plans (P) is defined according to definition 3.3.3" + ANSI_RESET);
        System.out.println(ANSI_GREEN + "The number of acceptable plans for each set of requirements is sufficient." + ANSI_RESET);

    // Identifies restricted domains according to definition 3.4.1 at pag.42
        //restricted_domain(restrictedDomain,attributes,setOfRequirements);
        //System.out.println();
        //ReadPrintParse.print_restricted_domain(restrictedDomain, attributeOrderList);
        //System.out.println();
        //System.out.println(ANSI_GREEN + "Restricted domain is defined according to definition 3.4.1" + ANSI_RESET);

    // Relation of preferences
    // If the preference relation is not defined for an attribute in a set of requirements, the default order is used.
    // Define set of preference relations defined in definition 3.4.2 pag 43
    // Calculate score function according definition 3.4.3 pag.46
    // Define weight function according definition 3.4.4. pag.47
        //preference_relation(preferences,restrictedDomain,attributes,
        //        Objects.requireNonNull(folder_relations_of_preference.listFiles()),
        //        Objects.requireNonNull(folder_list_of_weight_function.listFiles()));
        //System.out.println();
        //ReadPrintParse.print_preferences(preferences, attributeOrderList);
    // If everything is ok, the following definitions are checked
        //System.out.println();
        //System.out.println(ANSI_GREEN + "Set of preference relations is defined according to definition 3.4.2" + ANSI_RESET);
        //System.out.println(ANSI_GREEN + "Score function is defined according to definition 3.4.3" + ANSI_RESET);
        //System.out.println(ANSI_GREEN + "Weight function is defined according to definition 3.4.4" + ANSI_RESET);

    // Choose whether you want to use DP-Dominance or D-Dominance
        //String dominantRelationship = "";
        //long start = 0;
        //long end = 0;
        //if(!TIME_TEST) {
        //    while (!dominantRelationship.equals("D") && !dominantRelationship.equals("DP")) {
        //        System.out.println();
        //        dominantRelationship = pause("Choose the dominant relationship you prefer " +
        //                "\n\t Type \"DP\" for DP-Dominance or \"D\" for D-Dominance : ");
        //    }
        //} else {
        //    start = System.currentTimeMillis();
        //    dominantRelationship = DOMINANCE;
        //}

    // The score vector is calculated for each acceptable plane for each set of requirements
        //score_vector_calculation(dominantRelationship,attributeOrderList,plans,setOfAcceptablePlans,preferences,scoreVectors);
        //System.out.println();
        //ReadPrintParse.print_score_vector(scoreVectors,attributes,dominantRelationship);
        //System.out.println(ANSI_GREEN + "Ideal plans are defined according to definition 3.4.5" + ANSI_RESET);
        //System.out.println(ANSI_GREEN + (dominantRelationship.equals("D") ? "S" : "Weighted s") + "core vectors are defined according to definition " + (dominantRelationship.equals("D") ? "3.4.6" : "3.4.8") + ANSI_RESET);


    // For each set of requirements, the classification based on the dominance relationship D-Dominance
    // (or the dominance relationship DP-Dominance) is calculated
        //calculate_ranking(scoreVectors,ranking);
        //System.out.println();
        //ReadPrintParse.print_ranking(scoreVectors,ranking,dominantRelationship);
        //System.out.println(ANSI_GREEN + "Ranking is calculated with " + (dominantRelationship.equals("D") ? "D-Dominance" : "DP-Dominance") +
        //        " relationship defined according to definition " +
        //        (dominantRelationship.equals("D") ? "3.4.7" : "3.4.9") + ANSI_RESET);

    // Java Solver
        //if(RESULT || TIME_TEST) {
        // Definition of allocation function without considering global requirements
            //allocation_function_no_global_rules(setOfRequirements, ranking, allocationFunction);
            //System.out.println();
            //ReadPrintParse.print_allocation_function("Allocation function (L) without considering global requirements : ",
            //        allocationFunction, dominantRelationship);
            //System.out.println();
        // Check if solution found is correct for requirements
            //if (check_no_global_rules(allocationFunction))
            //    System.out.println(ANSI_GREEN + "Allocation Function is defined according to definition 3.4.10" + ANSI_RESET);
            //else
            //    stop("There is a set of requirements of type \"o\" that have allocated less than 1 plan or \n" +
            //            "There is a set of requirements of type \"c\" that have allocated less than n_p plans! \n");
        //}

        // Read hierarchy from hierarchy.txt and print it (if is defined)
            //ReadPrintParse.read_hierarchy(file_hierarchy, hierarchy, setOfRequirements);
            //if (!hierarchy.isEmpty()) {
            //    System.out.println();
            //    ReadPrintParse.print_hierarchy(hierarchy);
            //}

        // Read global rules from global_rules.txt
            ReadPrintParse.read_global_rules(file_global_rules, globalRules, setOfRequirements);
            System.out.println();
            ReadPrintParse.print_global_rules(globalRules);

    // Java Solver Part 2
        //if (RESULT || TIME_TEST) {
        // Definition of allocation function considering all global requirements of type ONLY_ONE
        // Check if exist a solution with ONLY_ONE rules
            //check_if_exist_a_solution_with_only_one_rules_with_print(setOfRequirements, ranking, globalRules);
            //// Check if exist a solution with ONLY_ONE rules without printing
            ////check_if_exist_a_solution_with_only_one_rules(setOfRequirements,ranking,globalRules);
            //System.out.println();
            //System.out.println(ANSI_GREEN + "Exist a solution considering ONLY_ONE RULES." + ANSI_RESET);
            //allocation_function_only_one(ranking, hierarchy, globalRules, allocationFunction);
            //System.out.println();
            //ReadPrintParse.print_allocation_function("Allocation function (L) considering only global requirements of type ONLY_ONE : ",
            //        allocationFunction, dominantRelationship);
            //System.out.println();
        // Check if solution found is correct for ONLY_ONE rules and for requirements
            //if (check_only_one(globalRules, allocationFunction) && check_no_global_rules(allocationFunction)) {
            //    System.out.println(ANSI_GREEN + "Allocation function respects ONLY_ONE rules." + ANSI_RESET);
            //} else {
            //    stop("Allocation function does not respect ONLY_ONE rules!");
            //}

        // Definition of allocation function considering all global requirements
            //allocation_function_global_rules(ranking, hierarchy, globalRules, allocationFunction);
            //System.out.println();
            //ReadPrintParse.print_allocation_function("Allocation function (L) considering global requirements : ", allocationFunction, dominantRelationship);
            //System.out.println();
        // Check if solution found is correct for all global rules and for requirements
            //if (check_all_rules(globalRules, allocationFunction))
            //    System.out.println(ANSI_GREEN + "Allocation function respects all global rules. \n" +
            //            "Allocation is correct as defined in definition 3.5.2" + ANSI_RESET);
            //else
            //    stop("Allocation function does not respect global rules!");
        //}

    // Write python code for OR-Tools SAT-Solver
        System.out.println();
        PythonART.writeFile(file_python, setOfRequirements, setOfAcceptablePlans, globalRules, planCost, dataSpace);
        System.out.println("To find the result using the SAT-Solver \n\t1. Install Google’s OR-Tools at the link : https://developers.google.com/optimization/install");
        System.out.println("\t2. Open CMD and paste the following commands");
        System.out.println("\t\tcd " + (file_python.getAbsolutePath().substring(0, file_python.getAbsolutePath().length() - file_python.getName().length())));
        System.out.println("\t\tpython result.py");
    }

    // Check if string test is an attribute
    public static boolean is_an_attribute(String test, ArrayList<String> a){
        return a.contains(test);
    }

    // Check if string v is a value included in attribute's values of a
    public static boolean is_a_value_of_an_attribute(String a, String v, LinkedHashMap<String, ArrayList<String>> attributes){
        return attributes.get(a).contains(v);
    }

    // Check if string test is a data
    public static boolean is_a_data(String test, ArrayList<String> data_order_list){
        return data_order_list.contains(test);
    }

    // Check if string test is a rule
    public static boolean is_a_rule(String test){
        return rules_list.contains(test);
    }

    // Check if string test is a global rule
    public static boolean is_a_global_rule(String test){
        return global_rules_list.contains(test);
    }

    // It Puts in "subsets" all possible subsets of set "input"
    // In our program, given a set of requirements "input" puts in set "subsets" all possible subsets of "input".
    public static <E> void find_all_subset(List<List<E>> subsets, ArrayList<E> input,
                                       ArrayList<E> output, int index){
        // Base Condition
        if (index == input.size()) {
            subsets.add(output);
            return;
        }

        // Not Including Value which is at Index
        find_all_subset(subsets, input, new ArrayList<>(output), index + 1);

        // Including Value which is at Index
        output.add(input.get(index));
        find_all_subset(subsets, input, new ArrayList<>(output), index + 1);
    }

    // Check if there are enough acceptable plans for each set of requirements
    // >= 1 for R_o
    // >= n_p for R_c1
    public static void check_if_there_enough_acceptable_plans(LinkedHashMap<SetOfRules,ArrayList<String>> setOfAcceptablePlans,
                                                              LinkedHashMap<String,ArrayList<SetOfRules>> setOfRequirements) {
        for (String data : setOfRequirements.keySet()) {
            for (SetOfRules setR : setOfRequirements.get(data)) {
                if (setR.getType().equals("o")) {
                    if (!(setOfAcceptablePlans.get(setR).size() >= 1)) {
                        stop("The number of acceptable plans for " + setR.printRuleName() + " is insufficient!");
                    }
                } else {
                    if (!(setOfAcceptablePlans.get(setR).size() >= setR.getNumberOfCopy())) {
                        stop("The number of acceptable plans for " + setR.printRuleName() + " is insufficient!");
                    }
                }
            }
        }
    }

    // Add in LinkedHashMap setOfAcceptablePlans for each SetOfRules the plan p if it satisfies SetOfRule's rule and term
    // Identifies acceptable plans according to definition 3.3.3 at pag.40
    public static void acceptable_plans(LinkedHashMap<String,ArrayList<SetOfRules>> setOfRequirements,
                                            LinkedHashMap<String,ArrayList<String>> plans,
                                            ArrayList<String> attribute_order_list,
                                            LinkedHashMap<SetOfRules, ArrayList<String>> setOfAcceptablePlans) {
        // Contains indexes of attributes that must be checked
        ArrayList<ArrayList<Integer>> attribute_index_for_verify;
        // Values of attributes that must be checked
        ArrayList<ArrayList<String>> value_of_plan;
        ArrayList<String> temp_string;
        ArrayList<Integer> temp_int;

        // Cycle all SetOfRules
        for(String s : setOfRequirements.keySet()) {
            for (SetOfRules setR : setOfRequirements.get(s)){

                // Create the set of acceptable plans for the set of rules setR
                setOfAcceptablePlans.put(setR, new ArrayList<>());

                // Search attribute indexes which will be put in attribute_index_for_verify
                attribute_index_for_verify = new ArrayList<>();
                for(ArrayList<String> list_av : setR.arrayList_list_for_verify()){
                    temp_int = new ArrayList<>();
                    for (String av : list_av){
                        temp_int.add(attribute_order_list.indexOf(av));
                    }
                    attribute_index_for_verify.add(temp_int);
                }

                for (String plan : plans.keySet()){
                    // Search respective values for each plan
                    value_of_plan = new ArrayList<>();
                    for (ArrayList<Integer> list_avi : attribute_index_for_verify){
                        temp_string = new ArrayList<>();
                        for (int avi : list_avi){
                            temp_string.add(plans.get(plan).get(avi));
                        }
                        value_of_plan.add(temp_string);
                    }

                    // If the plan satisfies the SetOfRule, it is added to the set of acceptable plans
                    if(setR.verify(value_of_plan)){
                        setOfAcceptablePlans.get(setR).add(plan);
                    }
                }
            }
        }
    }

    // Definition 3.2.1 pag.33
    // In each requirement's set there is at most 1 occurrence of an attribute for all terms and rules "ALL".
    // Each rule must contain at most 1 occurrence of an attribute.
    public static void check_if_requirements_are_well_defined(LinkedHashMap<String,ArrayList<SetOfRules>> setOfRequirements){
        ArrayList<String> a_in_term_all;
        ArrayList<String> a_in_rule;

        // Cycle all SetOfRules
        for(String s : setOfRequirements.keySet()) {
            for (SetOfRules setR : setOfRequirements.get(s)){
                a_in_term_all = new ArrayList<>();

                // Verify that an attribute only appears 1 time in an ALL term or rule
                for (Term t : setR.getTerms()){
                    if (!a_in_term_all.contains(t.getAttribute()))
                        a_in_term_all.add(t.getAttribute());
                    else
                        stop("Set of requirements " + setR.printRuleName() + " is malformed."
                                + "\nThere is more than 1 term that contains the attribute \"" +
                                t.getAttribute() + "\"!");
                }
                for (Rule r : setR.getRules()){
                    if(Objects.equals(r.getRuleName(), "ALL")) {
                        for (Term t : r.getTerms()) {
                            if (!a_in_term_all.contains(t.getAttribute()))
                                a_in_term_all.add(t.getAttribute());
                            else
                                stop("Set of requirements " + setR.printRuleName() + " is malformed."
                                        + "\nThere is more than 1 term or rule ALL that contains the attribute \"" +
                                        t.getAttribute() + "\" or \n" +
                                        "there is more than 1 occurrence of the the attribute \"" +
                                        t.getAttribute() + "\" " + "in an ALL rule!");
                        }
                    }
                }

                // Verify that each rule contains only one occurrence of an attribute
                for (Rule r : setR.getRules()){
                    a_in_rule = new ArrayList<>();
                    for (Term t : r.getTerms()) {
                        if (!a_in_rule.contains(t.getAttribute()))
                            a_in_rule.add(t.getAttribute());
                        else
                            stop("Set of requirements " + setR.printRuleName() + " is malformed." +
                                    "\nThe Rule \"" + r + "\" contains more than 1 occurrence an attribute \"" +
                                    t.getAttribute() + "\"!");
                        if (Objects.equals(r.getRuleName(), "IF-THEN")) {
                            for (Term t2 : r.getTerms2()) {
                                if (!a_in_rule.contains(t2.getAttribute()))
                                    a_in_rule.add(t2.getAttribute());
                                else
                                    stop("Set of requirements " + setR.printRuleName() + " is malformed." +
                                            "\nThe Rule \"" + r + "\" contains more than 1 occurrence of attribute \"" +
                                            t2.getAttribute() + "\"!");
                            }
                        }
                    }
                }
            }
        }
        System.out.println(ANSI_GREEN + "Set of Requirements (R) is well-defined according to definition 3.2.1" + ANSI_RESET);
    }

    // Stop execution
    static void stop(String m){
        System.out.println();
        System.out.println(ANSI_RED + m + ANSI_RESET);
        System.exit(0);
    }
}