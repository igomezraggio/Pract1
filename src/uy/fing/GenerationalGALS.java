package uy.fing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.localsearch.ArchiveMutationLocalSearch;
import org.uma.jmetal.operator.impl.localsearch.BasicLocalSearch;
import org.uma.jmetal.operator.impl.mutation.PermutationSwapMutation;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.comparator.ObjectiveComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;


/**
 * Created by igomez on 07/05/2016.
 */
public class GenerationalGALS<S extends Solution<?>> extends AbstractGeneticAlgorithm<S, S> {

    private Comparator<S> comparator;
    private int maxEvaluations;
    private int populationSize;
    private int evaluations;
    private Problem<S> problem;
    private SolutionListEvaluator<S> evaluator;
    private int localSearchIterations = 0;

    public GenerationalGALS(Problem<S> problem, int maxEvaluations, int populationSize, CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator, SelectionOperator<List<S>, S> selectionOperator, SolutionListEvaluator<S> evaluator) {
        this.problem = problem;
        this.maxEvaluations = maxEvaluations;
        this.populationSize = populationSize;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.selectionOperator = selectionOperator;
        this.evaluator = evaluator;
        this.comparator = new ObjectiveComparator(0);
        this.localSearchIterations = 10 + this.problem.getNumberOfVariables() * 5 < 1000 ? 10 + this.problem.getNumberOfVariables() * 5 : 1000;
    }

    @Override
    public void initProgress() {
        this.evaluations = this.populationSize;
    }

    @Override
    public void updateProgress() {
        this.evaluations += this.populationSize;
    }

    @Override
    protected boolean isStoppingConditionReached() {
        return this.evaluations >= this.maxEvaluations;
    }


    @Override
    protected List<S> createInitialPopulation() {
        ArrayList population = new ArrayList(this.populationSize);

        for(int i = 0; i < this.populationSize; ++i) {
            Solution newIndividual = this.problem.createSolution();
            population.add(newIndividual);
        }

        return population;
    }

    @Override
    protected List<S> evaluatePopulation(List<S> population) {
        population = this.evaluator.evaluate(population, this.problem);
        return population;
    }

    @Override
    protected List<S> selection(List<S> population) {
        ArrayList matingPopulation = new ArrayList(population.size());

        for(int i = 0; i < this.populationSize; ++i) {
            Solution solution = (Solution)this.selectionOperator.execute(population);
            matingPopulation.add(solution);
        }

        return matingPopulation;
    }

    @Override
    protected List<S> reproduction(List<S> matingPopulation) {
        ArrayList offspringPopulation = new ArrayList(matingPopulation.size() + 2);

        for(int i = 0; i < this.populationSize; i += 2) {
            ArrayList parents = new ArrayList(2);
            parents.add(matingPopulation.get(i));
            parents.add(matingPopulation.get(i + 1));
            List offspring = (List)this.crossoverOperator.execute(parents);
            this.mutationOperator.execute((S) offspring.get(0));
            this.mutationOperator.execute((S) offspring.get(1));


            BasicLocalSearch search = new BasicLocalSearch(localSearchIterations, new PermutationSwapMutation<>(1), this.comparator,  this.problem);
            offspringPopulation.add(search.execute((Solution<?>) offspring.get(0)));
            offspringPopulation.add(search.execute((Solution<?>) offspring.get(1)));
        }

        return offspringPopulation;
    }

    @Override
    protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
        Collections.sort(population, this.comparator);
        offspringPopulation.add(population.get(0));
        offspringPopulation.add(population.get(1));
        Collections.sort(offspringPopulation, this.comparator);
        offspringPopulation.remove(offspringPopulation.size() - 1);
        offspringPopulation.remove(offspringPopulation.size() - 1);
        return offspringPopulation;
    }

    @Override
    public S getResult() {
        Collections.sort(this.getPopulation(), this.comparator);
        return (S) this.getPopulation().get(0);
    }
}
