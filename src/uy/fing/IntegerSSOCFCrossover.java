package uy.fing;

import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by igomez on 28/07/2016.
 */
public class IntegerSSOCFCrossover implements CrossoverOperator<IntegerSolution> {

    private double crossoverProbability;
    private JMetalRandom randomGenerator;

    public IntegerSSOCFCrossover(double crossoverProbability) {
        if(crossoverProbability < 0.0D) {
            throw new JMetalException("Crossover probability is negative: " + crossoverProbability);
        } else {
            this.crossoverProbability = crossoverProbability;
            this.randomGenerator = JMetalRandom.getInstance();
        }
    }

    public double getCrossoverProbability() {
        return this.crossoverProbability;
    }

    public List<IntegerSolution> execute(List<IntegerSolution> solutions) {
        if(null == solutions) {
            throw new JMetalException("Null parameter");
        } else if(solutions.size() != 2) {
            throw new JMetalException("There must be two parents instead of " + solutions.size());
        } else {
            return this.doCrossover(this.crossoverProbability, (IntegerSolution)solutions.get(0), (IntegerSolution)solutions.get(1));
        }
    }

    public List<IntegerSolution> doCrossover(double probability, IntegerSolution parent1, IntegerSolution parent2) {
        ArrayList offspring = new ArrayList(2);
        offspring.add((IntegerSolution)parent1.copy());
        offspring.add((IntegerSolution)parent2.copy());
        if(this.randomGenerator.nextDouble() <= probability) {

            Integer commonFeatures = 0;
            Double NonSharedSelectedFeatures = 0.0; //Nu
            Double commonlySelectedFeatures = 0.0; //Nc
            Double selectedFeaturesX1 = 0.0; //N1
            Double selectedFeaturesX2 = 0.0; //N2
            for (int i = 0; i < parent1.getNumberOfVariables(); ++i) {
                int valueX1 = ((Integer) parent1.getVariableValue(i)).intValue();
                int valueX2 = ((Integer) parent2.getVariableValue(i)).intValue();
                if (valueX1 == valueX2 && valueX1 == 1)
                    commonlySelectedFeatures++;
                if (valueX1 != valueX2 && (valueX1 == 1 || valueX2 == 1))
                    NonSharedSelectedFeatures++;
                if (valueX1 == valueX2)
                    commonFeatures++;
                if (valueX1 == 1)
                    selectedFeaturesX1++;
                if (valueX2 == 1)
                    selectedFeaturesX2++;
            }

            Double inheritProbability = (Math.abs(selectedFeaturesX1 - commonlySelectedFeatures)) / ((Double) NonSharedSelectedFeatures);

            for (int i = 0; i < parent1.getNumberOfVariables(); ++i) {
                int valueX1 = ((Integer) parent1.getVariableValue(i)).intValue();
                int valueX2 = ((Integer) parent2.getVariableValue(i)).intValue();
                if (valueX1 == valueX2)
                    continue;
                if (this.randomGenerator.nextDouble() <= inheritProbability) {
                    ((IntegerSolution) offspring.get(0)).setVariableValue(i, Integer.valueOf((int) valueX1));
                    ((IntegerSolution) offspring.get(1)).setVariableValue(i, Integer.valueOf((int) valueX2));
                } else {
                    ((IntegerSolution) offspring.get(0)).setVariableValue(i, Integer.valueOf((int) valueX2));
                    ((IntegerSolution) offspring.get(1)).setVariableValue(i, Integer.valueOf((int) valueX1));
                }
            }
        }

        return offspring;
    }
}
