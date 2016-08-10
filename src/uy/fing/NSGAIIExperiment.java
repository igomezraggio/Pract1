package uy.fing;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;

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
import java.util.concurrent.TimeUnit;

/**
 * Created by igomez on 31/07/2016.
 */

public class NSGAIIExperiment {

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            throw new JMetalException("Missing argument: experiment base directory") ;
        }
        String experimentBaseDirectory = args[0] ;
        Integer attributeCount = Integer.valueOf(args[1]);
        String path = args[2];
        Integer moea = Integer.valueOf(args[3]); //codiguera para moeas: 0 - NSGAII, 1 - SPEAII, 2 - PESAII
        Integer coreCount = Integer.valueOf(args[4]);
        Integer iterations = Integer.valueOf(args[5]);
        Integer population = Integer.valueOf(args[6]);
        Double crossoverProb = Double.valueOf(args[7]);
        Double mutationProb = Double.valueOf(args[8]);
        Integer runs = Integer.valueOf(args[9]);


        BufferedReader reader = new BufferedReader(new FileReader(path));

        Instances file = new Instances(reader);
        reader.close();

        List<Problem<IntegerSolution>> problemList = Arrays.<Problem<IntegerSolution>>asList(new AttributesProblem(attributeCount, file)) ;

        List<TaggedAlgorithm<List<IntegerSolution>>> algorithmList = null;

        MultithreadedSolutionListEvaluator evaluator = new MultithreadedSolutionListEvaluator(coreCount,problemList.get(0));

        algorithmList = configureAlgorithmList(problemList, runs, evaluator,iterations,population,crossoverProb,mutationProb,moea);

        List<String> referenceFrontFileNames = Arrays.asList("ATPR.pf") ;

        String moeaSelected = "";
        switch (moea){
            case 0:
                moeaSelected = "NSGAIIExperiment";
                break;
            case 1:
                moeaSelected = "SPEA2Experiment";
                break;
            case 2:
                moeaSelected = "PESAIIExperiment";
                break;
        }

        Experiment<IntegerSolution, List<IntegerSolution>> experiment =
                new ExperimentBuilder<IntegerSolution, List<IntegerSolution>>(moeaSelected)
                        .setAlgorithmList(algorithmList)
                        .setProblemList(problemList)
                        .setExperimentBaseDirectory(experimentBaseDirectory)
                        .setOutputParetoFrontFileName("FUN")
                        .setOutputParetoSetFileName("VAR")
                        .setReferenceFrontDirectory(moeaSelected + "\\ParetoFronts")
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
            List<Problem<IntegerSolution>> problemList, int independentRuns,MultithreadedSolutionListEvaluator evaluator,
            Integer ierations, Integer population, Double crossProb, Double mutProb, Integer moea){
        List<TaggedAlgorithm<List<IntegerSolution>>> algorithms = new ArrayList<>() ;

        IntegerSSOCFCrossover crossover = new IntegerSSOCFCrossover(crossProb);
        IntBinaryFlipMutation mutation = new IntBinaryFlipMutation(mutProb);

        for (int run = 0; run < independentRuns; run++) {

            maxEvaluations = 2700;
            populationSize = 30;
            crossoverProbability = 1.0 ;
            mutationProbability = 0.02 ;

            for (int i = 0; i < problemList.size(); i++) {
                Algorithm<List<IntegerSolution>> algorithm = new NSGAIIBuilder<>(problemList.get(i), crossover, mutation)
                        .setSelectionOperator(selection)
                        .setMaxEvaluations(maxEvaluations)
                        .setPopulationSize(populationSize)
                        .setSolutionListEvaluator(evaluator)
                        .build();
                algorithms.add(new TaggedAlgorithm<List<IntegerSolution>>(algorithm, "NSGAIIcc", problemList.get(i), run));
            }

        }

        return algorithms ;
    }


}