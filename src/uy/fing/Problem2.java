package uy.fing;

import org.uma.jmetal.problem.impl.AbstractIntegerPermutationProblem;
import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.util.JMetalException;

import java.io.*;
import java.util.Scanner;

/**
 * Created by igomez on 27/04/2016.
 */
public class Problem2 extends AbstractIntegerPermutationProblem {

    private int[][] costMatrix;
    private int[][] seasonMatrix;
    //private
    private int cityCount;

    public Problem2(String matrixFilePath, String seasonsFilePath){
        this.readConfFiles(matrixFilePath, seasonsFilePath);

        this.setNumberOfVariables(this.cityCount-1);
        this.setNumberOfObjectives(1);
        this.setName("Problem2");
    }

    @Override
    public int getPermutationLength() {
        return this.cityCount-1;
    }

    @Override
    public void evaluate(PermutationSolution<Integer> integerPermutationSolution) {

        /*
        if(integerPermutationSolution.getVariableValue(0) > 0){
            integerPermutationSolution.setObjective(0,1000000000);
        }else {
        */
            float cost = 0;
            int daysTravelled = 1;

            long length = integerPermutationSolution.getNumberOfVariables();
            Integer gene1 = integerPermutationSolution.getVariableValue(0);
            float baseCost = this.costMatrix[0][gene1+1];
            cost = evalCost(daysTravelled,baseCost,cost);
            if(baseCost < 0) {
                cost += 10000000;
            }else{
                daysTravelled += 4;
                for (int i = 0; i < length-1; i++) {

                    Integer gene = integerPermutationSolution.getVariableValue(i);
                    Integer gene2 = integerPermutationSolution.getVariableValue(i+1);
                    baseCost = this.costMatrix[gene+1][gene2+1];

                    if(baseCost < 0) {
                        cost += 10000000;
                        break;
                    }

                    cost = evalCost(daysTravelled,baseCost,cost);
                    daysTravelled += 5;

                }

            }



            integerPermutationSolution.setObjective(0,cost);
        //}
    }

    private float evalCost(int daysTravelled, float baseCost, float totalCost){
        if(daysTravelled >= this.seasonMatrix[0][0] && daysTravelled <= this.seasonMatrix[0][1]){
            totalCost += baseCost;
        }else if (daysTravelled >= this.seasonMatrix[1][0] && daysTravelled <= this.seasonMatrix[1][1]){
            totalCost += Math.round(baseCost *1.10);
        }else {
            totalCost += Math.round(baseCost *1.30);
        }
        return totalCost;
    }

    private void readConfFiles(String matrixFilePath, String seasonsFilePath){

        String filePath = "";
        try{
            filePath = matrixFilePath;
            File file = new File(filePath);
            Scanner scanner = new Scanner(file);
            String[] tokens = null;
            int i = 0;
            while(scanner.hasNext()){
                tokens = scanner.nextLine().split(" ");
                if(i == 0){
                    this.costMatrix = new int[tokens.length][tokens.length];
                    this.cityCount = tokens.length;
                }
                for (int j = 0; j < tokens.length; j++){
                    this.costMatrix[i][j] = Integer.parseInt(tokens[j]);
                }
                i++;
            }

            filePath = seasonsFilePath;
            file = new File(filePath);
            scanner = new Scanner(file);
            tokens = null;
            i = 0;
            while(scanner.hasNext()){
                tokens = scanner.nextLine().split(",");
                if(i == 0){
                    this.seasonMatrix = new int[3][2];
                }
                for (int j = 0; j < 2; j++){
                    this.seasonMatrix[i][j] = Integer.parseInt(tokens[j]);
                }
                i++;
            }

        }catch (Exception e){
            new JMetalException("Problem2.readConfFiles(): error when reading file: " + filePath);
        }

    }
}
