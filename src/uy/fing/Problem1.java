package uy.fing;
import org.uma.jmetal.problem.ConstrainedProblem;
import org.uma.jmetal.problem.impl.AbstractIntegerPermutationProblem;
import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.solutionattribute.impl.NumberOfViolatedConstraints;
import org.uma.jmetal.util.solutionattribute.impl.OverallConstraintViolation;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by igomez on 26/04/2016.
 */
public class Problem1 extends AbstractIntegerPermutationProblem implements ConstrainedProblem<PermutationSolution<Integer>> {

    public OverallConstraintViolation<PermutationSolution<Integer>> overallConstraintViolationDegree ;
    public NumberOfViolatedConstraints<PermutationSolution<Integer>> numberOfViolatedConstraints ;

    private static final Map<String,Integer> map;
    static
    {
        map = new HashMap<String, Integer>();
        map.put("01B", 10);
        map.put("01M", 12);
        map.put("01A", 13);
        map.put("02B", 20);
        map.put("02M", 23);
        map.put("02A", 26);
        map.put("03B", 5);
        map.put("03M", 5);
        map.put("03A", 6);
        map.put("12B", 15);
        map.put("12M", 17);
        map.put("12A", 20);
        map.put("13B", 12);
        map.put("13M", 14);
        map.put("13A", 16);
        map.put("23B", 15);
        map.put("23M", 17);
        map.put("23A", 20);

    }

    public Problem1() {
        setNumberOfVariables(4);
        setNumberOfObjectives(1);
        setNumberOfConstraints(1);
    }

    @Override
    public int getPermutationLength() {
        return 4;
    }

    @Override
    public void evaluateConstraints(PermutationSolution<Integer> solution) {
        int violatedConstraints = 0;
        double overallConstraintViolation = 0.0;
        for (int i = 0; i < getNumberOfConstraints(); i++) {
            if (solution.getVariableValue(0)>0){
                overallConstraintViolation+=solution.getVariableValue(0);
                violatedConstraints++;
            }
        }
        overallConstraintViolationDegree.setAttribute(solution, overallConstraintViolation);
        numberOfViolatedConstraints.setAttribute(solution, violatedConstraints);
    }

    @Override
    public void evaluate(PermutationSolution<Integer> integerPermutationSolution) {

        float cost = 0;
        char season;

        long length = integerPermutationSolution.getNumberOfVariables();
        for (int i = 0; i < length-1; i++) {
            Integer gene = integerPermutationSolution.getVariableValue(i);
            Integer gene2 = integerPermutationSolution.getVariableValue(i+1);
            switch (i){
                case 0:
                    season = 'B';
                    break;
                case 1:
                    season = 'M';
                    break;
                default:
                    season = 'A';
                    break;
            }
            if(gene < gene2){
                cost += map.get(""+gene+""+gene2+""+season);
            }else{
                cost += map.get(""+gene2+""+gene+""+season);
            }
        }

        if(integerPermutationSolution.getVariableValue(0) > 0){
            integerPermutationSolution.setObjective(0,1000000000);
        }else{
            integerPermutationSolution.setObjective(0,cost);
        }

    }

}
