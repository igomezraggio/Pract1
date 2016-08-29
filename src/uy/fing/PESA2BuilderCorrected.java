package uy.fing;

/**
 * Created by igomez on 29/08/2016.
 */
//  Copia de PESA2Builder pero adaptado para PESA2Corrected

import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.AlgorithmBuilder;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;


public class PESA2BuilderCorrected<S extends Solution<?>> implements AlgorithmBuilder<PESA2Corrected<S>> {
    private final Problem<S> problem;
    private int maxEvaluations ;
    private int archiveSize ;
    private int populationSize ;
    private int biSections ;
    private CrossoverOperator<S> crossoverOperator;
    private MutationOperator<S> mutationOperator;
    private SolutionListEvaluator<S> evaluator;

    /**
     * Constructor
     */
    public PESA2BuilderCorrected(Problem<S> problem, CrossoverOperator<S> crossoverOperator,
                        MutationOperator<S> mutationOperator) {
        this.problem = problem;
        maxEvaluations = 250;
        populationSize = 100;
        archiveSize = 100 ;
        biSections = 5 ;
        this.crossoverOperator = crossoverOperator ;
        this.mutationOperator = mutationOperator ;

        evaluator = new SequentialSolutionListEvaluator<S>();
    }

    public PESA2BuilderCorrected<S> setMaxEvaluations(int maxEvaluations) {
        if (maxEvaluations < 0) {
            throw new JMetalException("maxEvaluations is negative: " + maxEvaluations);
        }
        this.maxEvaluations = maxEvaluations;

        return this;
    }

    public PESA2BuilderCorrected<S> setArchiveSize(int archiveSize) {
        if (archiveSize < 0) {
            throw new JMetalException("archiveSize is negative: " + maxEvaluations);
        }
        this.archiveSize = archiveSize;

        return this;
    }

    public PESA2BuilderCorrected<S> setBisections(int biSections) {
        if (biSections < 0) {
            throw new JMetalException("biSections is negative: " + maxEvaluations);
        }
        this.biSections = biSections;

        return this;
    }

    public PESA2BuilderCorrected<S> setPopulationSize(int populationSize) {
        if (populationSize < 0) {
            throw new JMetalException("Population size is negative: " + populationSize);
        }

        this.populationSize = populationSize;

        return this;
    }

    public PESA2BuilderCorrected<S> setSolutionListEvaluator(SolutionListEvaluator<S> evaluator) {
        if (evaluator == null) {
            throw new JMetalException("evaluator is null");
        }
        this.evaluator = evaluator;

        return this;
    }

    public PESA2Corrected<S> build() {
        PESA2Corrected<S> algorithm  ;
        algorithm = new PESA2Corrected<S>(problem, maxEvaluations, populationSize, archiveSize, biSections,
                crossoverOperator, mutationOperator, evaluator);

        return algorithm ;
    }

    /* Getters */
    public Problem<S> getProblem() {
        return problem;
    }

    public int getMaxEvaluations() {
        return maxEvaluations;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public CrossoverOperator<S> getCrossoverOperator() {
        return crossoverOperator;
    }

    public MutationOperator<S> getMutationOperator() {
        return mutationOperator;
    }

    public SolutionListEvaluator<S> getSolutionListEvaluator() {
        return evaluator;
    }

    public int getBiSections() {
        return biSections ;
    }

    public int getArchiveSize() {
        return archiveSize ;
    }
}
