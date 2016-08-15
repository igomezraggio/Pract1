package uy.fing;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;

import org.uma.jmetal.algorithm.multiobjective.pesa2.PESA2Builder;
import org.uma.jmetal.algorithm.multiobjective.spea2.SPEA2Builder;
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
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by igomez on 31/07/2016.
 */

public class Experiments {

    public static final int INDEPENDENT_RUNS = 1;

    public static void main(String[] args) throws IOException {
        if(args[0].equals("help")){
            System.out.println("Arguments for the Experiments:");
            System.out.println("\t 1- Experiment base directory");
            System.out.println("\t 2- Attribute count without class attribute");
            System.out.println("\t 3- ARFF file");
            System.out.println("\t 4- Multiobjective Algorithm to use: 0 - NSGAII, 1 - SPEAII, 2 - PESAII");
            System.out.println("\t 5- Count of cores to use");
            System.out.println("\t 6- Number of iterations");
            System.out.println("\t 7- Population size");
            System.out.println("\t 8- Crossover Probability: examples: 1.0,0.9,...,0.7");
            System.out.println("\t 9- Mutation Probability: examples: 0.05,0.04,...,0.01");
            System.out.println("\t 10- Count of independent runs: used for statistical tests. Put 1 for default");

        }else {
            if (args.length < 10) {
                throw new JMetalException("Missing arguments") ;
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
                            .setReferenceFrontDirectory(experimentBaseDirectory)
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
            Integer iterations, Integer population, Double crossProb, Double mutProb, Integer moea){
        List<TaggedAlgorithm<List<IntegerSolution>>> algorithms = new ArrayList<>() ;

        IntegerSSOCFCrossover crossover = new IntegerSSOCFCrossover(crossProb);
        IntBinaryFlipMutation mutation = new IntBinaryFlipMutation(mutProb);
        SelectionOperator<List<IntegerSolution>, IntegerSolution> selection = new BinaryTournamentSelection<IntegerSolution>() ;

        Algorithm<List<IntegerSolution>> algorithm;

        for (int run = 0; run < independentRuns; run++) {

            int maxEvaluations = iterations * population;

            for (int i = 0; i < problemList.size(); i++) {

                switch (moea){
                    case 0:
                        algorithm = new NSGAIIBuilder<>(problemList.get(i), crossover, mutation)
                                .setSelectionOperator(selection)
                                .setMaxEvaluations(maxEvaluations)
                                .setPopulationSize(population)
                                .setSolutionListEvaluator(evaluator)
                                .build();
                        algorithms.add(new TaggedAlgorithm<List<IntegerSolution>>(algorithm, "NSGAII_"+run, problemList.get(i), i));
                        break;
                    case 1:
                        algorithm = new SPEA2Builder<>(problemList.get(i), crossover, mutation)
                                .setSelectionOperator(selection)
                                .setMaxIterations(maxEvaluations)
                                .setPopulationSize(population)
                                .setSolutionListEvaluator(evaluator)
                                .build();
                        algorithms.add(new TaggedAlgorithm<List<IntegerSolution>>(algorithm, "SPEA2_"+run, problemList.get(i), i));
                        break;
                    case 2:
                        algorithm = new PESA2Builder<IntegerSolution>(problemList.get(i),crossover,mutation)
                                .setMaxEvaluations(maxEvaluations)
                                .setPopulationSize(population)
                                .setSolutionListEvaluator(evaluator)
                                .build() ;
                        algorithms.add(new TaggedAlgorithm<List<IntegerSolution>>(algorithm, "PESAII_"+run, problemList.get(i), i));
                        break;
                }
            }
        }



        return algorithms ;
    }


}