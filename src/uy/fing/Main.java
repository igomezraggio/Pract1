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
import org.uma.jmetal.solution.impl.DefaultIntegerSolution;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.ProblemUtils;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.MultithreadedSolutionListEvaluator;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.front.util.FrontNormalizer;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomTree;
import weka.core.Debug;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.Remove;

import java.io.*;
import java.util.*;
import java.util.logging.Handler;

import static org.uma.jmetal.runner.AbstractAlgorithmRunner.printFinalSolutionSet;
import static org.uma.jmetal.util.JMetalLogger.logger;

/**
 * Created by igomez on 26/04/2016.
 */
public class Main {

    public static void main(String[] args){

        Integer expCountNSGAII = Integer.valueOf(args[0]);
        Integer expCountSPEA2 = Integer.valueOf(args[1]);
        Integer expCountPESAII = Integer.valueOf(args[2]);
        String experimentsParentFolder = args[3];
        //Integer moea = Integer.valueOf(args[5]); //codiguera para moeas: 0 - NSGAII, 1 - SPEAII, 2 - PAES

        try{
            /*
            BufferedReader reader = new BufferedReader(
                    new FileReader(path));

            Instances file = new Instances(reader);
            reader.close();

            AttributesProblem problem = new AttributesProblem(attributeCount, file);


            String pareto1 = executeMoea(0, attributeCount, threadCount, iterations, pop, problem);
            String pareto2 = executeMoea(1, attributeCount, threadCount, iterations, pop, problem);
            */

            //PODER USAR PARA GENERAR MEJOR PARETO DE TODAS LAS ITERACIONES DE CADA MOEA
            //LUEGO HACER PARETO GLOBAL

            ArrayList<String> fronts = new ArrayList();
            ArrayList<String> frontsIndividuals = new ArrayList();
            for (int i = 0; i < expCountNSGAII; i++) {
                fronts.add(experimentsParentFolder + "\\NSGAIIExperiment\\data\\NSGAII_"+i+"\\AttributesProblem\\FUN0.tsv");
                frontsIndividuals.add(experimentsParentFolder + "\\NSGAIIExperiment\\data\\NSGAII_"+i+"\\AttributesProblem\\VAR0.tsv");
            }
            for (int i = 0; i < expCountSPEA2; i++) {
                fronts.add(experimentsParentFolder + "\\SPEA2Experiment\\data\\SPEA2_"+i+"\\AttributesProblem\\FUN0.tsv");
                frontsIndividuals.add(experimentsParentFolder + "\\SPEA2Experiment\\data\\SPEA2_"+i+"\\AttributesProblem\\VAR0.tsv");
            }
            for (int i = 0; i < expCountPESAII; i++) {
                fronts.add(experimentsParentFolder + "\\PESAIIExperiment\\data\\PESAII_"+i+"\\AttributesProblem\\FUN0.tsv");
                frontsIndividuals.add(experimentsParentFolder + "\\PESAIIExperiment\\data\\PESAII_"+i+"\\AttributesProblem\\VAR0.tsv");
            }


            ArrayList<Item> globalPareto = new ArrayList<>();
            for (int i = 0; i < fronts.size(); i++) {
                BufferedReader reader1 = new BufferedReader(
                        new FileReader(fronts.get(i)));

                BufferedReader reader2 = new BufferedReader(
                        new FileReader(frontsIndividuals.get(i)));


                String line;
                StringTokenizer tokenizer;
                Double obj1;
                Double obj2;
                String genome;


                while((line = reader1.readLine()) != null){
                    tokenizer = new StringTokenizer(line, " ");

                    obj1 = Double.valueOf(tokenizer.nextToken());
                    obj2 = Double.valueOf(tokenizer.nextToken());
                    genome = reader2.readLine();

                    Item item = new Item(obj1,obj2,genome);
                    globalPareto.add(item);
                }
            }


            System.out.println("ParetoEntero");
            for (int i = 0; i < globalPareto.size(); i++) {
                System.out.print("("+globalPareto.get(i).getObj1() +";"+globalPareto.get(i).getObj2()+"),");
            }

            ArrayList<Item> dominatedList = new ArrayList<>();

            for (int i = 0; i < globalPareto.size(); i++) {
                for (int j = 0; j < globalPareto.size(); j++) {
                    if (globalPareto.get(i).getObj1().doubleValue() > globalPareto.get(j).getObj1().doubleValue() &&
                            globalPareto.get(i).getObj2().doubleValue() >= globalPareto.get(j).getObj2().doubleValue()){
                        dominatedList.add(globalPareto.get(i));
                        break;
                    }
                    if (globalPareto.get(i).getObj1().doubleValue() == globalPareto.get(j).getObj1().doubleValue() &&
                            globalPareto.get(i).getObj2().doubleValue() > globalPareto.get(j).getObj2().doubleValue()){
                        dominatedList.add(globalPareto.get(i));
                        break;
                    }
                }
            }

            for (int i = 0; i < dominatedList.size(); i++) {
                globalPareto.remove(dominatedList.get(i));
            }

            System.out.println("\nParetoFiltrado");
            for (int i = 0; i < globalPareto.size(); i++) {
                System.out.println(
                        "("+globalPareto.get(i).getObj1() +";"+globalPareto.get(i).getObj2()
                                +";\n\t"+globalPareto.get(i).getAttributes()
                        +"\n),");
            }


        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static String executeMoea(int moea, int attributeCount, int threadCount, int iterations, int pop, AttributesProblem problem){

        Algorithm<List<IntegerSolution>> algorithm = null;
        CrossoverOperator<IntegerSolution> crossover;
        MutationOperator<IntegerSolution> mutation;
        SelectionOperator<List<IntegerSolution>, IntegerSolution> selection;

        double crossoverProbability = 0.7 ;
        crossover = new IntegerSSOCFCrossover(crossoverProbability) ;

        double mutationProbability = 0.05 ;
        mutation = new IntBinaryFlipMutation(mutationProbability);

        selection = new BinaryTournamentSelection<IntegerSolution>() ;


        MultithreadedSolutionListEvaluator evaluator = new MultithreadedSolutionListEvaluator(threadCount,problem);

        String stringMoea = "";
        switch (moea){
            case 0:
                //400 iteraciones puse en el doc
                //50 individuos
                algorithm = new NSGAIIBuilder<IntegerSolution>(problem, crossover, mutation)
                        .setSelectionOperator(selection)
                        .setMaxEvaluations(iterations*pop)
                        .setPopulationSize(pop)
                        .setSolutionListEvaluator(evaluator)
                        .build() ;
                stringMoea = "NSGAII";
                break;
            case 1:
                algorithm = new SPEA2Builder<IntegerSolution>(problem, crossover, mutation)
                        .setMaxIterations(iterations)
                        .setPopulationSize(pop)
                        .setSolutionListEvaluator(evaluator)
                        .build() ;
                stringMoea = "SPEA2";
                break;
            case 2:
                algorithm = new PAESBuilder<IntegerSolution>(problem)
                        .setArchiveSize(pop)
                        .setMaxEvaluations(iterations)
                        .setMutationOperator(mutation)
                        .build() ;
                stringMoea = "PAES";
                break;
        }

        AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
                .execute() ;

        List<IntegerSolution> population = algorithm.getResult() ;
        long computingTime = algorithmRunner.getComputingTime() ;

        JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");

        //printFinalSolutionSet(population);


        new SolutionListOutput(population)
                .setSeparator("\t")
                .setVarFileOutputContext(new DefaultFileOutputContext("VAR"+stringMoea+".tsv"))
                .setFunFileOutputContext(new DefaultFileOutputContext("FUN"+stringMoea+".tsv"))
                .print();

        JMetalLogger.logger.info("Random seed: " + JMetalRandom.getInstance().getSeed());
        JMetalLogger.logger.info("Objectives values have been written to file FUN"+stringMoea+".tsv");
        JMetalLogger.logger.info("Variables values have been written to file VAR"+stringMoea+".tsv");

        evaluator.shutdown();

        /*
        Front referenceFront = null;
        try {
            referenceFront = new ArrayFront("C:\\Users\\igomez\\IdeaProjects\\Pract1\\FUN2.tsv");
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

        */
        stringMoea = "VAR"+stringMoea+".tsv";
        return stringMoea;

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
