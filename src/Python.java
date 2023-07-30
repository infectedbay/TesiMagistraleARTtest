import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class Python {
    public static void writeFile(File file_python,
                                 LinkedHashMap<String, ArrayList<SetOfRules>> setOfRequirements,
                                 LinkedHashMap<SetOfRules,ArrayList<String>> setOfAcceptablePlans,
                                 LinkedHashMap<SetOfRules,ArrayList<String>> ranking,
                                 ArrayList<GlobalRule> globalRules,
                                 LinkedHashMap<String,ArrayList<SetOfRules>> hierarchy) throws FileNotFoundException {
        System.out.println("file " + file_python.getName() + ": " + file_python.isFile());
        PrintWriter p = new PrintWriter(file_python);

        HashMap<SetOfRules, ArrayList<String>> py_setOfRequirements = new HashMap<>();
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
                    py_setOfRequirements.put(setR, new ArrayList<>());
                    py_setOfRequirements.get(setR).add("Ro_" + data);
                } else {
                    py_setOfRequirements.put(setR, new ArrayList<>());
                    for (int i = 0; i < setR.getNumberOfCopy(); i++) {
                        py_setOfRequirements.get(setR).add("Rc"+ setR.getIndex() + "_" + data + ((setR.getNumberOfCopy() > 1) ? ("_" + (i+1)) : ""));
                    }
                }
                for (String py_setR : py_setOfRequirements.get(setR)) {
                    p.print("    " + py_setR + " = model.NewIntVarFromDomain(cp_model. Domain.FromIntervals([");
                    for (String plan : setOfAcceptablePlans.get(setR)) {
                        p.print("["+plan.substring(2)+"]");
                        if (setOfAcceptablePlans.get(setR).indexOf(plan) == setOfAcceptablePlans.get(setR).size() - 1) {
                            p.println("]), '"+py_setR+"')");
                        } else {
                            p.print(",");
                        }
                    }
                }
            }
        }
        p.println();
        for (String data : hierarchy.keySet()) {
            for (SetOfRules setR : hierarchy.get(data)) {
                for (String py_setR : py_setOfRequirements.get(setR)) {
                    p.print("    PI_" + py_setR + " = model.NewIntVarFromDomain(cp_model. Domain.FromIntervals([");
                    for (int i = 0; i<ranking.get(setR).size(); i++) {
                        p.print("[" + i + "]");
                        if (i == ranking.get(setR).size() - 1)
                            p.println("]), 'PI_" + py_setR + "')");
                        else
                            p.print(",");
                    }
                }
            }
        }
        p.println();

        // REQUIREMENTS FOR RC
        for (SetOfRules setR : py_setOfRequirements.keySet()) {
            if (py_setOfRequirements.get(setR).size() > 1) {
                p.println("    # " + setR.printRuleName());
                subsets_string = new ArrayList<>();
                Main.find_all_subset(subsets_string, py_setOfRequirements.get(setR), new ArrayList<>(),0);
                for (List<String> list : subsets_string) {
                    if (list.size() == 2) {
                        p.println("    model.Add(" + list.get(0) + " != " + list.get(1) + ")");
                    }
                }
                p.println();
            }
        }

        p.println("    # Global rules");
        // ONLY_ONE
        for (GlobalRule g_rule : globalRules) {
            if (g_rule.getGlobalRule().equals("ONLY_ONE")) {
                p.println("    # " + g_rule.getGlobalRule() + "(" + g_rule.getDataType() + ")");
                input = new ArrayList<>(setOfRequirements.get(g_rule.getDataType()));
                subsets_setofrules = new ArrayList<>();
                Main.find_all_subset(subsets_setofrules, input, new ArrayList<>(), 0);

                for (List<SetOfRules> setRlist : subsets_setofrules) {
                    if (setRlist.size() == 2) {
                        for (String r1 : py_setOfRequirements.get(setRlist.get(0))) {
                            for (String r2 : py_setOfRequirements.get(setRlist.get(1))) {
                                p.println("    model.Add(" + r1 + " != " + r2 +")");
                            }
                        }
                    }
                }
                p.println();
            }
        }

        // TOGETHER
        for (GlobalRule g_rule : globalRules) {
            if (g_rule.getGlobalRule().equals("TOGETHER")) {
                p.println("    # " + g_rule.getGlobalRule() + "(" + g_rule.getDataType() + ", " + g_rule.getDataType2() + ")");
                for (SetOfRules r1 : setOfRequirements.get(g_rule.getDataType())) {
                    for (String py_r1 : py_setOfRequirements.get(r1)) {
                        temp = new ArrayList<>();
                        for (SetOfRules r2 : setOfRequirements.get(g_rule.getDataType2())) {
                            for (String py_r2 : py_setOfRequirements.get(r2)) {
                                temp.add(py_r1 + "_eq_" + py_r2);
                                p.println("    " + py_r1 + "_eq_" + py_r2 + " = model.NewBoolVar('" + py_r1 + "_eq_" + py_r2 + "')");
                                p.println("    model.Add(" + py_r1 +" == " + py_r2 +").OnlyEnforceIf("+ py_r1 + "_eq_" + py_r2+")");
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
                    }
                }
                p.println();
            }
        }

        // TOGETHER*
        for (GlobalRule g_rule : globalRules) {
            if (g_rule.getGlobalRule().equals("TOGETHER*")) {
                temp = new ArrayList<>();
                p.println("    # " + g_rule.getGlobalRule() + "(" + g_rule.getDataType() + ", " + g_rule.getDataType2() + ")");
                for (SetOfRules r1 : setOfRequirements.get(g_rule.getDataType())) {
                    for (SetOfRules r2 : setOfRequirements.get(g_rule.getDataType2())) {
                        for (String py_r1 : py_setOfRequirements.get(r1)) {
                            for (String py_r2 : py_setOfRequirements.get(r2)) {
                                temp.add(py_r1 + "_eq_" + py_r2);
                                p.println("    " + py_r1 + "_eq_" + py_r2 + " = model.NewBoolVar('" + py_r1 + "_eq_" + py_r2 + "')");
                                p.println("    model.Add(" + py_r1 +" == " + py_r2 +").OnlyEnforceIf("+ py_r1 + "_eq_" + py_r2+")");
                            }
                        }
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

        // NOT_TOGETHER
        for (GlobalRule g_rule : globalRules) {
            if (g_rule.getGlobalRule().equals("NOT_TOGETHER")) {
                p.println("    # " + g_rule.getGlobalRule() + "(" + g_rule.getDataType() + ", " + g_rule.getDataType2() + ")");
                for (SetOfRules r1 : setOfRequirements.get(g_rule.getDataType())) {
                    for (SetOfRules r2 : setOfRequirements.get(g_rule.getDataType2())) {
                        for (String py_r1 : py_setOfRequirements.get(r1)) {
                            for (String py_r2 : py_setOfRequirements.get(r2)) {
                                p.println("    model.Add(" + py_r1 + " != " + py_r2 + ")");
                            }
                        }
                    }
                }
                p.println();
            }
        }

        // RANKING
        String temp_s;
        if (!hierarchy.isEmpty()) {
            p.println("    # Ranking");
            for (String data : hierarchy.keySet()) {
                for (SetOfRules setR : hierarchy.get(data)) {
                    for (String py_setR : py_setOfRequirements.get(setR)) {
                        for (String plan : ranking.get(setR)) {
                            temp_s = py_setR + "_IS_" + plan.substring(2);
                            p.println("    " + temp_s + " = model.NewBoolVar('" + temp_s + "')");
                        }
                        p.print("    model.AddBoolOr([");
                        int i = 0;
                        for (String plan : ranking.get(setR)) {
                            i=i+1;
                            p.print(py_setR + "_IS_" + plan.substring(2));
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
                            p.println("    model.Add(" + py_setR + " == " + plan.substring(2) + ").OnlyEnforceIf(" +
                                    py_setR + "_IS_" + plan.substring(2) + ")");
                        }
                        for (String plan : ranking.get(setR)) {
                            p.println("    model.Add(PI_" + py_setR + " == " + ranking.get(setR).indexOf(plan) + ").OnlyEnforceIf(" +
                                    py_setR + "_IS_" + plan.substring(2) + ")");
                        }
                        p.println();
                    }
                }
            }
        }

        // OBJECTIVE FUNCTION
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
                            for (String py_setR : py_setOfRequirements.get(setR)) {
                                p.print("PI_"+py_setR);
                                if (py_setOfRequirements.get(setR).indexOf(py_setR) < py_setOfRequirements.get(setR).size()-1) {
                                    p.print(" + ");
                                } else {
                                    need_plus = true;
                                }
                            }
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

        // CREATE A SOLVER
        p.println("    # Creates a solver and solves the model.");
        p.println("    solver = cp_model.CpSolver()\n" +
                "    status = solver.Solve(model)\n");

        // PRINT SOLUTION
        p.println("    if status == cp_model.OPTIMAL or status == cp_model.FEASIBLE:");
        for (String data : setOfRequirements.keySet()) {
            for (SetOfRules setR : setOfRequirements.get(data)) {
                p.print("        print('    "+setR.printRuleName()+" = ");
                for (int i=0; i<py_setOfRequirements.get(setR).size(); i++) {
                    p.print("p_%i");
                    if (i<py_setOfRequirements.get(setR).size() - 1){
                        p.print(",");
                    } else {
                        p.print("' % (");
                    }
                }
                for (String py_plan : py_setOfRequirements.get(setR)) {
                    p.print("solver.Value("+ py_plan +")");
                    if (py_setOfRequirements.get(setR).indexOf(py_plan) < py_setOfRequirements.get(setR).size() - 1) {
                        p.print(",");
                    } else {
                        p.println("))");
                    }
                }
            }
        }
        p.println("    else:\n" +
                "        print('    No solution found.')\n");

        // EXECUTE MAIN
        p.print("if __name__ == '__main__':\n" +
                "    main()");

        p.flush();
        p.close();
        System.out.println("    - result.py: GENERATED\n");
    }
}
