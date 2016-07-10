package uy.fing;

import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

/**
 * Created by igomez on 08/07/2016.
 */
public class IntBinaryFlipMutation  implements MutationOperator<IntegerSolution> {
    private double mutationProbability;
    private JMetalRandom randomGenerator;

    public IntBinaryFlipMutation(double mutationProbability) {
        if(mutationProbability < 0.0D) {
            throw new JMetalException("Mutation probability is negative: " + mutationProbability);
        } else {
            this.mutationProbability = mutationProbability;
            this.randomGenerator = JMetalRandom.getInstance();
        }
    }

    public double getMutationProbability() {
        return this.mutationProbability;
    }

    public IntegerSolution execute(IntegerSolution solution) {
        if(null == solution) {
            throw new JMetalException("Null parameter");
        } else {
            this.doMutation(this.mutationProbability, solution);
            return solution;
        }
    }

    public void doMutation(double probability, IntegerSolution solution) {
        for(int i = 0; i < solution.getNumberOfVariables(); ++i) {
            for(int j = 0; j < solution.getNumberOfVariables(); ++j) {
                if(this.randomGenerator.nextDouble() <= probability) {
                    if(solution.getVariableValue(i) == 0){
                        solution.setVariableValue(i,1);
                    }else{
                        solution.setVariableValue(i,0);
                    }
                }
            }
        }

    }
}
