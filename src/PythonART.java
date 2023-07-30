import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class PythonART {
    public static void writeFile(File file_python,
                                 LinkedHashMap<String, ArrayList<SetOfRules>> setOfRequirements,
                                 LinkedHashMap<SetOfRules,ArrayList<String>> setOfAcceptablePlans,
                                 ArrayList<GlobalRule> globalRules,
                                 LinkedHashMap<String,Integer> planCost,
                                 LinkedHashMap<String,Integer> dataSpace) throws FileNotFoundException {
        System.out.println("file " + file_python.getName() + ": " + file_python.isFile());
        PrintWriter p = new PrintWriter(file_python);

        LinkedHashMap<SetOfRules, String> py_setOfRequirements = new LinkedHashMap<>();
        List<List<String>> subsets_string;
        ArrayList<String> temp;
        List<List<SetOfRules>> subsets_setofrules;
        ArrayList<SetOfRules> input;

        // LIBRARY
        p.println("from ortools.sat.python import cp_model\n");

        // MAIN
        p.println("def main():\n" +
                "    # Create the model.\n" +
                "    model = cp_model.CpModel()\n");

        // VARIABLES
        p.println("    # Variables");
        for (String data : setOfRequirements.keySet()) {
            for (SetOfRules setR : setOfRequirements.get(data)) {
                if(setR.getType().equals("o")) {
                    py_setOfRequirements.put(setR, "Ro_" + data);
                } else {
                    py_setOfRequirements.put(setR, ("Rc" + setR.getIndex() + "_" + data));
                }
                p.print("    " +  py_setOfRequirements.get(setR) + " = model.NewIntVarFromDomain(cp_model. Domain.FromIntervals([");
                for (String plan : setOfAcceptablePlans.get(setR)) {
                    p.print("["+plan.substring(2)+"]");
                    if (setOfAcceptablePlans.get(setR).indexOf(plan) == setOfAcceptablePlans.get(setR).size() - 1) {
                        p.println("]), '"+ py_setOfRequirements.get(setR) +"')");
                    } else {
                        p.print(",");
                    }
                }
            }
        }
        p.println();
        /*
        for (String data : hierarchy.keySet()) { // VARIABILI PER LA GERARCHIA
            for (SetOfRules setR : hierarchy.get(data)) {
                p.print("    PI_" + py_setOfRequirements.get(setR) + " = model.NewIntVarFromDomain(cp_model. Domain.FromIntervals([");
                for (int i = 0; i<ranking.get(setR).size(); i++) {
                    p.print("[" + i + "]");
                    if (i == ranking.get(setR).size() - 1)
                        p.println("]), 'PI_" + py_setOfRequirements.get(setR) + "')");
                    else
                        p.print(",");
                }
            }
        }
        p.println();
        */
        // POSSIBLE D
        // COST OF PLAN ASSIGNED TO A RESOURCE
        for (String data : setOfRequirements.keySet()) { // VARIABILI PER LA GERARCHIA
            for (SetOfRules setR : setOfRequirements.get(data)) {
                p.print("    C_" + py_setOfRequirements.get(setR) + " = model.NewIntVarFromDomain(cp_model. Domain.FromIntervals([");
                int i=0;
                for (String plan : planCost.keySet()) {
                    p.print("[" + planCost.get(plan) + "]");
                    if (i == planCost.size() - 1)
                        p.println("]), 'C_" + py_setOfRequirements.get(setR) + "')");
                    else
                        p.print(",");
                    i=i+1;
                }
            }
        }
        p.println();


            // GLOBAL RULES
        p.println("    # Global rules");

        // TOGETHER
        for (GlobalRule g_rule : globalRules) {
            if (g_rule.getGlobalRule().equals("TOGETHER")) {
                p.println("    # " + g_rule.getGlobalRule() + "(" + g_rule.getSet1().printRuleName() + ", " +
                        g_rule.getSet2().printRuleName() +")");
                p.println("    model.Add(" + py_setOfRequirements.get(g_rule.getSet1()) + " == " +
                        py_setOfRequirements.get(g_rule.getSet2()) +")");
                p.println();
            }
        }

        // NOT_TOGETHER
        for (GlobalRule g_rule : globalRules) {
            if (g_rule.getGlobalRule().equals("NOT_TOGETHER")) {
                p.println("    # " + g_rule.getGlobalRule() + "(" + g_rule.getSet1().printRuleName() + ", " +
                        g_rule.getSet2().printRuleName() +")");
                p.println("    model.Add(" + py_setOfRequirements.get(g_rule.getSet1()) + " != " +
                        py_setOfRequirements.get(g_rule.getSet2()) +")");
                p.println();
            }
        }

        // ALONE
        for (GlobalRule g_rule : globalRules) {
            if (g_rule.getGlobalRule().equals("ALONE")) {
                p.println("    # " + g_rule.getGlobalRule() + "(" + g_rule.getSet1().printRuleName() + ")");
                for(String data : setOfRequirements.keySet()){
                    for(SetOfRules setR : setOfRequirements.get(data)) {
                        if (!g_rule.getSet1().printRuleName().equals(setR.printRuleName())){
                            p.println("    model.Add(" + py_setOfRequirements.get(g_rule.getSet1()) + " != " +
                                    py_setOfRequirements.get(setR) +")");
                        }
                    }
                }
                p.println();
            }
        }

        // TOGETHER*
        for (GlobalRule g_rule : globalRules) {
            if (g_rule.getGlobalRule().equals("TOGETHER*")) {
                p.println("    # " + g_rule.getGlobalRule() + "(" + g_rule.getDataType() + ", " + g_rule.getDataType2() + ")");
                temp = new ArrayList<>();
                for (SetOfRules r1 : setOfRequirements.get(g_rule.getDataType())) {
                    for (SetOfRules r2 : setOfRequirements.get(g_rule.getDataType2())) {
                        temp.add(py_setOfRequirements.get(r1) + "_eq_" + py_setOfRequirements.get(r2));
                        p.println("    " + py_setOfRequirements.get(r1) + "_eq_" + py_setOfRequirements.get(r2) +
                                " = model.NewBoolVar('" + py_setOfRequirements.get(r1) + "_eq_" + py_setOfRequirements.get(r2) + "')");
                        p.println("    model.Add(" + py_setOfRequirements.get(r1) +" == " + py_setOfRequirements.get(r2) +
                                ").OnlyEnforceIf("+ py_setOfRequirements.get(r1) + "_eq_" + py_setOfRequirements.get(r2) +")");
                    }
                }
                p.print("    model.AddBoolOr([");
                for (String bool : temp) {
                    p.print(bool);
                    if (temp.indexOf(bool) < temp.size() - 1) {
                        p.print(",");
                    } else {
                        p.println("])");
                    }
                }
                p.println();
            }
        }

        // NOT_TOGETHER*
        for (GlobalRule g_rule : globalRules) {
            if (g_rule.getGlobalRule().equals("NOT_TOGETHER*")) {
                p.println("    # " + g_rule.getGlobalRule() + "(" + g_rule.getDataType() + ", " + g_rule.getDataType2() + ")");
                for (SetOfRules r1 : setOfRequirements.get(g_rule.getDataType())) {
                    for (SetOfRules r2 : setOfRequirements.get(g_rule.getDataType2())) {
                        p.println("    model.Add(" + py_setOfRequirements.get(r1) + " != " + py_setOfRequirements.get(r2) + ")");
                    }
                }
                p.println();
            }
        }

        // ALL_TOGETHER*
        for (GlobalRule g_rule : globalRules) {
            if (g_rule.getGlobalRule().equals("ALL_TOGETHER*")) {
                p.println("    # " + g_rule.getGlobalRule() + "(" + g_rule.getDataType() + ", " + g_rule.getDataType2() + ")");
                for (SetOfRules r1 : setOfRequirements.get(g_rule.getDataType())) {
                    temp = new ArrayList<>();
                    for (SetOfRules r2 : setOfRequirements.get(g_rule.getDataType2())) {
                        temp.add(py_setOfRequirements.get(r1) + "_eq_" + py_setOfRequirements.get(r2));
                        p.println("    " + py_setOfRequirements.get(r1) + "_eq_" + py_setOfRequirements.get(r2) + " = model.NewBoolVar('" +
                                py_setOfRequirements.get(r1) + "_eq_" + py_setOfRequirements.get(r2) + "')");
                        p.println("    model.Add(" + py_setOfRequirements.get(r1) +" == " + py_setOfRequirements.get(r2) +").OnlyEnforceIf("+
                                py_setOfRequirements.get(r1) + "_eq_" + py_setOfRequirements.get(r2)+")");
                    }
                    p.print("    model.AddBoolOr([");
                    for (String bool : temp) {
                        p.print(bool);
                        if (temp.indexOf(bool) < temp.size() - 1) {
                            p.print(",");
                        } else {
                            p.println("])");
                            p.println();
                        }
                    }
                }
                for (SetOfRules r1 : setOfRequirements.get(g_rule.getDataType2())) {
                    temp = new ArrayList<>();
                    for (SetOfRules r2 : setOfRequirements.get(g_rule.getDataType())) {
                        temp.add(py_setOfRequirements.get(r1) + "_eq_" + py_setOfRequirements.get(r2));
                        p.println("    " + py_setOfRequirements.get(r1) + "_eq_" + py_setOfRequirements.get(r2) + " = model.NewBoolVar('" +
                                py_setOfRequirements.get(r1) + "_eq_" + py_setOfRequirements.get(r2) + "')");
                        p.println("    model.Add(" + py_setOfRequirements.get(r1) +" == " + py_setOfRequirements.get(r2) +").OnlyEnforceIf("+
                                py_setOfRequirements.get(r1) + "_eq_" + py_setOfRequirements.get(r2)+")");
                    }
                    p.print("    model.AddBoolOr([");
                    for (String bool : temp) {
                        p.print(bool);
                        if (temp.indexOf(bool) < temp.size() - 1) {
                            p.print(",");
                        } else {
                            p.println("])");
                            p.println();
                        }
                    }
                }
            }
        }

        // SPLIT
        for (GlobalRule g_rule : globalRules) {
            if (g_rule.getGlobalRule().equals("SPLIT")) {
                p.println("    # " + g_rule.getGlobalRule() + "(" + g_rule.getDataType() + ")");
                SetOfRules origin = new SetOfRules();
                for (SetOfRules setR : setOfRequirements.get(g_rule.getDataType())){
                    if (setR.getType().equals("o")){
                        origin = setR;
                    }
                }
                for (SetOfRules setR : setOfRequirements.get(g_rule.getDataType())){
                    if(!setR.printRuleName().equals(origin.printRuleName())){
                        p.println("    model.Add(" + py_setOfRequirements.get(origin) + " != " +
                                py_setOfRequirements.get(setR) +")");
                    }
                }
                p.println();
            }
        }

        // ALL_SPLIT (ONLY_ONE)
        for (GlobalRule g_rule : globalRules) {
            if (g_rule.getGlobalRule().equals("ALL_SPLIT")) {
                p.println("    # " + g_rule.getGlobalRule() + "(" + g_rule.getDataType() + ")");
                input = new ArrayList<>(setOfRequirements.get(g_rule.getDataType()));
                subsets_setofrules = new ArrayList<>();
                Main.find_all_subset(subsets_setofrules, input, new ArrayList<>(), 0);
                for (List<SetOfRules> setRlist : subsets_setofrules) {
                    if (setRlist.size() == 2) {
                        p.println("    model.Add(" + py_setOfRequirements.get(setRlist.get(0)) + " != " +
                                py_setOfRequirements.get(setRlist.get(1)) +")");
                    }
                }
                p.println();
            }
        }

        // FIND RIGHT PRICE
        String temp_s;
        p.println("    # Pricing");
        for (String data : setOfRequirements.keySet()) {
            for (SetOfRules setR : setOfRequirements.get(data)) {
                for (String plan : planCost.keySet()) {
                    temp_s = py_setOfRequirements.get(setR) + "_IS_" + plan.substring(2);
                    p.println("    " + temp_s + " = model.NewBoolVar('" + temp_s + "')");
                }
                p.print("    model.AddBoolOr([");
                int i = 0;
                for (String plan : planCost.keySet()) {
                    p.print(py_setOfRequirements.get(setR) + "_IS_" + plan.substring(2));

                    if (i < planCost.size() - 1) {
                        p.print(",");
                    } else {
                        p.println("])");
                    }
                    if(i < planCost.size() - 1 && i%3 == 0) {
                        p.print("\n        ");
                    }
                    i=i+1;
                }
                for (String plan : planCost.keySet()) {
                    p.println("    model.Add(" + py_setOfRequirements.get(setR) + " == " + plan.substring(2) + ").OnlyEnforceIf(" +
                            py_setOfRequirements.get(setR) + "_IS_" + plan.substring(2) + ")");
                }
                for (String plan : planCost.keySet()) {
                    p.println("    model.Add(C_" + py_setOfRequirements.get(setR) + " == " + planCost.get(plan) + ").OnlyEnforceIf(" +
                            py_setOfRequirements.get(setR) + "_IS_" + plan.substring(2) + ")");
                }
                p.println();
            }
        }


        // RANKING
        /*
        String temp_s;
        if (!hierarchy.isEmpty()) {
            p.println("    # Ranking");
            for (String data : hierarchy.keySet()) {
                for (SetOfRules setR : hierarchy.get(data)) {
                    for (String plan : ranking.get(setR)) {
                        temp_s = py_setOfRequirements.get(setR) + "_IS_" + plan.substring(2);
                        p.println("    " + temp_s + " = model.NewBoolVar('" + temp_s + "')");
                    }
                    p.print("    model.AddBoolOr([");
                    int i = 0;
                    for (String plan : ranking.get(setR)) {
                        i=i+1;
                        p.print(py_setOfRequirements.get(setR) + "_IS_" + plan.substring(2));
                        if (ranking.get(setR).indexOf(plan) < ranking.get(setR).size()-1) {
                            p.print(",");
                        } else {
                            p.println("])");
                        }
                        if(ranking.get(setR).indexOf(plan) < ranking.get(setR).size()-1 && i%3 == 0) {
                            p.print("\n        ");
                        }
                    }
                    for (String plan : ranking.get(setR)) {
                        p.println("    model.Add(" + py_setOfRequirements.get(setR) + " == " + plan.substring(2) + ").OnlyEnforceIf(" +
                                py_setOfRequirements.get(setR) + "_IS_" + plan.substring(2) + ")");
                    }
                    for (String plan : ranking.get(setR)) {
                        p.println("    model.Add(PI_" + py_setOfRequirements.get(setR) + " == " + ranking.get(setR).indexOf(plan) + ").OnlyEnforceIf(" +
                                py_setOfRequirements.get(setR) + "_IS_" + plan.substring(2) + ")");
                    }
                    p.println();
                }

            }
        }
        */



        // OBJECTIVE FUNCTION
        p.println("    # Objective Function");
        p.print("    model.Minimize(");
        int j = 0;
        for (String data : setOfRequirements.keySet()) {
            int i = 0;
            for (SetOfRules setR : setOfRequirements.get(data)) {
                if (j == setOfRequirements.keySet().size() - 1 &&
                i == setOfRequirements.get(data).size() - 1) {
                    p.print("(C_" + py_setOfRequirements.get(setR) + " * " + dataSpace.get(data)+ ")");
                } else {
                    p.print("(C_" + py_setOfRequirements.get(setR) + " * " + dataSpace.get(data)+ ") + ");
                }
                i=i+1;
            }
            j=j+1;
        }
        p.println(")");
        p.println();


        /*
        if (!hierarchy.isEmpty()) {
            p.println("    # Objective Function");
            p.print("    model.Minimize(");
            boolean need_plus = false;
            int size = 0;
            for (String data : hierarchy.keySet()) {
                if (size < hierarchy.get(data).size())
                    size = hierarchy.get(data).size();
            }
            for (int i=0; i<size; i++) {
                p.print("((");
                for (String data : hierarchy.keySet()) {
                    for (SetOfRules setR : hierarchy.get(data)) {
                        if (hierarchy.get(data).indexOf(setR) == i) {
                            if (need_plus) {
                                p.print(" + ");
                                need_plus = false;
                            }
                            p.print("PI_"+py_setOfRequirements.get(setR));
                            need_plus = true;
                        }
                    }
                }
                need_plus = false;
                p.print(")");
                int count = 0;
                if (i != size -1) {
                    for (int j=i+1; j<size; j++) {
                        for (String data : hierarchy.keySet()) {
                            for (SetOfRules setR : hierarchy.get(data)) {
                                if (hierarchy.get(data).indexOf(setR) == j) {
                                    count = count + setR.getNumberOfCopy();
                                }
                            }
                        }
                    }
                    if (count > 0) {
                        p.print("*" + count);
                    } else {
                        p.print("*1");
                    }
                    for (int j=0; j<(size - (i + 1))*2; j++) {
                        p.print("0");
                    }
                    p.print(") + \n        ");
                } else {
                    p.println("))");
                }
            }
            p.println();
        }
        */

        // CREATE A SOLVER
        p.println("    # Creates a solver and solves the model.");
        p.println("    solver = cp_model.CpSolver()\n" +
                "    status = solver.Solve(model)\n");

        // PRINT SOLUTION
        p.println("    if status == cp_model.OPTIMAL or status == cp_model.FEASIBLE:");
        p.println("        print('\\nPlans for Allocation')");
        for (String data : setOfRequirements.keySet()) {
            for (SetOfRules setR : setOfRequirements.get(data)) {
                p.print("        print('    "+setR.printRuleName()+" = ");
                p.print("p_%i");
                p.print("' % (");
                p.print("solver.Value("+ py_setOfRequirements.get(setR) +")");
                p.println("))");
            }
        }
        p.println("    else:\n" +
                "        print('    No solution found.')\n");

        // STATISTICS
        p.println();
        p.println("    # Statistics");
        p.println("    print('\\nStatistics')");
        p.println("    print(f'    status   : {solver.StatusName(status)}')");
        p.println("    print(f'    conflicts: {solver.NumConflicts()}')");
        p.println("    print(f'    branches : {solver.NumBranches()}')");
        p.println("    print(f'    wall time: {solver.WallTime()} s')");
        p.println();

        // EXECUTE MAIN
        p.print("if __name__ == '__main__':\n" +
                "    main()");

        p.flush();
        p.close();
        System.out.println("    - result.py: GENERATED\n");
    }

}
