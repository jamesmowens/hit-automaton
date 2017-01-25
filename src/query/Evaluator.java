package query;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Evaluator {

    public static boolean evaluateLine(String name) throws Exception {
        boolean comparison = true;
        String parsedEquation = "";
        String var = new String();

        String n[] = name.split(" ");
        int nCount = 0;
        for (String m : n) {
            if (m.equals("+")) {
                parsedEquation = parsedEquation.concat(m+" ");
            } else if (m.equals("-")) {
                parsedEquation = parsedEquation.concat(m+" ");
            } else if (m.equals("*")) {
                parsedEquation = parsedEquation.concat(m+" ");
            } else if (m.equals("/")) {
                parsedEquation = parsedEquation.concat(m+" ");
            } else if (m.matches("[-+]?[0-9]")) {
                Integer.parseInt(m);
                parsedEquation = parsedEquation.concat(m+" ");
            } else if (m.matches("[-+]?[0-9]*\\.?[0-9]+")) {
                Float.parseFloat(m);
                parsedEquation = parsedEquation.concat(m+" ");
            } else if (m.matches(">=")) {
                comparison = true;
                parsedEquation = parsedEquation.concat(m+" ");
            } else if (m.matches("<=")) {
                comparison = true;
                parsedEquation = parsedEquation.concat(m+" ");
            } else if (m.matches(">")) {
                comparison = true;
                parsedEquation = parsedEquation.concat(m+" ");
            } else if (m.matches("<")) {
                comparison = true;
                parsedEquation = parsedEquation.concat(m+" ");
            } else if (m.matches("=")) {
                System.out.println(m+": assignment to "+n[nCount-1]);
                var = n[nCount-1];
                parsedEquation = parsedEquation.concat(m+" ");
                comparison = false;
            } else {
                double temp = getVariable(m);
                System.out.println(m + ": " + temp);
                parsedEquation = parsedEquation.concat(temp+" ");
            }
            nCount++;
        }

        System.out.println(parsedEquation);

        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");

        if (comparison) {
            System.out.println(engine.eval(parsedEquation));
            return  (Boolean) engine.eval(parsedEquation);
        } else {
            String rightHandSide = parsedEquation.split("= ")[1];
            double x = (Double) engine.eval(rightHandSide);
            System.out.println(var+" is now "+x);
            updateVariable(var, x);
            return true;
        }

    }
    
    
    /**
     * Pulls the current value of a variable from global "variables.txt"
     *
     * @param name name of variable
     * @return integer value of variable
     * @throws Exception //TODO
     */
    private static double getVariable(String name) throws Exception {

        // Retrieves variables.txt from relative file path
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        BufferedReader file = new BufferedReader(new FileReader(s + "/variables.txt")); //TODO stored in src file

        // Checks for line matching
        String line = file.readLine();
        while (line != null) {
            if (line.startsWith(name + " =")) {
                // assuming the var to the right of the equals is an int
                double temp = Float.parseFloat(line.substring(line.lastIndexOf("= ") + 2));
                return temp;
            }
            line = file.readLine();
        }
        System.out.println("Error: no variable of that name present"); //TODO
        file.close();
        return 0;
    }

    private static void updateVariable(String name, double value) throws Exception { //TODO
        // Retrieves variables.txt from relative file path
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        BufferedReader var = new BufferedReader(new FileReader(s + "/variables.txt")); //TODO stored in src file

        FileWriter temp = new FileWriter(s + "/temp.txt");
        // Checks for line matching
        String line = var.readLine();
        while (line != null) {
            if (line.startsWith(name + " =")) {
                temp.append(name + " = " + value + "\n");
            } else {
                temp.append(line+"\n");
            }
            line = var.readLine();
        }
        var.close();
        temp.close();

        File tempFile = new File(s + "/temp.txt");
        File varFile = new File(s + "/variables.txt");

        tempFile.renameTo(varFile);
    }
}