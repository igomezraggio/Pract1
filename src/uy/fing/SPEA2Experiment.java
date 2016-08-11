package uy.fing;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.spea2.SPEA2Builder;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.qualityindicator.impl.*;
import org.uma.jmetal.qualityindicator.impl.hypervolume.PISAHypervolume;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.evaluator.impl.MultithreadedSolutionListEvaluator;
import org.uma.jmetal.util.experiment.Experiment;
import org.uma.jmetal.util.experiment.ExperimentBuilder;
import org.uma.jmetal.util.experiment.component.*;
import org.uma.jmetal.util.experiment.util.TaggedAlgorithm;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by igomez on 02/08/2016.
 */
public class SPEA2Experiment {

    private static final int INDEPENDENT_RUNS = 1 ;

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            throw new JMetalException("Missing argument: experiment base directory") ;
        }
        String experimentBaseDirectory = args[0] ;
        Integer attributeCount = Integer.valueOf(args[1]);
        String path = args[2];

        BufferedReader reader = new BufferedReader(new FileReader(path));

        Instances file = new Instances(reader);
        reader.close();

        List<Problem<IntegerSolution>> problemList = Arrays.<Problem<IntegerSolution>>asList(new AttributesProblem(attributeCount, file)) ;

        List<TaggedAlgorithm<List<IntegerSolution>>> algorithmList = null;

        MultithreadedSolutionListEvaluator evaluator = new MultithreadedSolutionListEvaluator(6,problemList.get(0));

        algorithmList = configureAlgorithmList(problemList, INDEPENDENT_RUNS, evaluator);

        List<String> referenceFrontFileNames = Arrays.asList("ATPR.pf") ;

        Experiment<IntegerSolution, List<IntegerSolution>> experiment =
                new ExperimentBuilder<IntegerSolution, List<IntegerSolution>>("SPEA2Experiment")
                        .setAlgorithmList(algorithmList)
                        .setProblemList(problemList)
                        .setExperimentBaseDirectory(experimentBaseDirectory)
                        .setOutputParetoFrontFileName("FUN")
                        .setOutputParetoSetFileName("VAR")
                        //C:\Users\igomez\Desktop\BECA\AttsProblem\Experiments\data\NSGAIIa\AttributesProblem\ParetoFronts --en casa
                        .setReferenceFrontDirectory("SPEA2Experiment\\ParetoFronts")
                        .setReferenceFrontFileNames(referenceFrontFileNames)
                        .setIndicatorList(Arrays.asList(
                                new Epsilon<IntegerSolution>(),
                                new Spread<IntegerSolution>(),
                                new GenerationalDistance<IntegerSolution>(),
                                new PISAHypervolume<IntegerSolution>(),
                                new InvertedGenerationalDistance<IntegerSolution>(),
                                new InvertedGenerationalDistancePlus<IntegerSolution>()))
                        .setIndependentRuns(INDEPENDENT_RUNS)
                        .setNumberOfCores(1)
                        .build();

        new ExecuteAlgorithms<>(experiment).run();
        new ComputeQualityIndicators<>(experiment).run() ;
        new GenerateLatexTablesWithStatistics(experiment).run() ;
        new GenerateWilcoxonTestTablesWithR<>(experiment).run() ;
        new GenerateFriedmanTestTables<>(experiment).run();
        new GenerateBoxplotsWithR<>(experiment).setRows(3).setColumns(3).run() ;

        evaluator.shutdown();
    }

    /**
     * The algorithm list is composed of pairs {@link Algorithm} + {@link Problem} which form part of a
     * {@link TaggedAlgorithm}, which is a decorator for class {@link Algorithm}. The {@link TaggedAlgorithm}
     * has an optional tag component, that can be set as it is shown in this example, where four variants of a
     * same algorithm are defined.
     *
     * @param problemList
     * @return
     */
    static List<TaggedAlgorithm<List<IntegerSolution>>> configureAlgorithmList(
            List<Problem<IntegerSolution>> problemList, int independentRuns,MultithreadedSolutionListEvaluator evaluator){
        List<TaggedAlgorithm<List<IntegerSolution>>> algorithms = new ArrayList<>() ;

        double crossoverProbability = 0.7 ;
        CrossoverOperator<IntegerSolution> crossover = new IntegerSSOCFCrossover(crossoverProbability) ;
        MutationOperator<IntegerSolution> mutation;
        SelectionOperator<List<IntegerSolution>, IntegerSolution> selection;

        double mutationProbability = 0.05 ;
        mutation = new IntBinaryFlipMutation(mutationProbability);

        selection = new BinaryTournamentSelection<IntegerSolution>() ;


        Integer maxEvaluations = 200;
        Integer populationSize = 8;
        int maxIterations = maxEvaluations / populationSize;
        for (int run = 0; run < independentRuns; run++) {

            maxEvaluations = 30;
            populationSize = 8;
            crossoverProbability = 1.0 ;
            mutationProbability = 0.05 ;
            crossover = new IntegerSSOCFCrossover(crossoverProbability) ;
            mutation = new IntBinaryFlipMutation(mutationProbability);
            for (int i = 0; i < problemList.size(); i++) {
                Algorithm<List<IntegerSolution>> algorithm = new SPEA2Builder<>(problemList.get(i), crossover, mutation)
                        .setSelectionOperator(selection)
                        .setMaxIterations(maxEvaluations)
                        .setPopulationSize(populationSize)
                        .setSolutionListEvaluator(evaluator)
                        .build();
                algorithms.add(new TaggedAlgorithm<List<IntegerSolution>>(algorithm, "SPEA2_1", problemList.get(i), run));
            }

            maxEvaluations = 40;
            populationSize = 8;
            for (int i = 0; i < problemList.size(); i++) {
                Algorithm<List<IntegerSolution>> algorithm = new SPEA2Builder<>(problemList.get(i), crossover, mutation)
                        .setSelectionOperator(selection)
                        .setMaxIterations(maxEvaluations)
                        .setPopulationSize(populationSize)
                        .setSolutionListEvaluator(evaluator)
                        .build();
                algorithms.add(new TaggedAlgorithm<List<IntegerSolution>>(algorithm, "SPEA2_2", problemList.get(i), run));
            }

            maxEvaluations = 50;
            populationSize = 8;
            for (int i = 0; i < problemList.size(); i++) {
                Algorithm<List<IntegerSolution>> algorithm = new SPEA2Builder<>(problemList.get(i), crossover, mutation)
                        .setSelectionOperator(selection)
                        .setMaxIterations(maxEvaluations)
                        .setPopulationSize(populationSize)
                        .setSolutionListEvaluator(evaluator)
                        .build();
                algorithms.add(new TaggedAlgorithm<List<IntegerSolution>>(algorithm, "SPEA2_3", problemList.get(i), run));
            }

            maxEvaluations = 60;
            populationSize = 8;
            for (int i = 0; i < problemList.size(); i++) {
                Algorithm<List<IntegerSolution>> algorithm = new SPEA2Builder<>(problemList.get(i), crossover, mutation)
                        .setSelectionOperator(selection)
                        .setMaxIterations(maxEvaluations)
                        .setPopulationSize(populationSize)
                        .setSolutionListEvaluator(evaluator)
                        .build();
                algorithms.add(new TaggedAlgorithm<List<IntegerSolution>>(algorithm, "SPEA2_4", problemList.get(i), run));
            }

            maxEvaluations = 70;
            populationSize = 8;
            for (int i = 0; i < problemList.size(); i++) {
                Algorithm<List<IntegerSolution>> algorithm = new SPEA2Builder<>(problemList.get(i), crossover, mutation)
                        .setSelectionOperator(selection)
                        .setMaxIterations(maxEvaluations)
                        .setPopulationSize(populationSize)
                        .setSolutionListEvaluator(evaluator)
                        .build();
                algorithms.add(new TaggedAlgorithm<List<IntegerSolution>>(algorithm, "SPEA2dd", problemList.get(i), run));
            }

            maxEvaluations = 80;
            populationSize = 8;
            for (int i = 0; i < problemList.size(); i++) {
                Algorithm<List<IntegerSolution>> algorithm = new SPEA2Builder<>(problemList.get(i), crossover, mutation)
                        .setSelectionOperator(selection)
                        .setMaxIterations(maxEvaluations)
                        .setPopulationSize(populationSize)
                        .setSolutionListEvaluator(evaluator)
                        .build();
                algorithms.add(new TaggedAlgorithm<List<IntegerSolution>>(algorithm, "SPEA2de", problemList.get(i), run));
            }

            maxEvaluations = 90;
            populationSize = 8;
            for (int i = 0; i < problemList.size(); i++) {
                Algorithm<List<IntegerSolution>> algorithm = new SPEA2Builder<>(problemList.get(i), crossover, mutation)
                        .setSelectionOperator(selection)
                        .setMaxIterations(maxEvaluations)
                        .setPopulationSize(populationSize)
                        .setSolutionListEvaluator(evaluator)
                        .build();
                algorithms.add(new TaggedAlgorithm<List<IntegerSolution>>(algorithm, "SPEA2df", problemList.get(i), run));
            }

        }

        return algorithms ;
    }


}
