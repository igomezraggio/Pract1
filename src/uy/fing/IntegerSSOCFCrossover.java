package uy.fing;

import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by igomez on 28/07/2016.
 */
public class IntegerSSOCFCrossover {

    private static final double EPS = 1.0E-14D;
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
            Double commonlySelectedFeatures = 0.0; //Nc
            Double selectedFeaturesX1 = 0.0; //N1
            Double selectedFeaturesX2 = 0.0; //N2
            for(int i = 0; i < parent1.getNumberOfVariables(); ++i) {
                int valueX1 = ((Integer)parent1.getVariableValue(i)).intValue();
                int valueX2 = ((Integer)parent2.getVariableValue(i)).intValue();
                if (valueX1 == valueX2 && valueX1 == 1)
                    commonlySelectedFeatures++;
                if (valueX1 == valueX2)
                    commonFeatures++;
                if (valueX1 == 1)
                    selectedFeaturesX1++;
                if (valueX2 == 1)
                    selectedFeaturesX2++;
            }

            Double inheritProbability = (Math.abs(selectedFeaturesX1-selectedFeaturesX2))/((Double)commonlySelectedFeatures);

            for(int i = 0; i < parent1.getNumberOfVariables(); ++i) {
                int valueX1 = ((Integer)parent1.getVariableValue(i)).intValue();
                int valueX2 = ((Integer)parent2.getVariableValue(i)).intValue();
                if (valueX1 == valueX2)
                    continue;
                if(this.randomGenerator.nextDouble() <= probability) {

                }
            }

            for(int i = 0; i < parent1.getNumberOfVariables(); ++i) {
                int valueX1 = ((Integer)parent1.getVariableValue(i)).intValue();
                int valueX2 = ((Integer)parent2.getVariableValue(i)).intValue();
                if(this.randomGenerator.nextDouble() <= 0.5D) {
                    if((double)Math.abs(valueX1 - valueX2) > 1.0E-14D) {
                        double y1;
                        double y2;
                        if(valueX1 < valueX2) {
                            y1 = (double)valueX1;
                            y2 = (double)valueX2;
                        } else {
                            y1 = (double)valueX2;
                            y2 = (double)valueX1;
                        }

                        double yL = (double)parent1.getLowerBound(i).intValue();
                        double yu = (double)parent1.getUpperBound(i).intValue();
                        double rand = this.randomGenerator.nextDouble();
                        double beta = 1.0D + 2.0D * (y1 - yL) / (y2 - y1);
                        double alpha = 2.0D - Math.pow(beta, -(this.distributionIndex + 1.0D));
                        double betaq;
                        if(rand <= 1.0D / alpha) {
                            betaq = Math.pow(rand * alpha, 1.0D / (this.distributionIndex + 1.0D));
                        } else {
                            betaq = Math.pow(1.0D / (2.0D - rand * alpha), 1.0D / (this.distributionIndex + 1.0D));
                        }

                        double c1 = 0.5D * (y1 + y2 - betaq * (y2 - y1));
                        beta = 1.0D + 2.0D * (yu - y2) / (y2 - y1);
                        alpha = 2.0D - Math.pow(beta, -(this.distributionIndex + 1.0D));
                        if(rand <= 1.0D / alpha) {
                            betaq = Math.pow(rand * alpha, 1.0D / (this.distributionIndex + 1.0D));
                        } else {
                            betaq = Math.pow(1.0D / (2.0D - rand * alpha), 1.0D / (this.distributionIndex + 1.0D));
                        }

                        double c2 = 0.5D * (y1 + y2 + betaq * (y2 - y1));
                        if(c1 < yL) {
                            c1 = yL;
                        }

                        if(c2 < yL) {
                            c2 = yL;
                        }

                        if(c1 > yu) {
                            c1 = yu;
                        }

                        if(c2 > yu) {
                            c2 = yu;
                        }

                        if(this.randomGenerator.nextDouble() <= 0.5D) {
                            ((IntegerSolution)offspring.get(0)).setVariableValue(i, Integer.valueOf((int)c2));
                            ((IntegerSolution)offspring.get(1)).setVariableValue(i, Integer.valueOf((int)c1));
                        } else {
                            ((IntegerSolution)offspring.get(0)).setVariableValue(i, Integer.valueOf((int)c1));
                            ((IntegerSolution)offspring.get(1)).setVariableValue(i, Integer.valueOf((int)c2));
                        }
                    } else {
                        ((IntegerSolution)offspring.get(0)).setVariableValue(i, Integer.valueOf(valueX1));
                        ((IntegerSolution)offspring.get(1)).setVariableValue(i, Integer.valueOf(valueX2));
                    }
                } else {
                    ((IntegerSolution)offspring.get(0)).setVariableValue(i, Integer.valueOf(valueX2));
                    ((IntegerSolution)offspring.get(1)).setVariableValue(i, Integer.valueOf(valueX1));
                }
            }
        }

        return offspring;
    }
}
