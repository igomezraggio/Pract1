package uy.fing;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.algorithm.multiobjective.paes.PAESBuilder;
import org.uma.jmetal.algorithm.multiobjective.pesa2.PESA2;
import org.uma.jmetal.algorithm.multiobjective.pesa2.PESA2Builder;
import org.uma.jmetal.algorithm.multiobjective.spea2.SPEA2Builder;
import org.uma.jmetal.algorithm.singleobjective.geneticalgorithm.GenerationalGeneticAlgorithm;
import org.uma.jmetal.algorithm.singleobjective.geneticalgorithm.GeneticAlgorithmBuilder;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.crossover.IntegerSBXCrossover;
import org.uma.jmetal.operator.impl.crossover.PMXCrossover;
import org.uma.jmetal.operator.impl.mutation.BitFlipMutation;
import org.uma.jmetal.operator.impl.mutation.IntegerPolynomialMutation;
import org.uma.jmetal.operator.impl.mutation.PermutationSwapMutation;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.operator.impl.selection.RankingAndCrowdingSelection;
import org.uma.jmetal.operator.impl.selection.TournamentSelection;
import org.uma.jmetal.qualityIndicator.CommandLineIndicatorRunner;
import org.uma.jmetal.qualityindicator.QualityIndicator;
import org.uma.jmetal.qualityindicator.impl.Hypervolume;
import org.uma.jmetal.runner.multiobjective.NSGAIIIntegerRunner;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.ProblemUtils;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.MultithreadedSolutionListEvaluator;
import org.uma.jmetal.util.fileoutput.SolutionSetOutput;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.front.util.FrontNormalizer;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomTree;
import weka.core.Debug;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.Remove;

import java.io.*;
import java.util.List;
import java.util.logging.Handler;

import static org.uma.jmetal.runner.AbstractAlgorithmRunner.printFinalSolutionSet;
import static org.uma.jmetal.util.JMetalLogger.logger;

/**
 * Created by igomez on 26/04/2016.
 */
public class Main {

    public static void main(String[] args){

        Integer attributeCount = Integer.valueOf(args[0]);
        String path = args[1];
        Integer threadCount = Integer.valueOf(args[2]);
        Integer iterations = Integer.valueOf(args[3]);
        Integer pop = Integer.valueOf(args[4]);
        Integer moea = Integer.valueOf(args[5]);; //codiguera para moeas: 0 - NSGAII, 1 - SPEAII, 2 - PAES
        try{

            BufferedReader reader = new BufferedReader(
                    new FileReader(path));

            Instances file = new Instances(reader);
            reader.close();

            AttributesProblem problem = new AttributesProblem(attributeCount, file);

            executeMoea(moea, attributeCount, threadCount, iterations, pop, problem);


        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void executeMoea(int moea, int attributeCount, int threadCount, int iterations, int pop, AttributesProblem problem){

        Algorithm<List<IntegerSolution>> algorithm = null;
        CrossoverOperator<IntegerSolution> crossover;
        MutationOperator<IntegerSolution> mutation;
        SelectionOperator<List<IntegerSolution>, IntegerSolution> selection;

        double crossoverProbability = 0.7 ;
        double crossoverDistributionIndex = 20.0 ;
        crossover = new IntegerSBXCrossover(crossoverProbability, crossoverDistributionIndex) ;

        double mutationProbability = 0.05 ;
        mutation = new IntBinaryFlipMutation(mutationProbability);

        selection = new BinaryTournamentSelection<IntegerSolution>() ;

        MultithreadedSolutionListEvaluator evaluator = new MultithreadedSolutionListEvaluator(threadCount,problem);

        switch (moea){
            case 0:
                //400 iteraciones puse en el doc
                //50 individuos
                algorithm = new NSGAIIBuilder<IntegerSolution>(problem, crossover, mutation)
                        .setSelectionOperator(selection)
                        .setMaxIterations(iterations)
                        .setPopulationSize(pop)
                        .setSolutionListEvaluator(evaluator)
                        .build() ;
                break;
            case 1:
                algorithm = new SPEA2Builder<IntegerSolution>(problem, crossover, mutation)
                        .setMaxIterations(iterations)
                        .setPopulationSize(pop)
                        .setSolutionListEvaluator(evaluator)
                        .build() ;
                break;
            case 2:
                algorithm = new PAESBuilder<IntegerSolution>(problem)
                        .setArchiveSize(pop)
                        .setMaxEvaluations(iterations)
                        .setMutationOperator(mutation)
                        .build() ;
                break;
        }

        AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
                .execute() ;

        List<IntegerSolution> population = algorithm.getResult() ;
        long computingTime = algorithmRunner.getComputingTime() ;

        JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");

        printFinalSolutionSet(population);

        evaluator.shutdown();

        Front referenceFront = null;
        try {
            referenceFront = new ArrayFront("C:\\Users\\igomez\\Desktop\\Beca\\Maestría\\AE\\Pract1\\FUN2.tsv");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        FrontNormalizer frontNormalizer = new FrontNormalizer(referenceFront) ;
        Front normalizedReferenceFront = frontNormalizer.normalize(referenceFront) ;

        Hypervolume<List<? extends Solution<?>>> hypervolume ;
        hypervolume = new Hypervolume<List<? extends Solution<?>>>(normalizedReferenceFront) ;

        double hvValue = hypervolume.evaluate(population) ;

        System.out.println("Hypervolume: " + hvValue);
        //CommandLineIndicatorRunner runner = new CommandLineIndicatorRunner();

    }


    public void executeProblem2(String[] args){
        String matrix = args[0];
        String seasons = args[1];

        Problem2 problem = new Problem2(matrix,seasons);

        GenerationalGALS<PermutationSolution<Integer>> algorithm;

        PMXCrossover crossover = new PMXCrossover(0.7);
        PermutationSwapMutation mutation = new PermutationSwapMutation(0.05);
        BinaryTournamentSelection selection = new BinaryTournamentSelection<PermutationSolution<Integer>>(new RankingAndCrowdingDistanceComparator<PermutationSolution<Integer>>());
        MultithreadedSolutionListEvaluator evaluator = new MultithreadedSolutionListEvaluator(1,problem);
        algorithm = new GenerationalGALS<>(problem, 10000, 100, crossover, mutation, selection, evaluator);

        AlgorithmRunner runner = new AlgorithmRunner.Executor(algorithm).execute();
        long computingTime = runner.getComputingTime();

        logger.info("Total execution time: " + computingTime + "ms");
        for(Handler h:logger.getHandlers())
        {
            h.close();
        }

        PermutationSolution<Integer> result = algorithm.getResult();

        String resultS = "0 ";
        for (int i = 0; i < result.getNumberOfVariables(); i++){
            int realVar = result.getVariableValue(i) +1;
            resultS = resultS + realVar + " ";
        }
        System.out.println("Result: "+ resultS);
        System.out.println("Cost: " + result.getObjective(0));


        PrintWriter writer = null;
        try {
            writer = new PrintWriter("solucion.out", "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        writer.println(resultS);
        writer.close();

        evaluator.shutdown();
    }
}
