package uy.fing;

import org.uma.jmetal.problem.impl.AbstractBinaryProblem;
import org.uma.jmetal.problem.impl.AbstractIntegerProblem;
import org.uma.jmetal.solution.BinarySolution;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.util.binarySet.BinarySet;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomTree;
import weka.core.Debug;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.Remove;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;

/**
 * Created by igomez on 08/07/2016.
 */
public class AttributesProblem extends AbstractIntegerProblem {

    private Instances file;
    private int ind = 0;

    public AttributesProblem(Integer attCount, Instances instance, RandomTree tree, Remove rmFilter){

        ArrayList indLow = new ArrayList(attCount);
        ArrayList indUpp = new ArrayList(attCount);

        this.setNumberOfVariables(attCount);
        this.setNumberOfObjectives(2);
        for (int i = 0;i < attCount; i++){
            indLow.add(i,0);
            indUpp.add(i,1);
        }

        this.setLowerLimit(indLow);
        this.setUpperLimit(indUpp);
        this.setName("AttributesProblem");

    }

    public AttributesProblem(Integer attCount){

        ArrayList indLow = new ArrayList(attCount);
        ArrayList indUpp = new ArrayList(attCount);

        this.setNumberOfVariables(attCount);
        this.setNumberOfObjectives(2);
        for (int i = 0;i < attCount; i++){
            indLow.add(i,0);
            indUpp.add(i,1);
        }

        this.setLowerLimit(indLow);
        this.setUpperLimit(indUpp);
        this.setName("AttributesProblem");

    }

    public AttributesProblem(Integer attCount, Instances file){

        ArrayList indLow = new ArrayList(attCount);
        ArrayList indUpp = new ArrayList(attCount);

        this.setNumberOfVariables(attCount);
        this.setNumberOfObjectives(2);
        for (int i = 0;i < attCount; i++){
            indLow.add(i,0);
            indUpp.add(i,1);
        }

        this.setLowerLimit(indLow);
        this.setUpperLimit(indUpp);
        this.setName("AttributesProblem");
        this.file = file;
    }
    @Override
    public void evaluate(IntegerSolution integerSolution) {

        evalAttributeCount(integerSolution);

        evalPredictionError(integerSolution);
    }

    public void evalAttributeCount(IntegerSolution integerSolution){
        int vectorLength = integerSolution.getNumberOfVariables();
        Integer objOneCost = new Integer(0);
        for(int i = 0; i < vectorLength; i++){
            objOneCost += integerSolution.getVariableValue(i);
        }

        if(objOneCost == 0){
            integerSolution.setObjective(0,objOneCost+10000); //Tratar de descartar soluciones no factibles. Que no tengan variables seleccionadas.
        }else{
            integerSolution.setObjective(0,objOneCost);
        }
    }

    /**
     * Calls Weka to get the Prediction Error
     * @param integerSolution
     */
    public void evalPredictionError(IntegerSolution integerSolution){


        int vectorLength = integerSolution.getNumberOfVariables();
        String attributes = new String("");
        Double objTwoCost = new Double(0.0);
        if(integerSolution.getVariableValue(0) == 1){
            attributes = attributes +""+1;
        }
        for(int i = 1; i < vectorLength; i++){
            if(integerSolution.getVariableValue(i) == 1){
                if(!attributes.isEmpty())
                    attributes = attributes +""+ ",";
                attributes = attributes+""+(i+1);
            }
        }
        if(!attributes.isEmpty()){
            attributes = attributes+",22"; //classIndex

            //ARMAR BIEN EL STRING Y MANDARSELO A WEKA PARA ASIGNAR EL COSTO AL OBJETIVO
            try{

                Instances train = new Instances(file);
                // setting class attribute
                train.setClassIndex(train.numAttributes() -1);

                RandomTree tree = new RandomTree();
                tree.setMaxDepth(10);

                Remove rmFilter = new Remove();
                rmFilter.setAttributeIndices(attributes);
                boolean is = rmFilter.getInvertSelection();
                rmFilter.setInvertSelection(true);
                // meta-classifier
                FilteredClassifier fc = new FilteredClassifier();
                fc.setFilter(rmFilter);
                fc.setClassifier(tree);

                fc.buildClassifier(train);
                Evaluation eval = new Evaluation(train);

                eval.crossValidateModel(fc, train, 10, new Debug.Random(1));
                eval.confusionMatrix();
                double valf = new Double(eval.areaUnderPRC(0));
                System.out.println("["+Thread.currentThread().getId()+"]:"+" PRC del individuo: " + valf);
                objTwoCost = 1 - valf;

                fc = null;
                train = null;
                eval = null;
                tree = null;
                rmFilter = null;

            }catch (Exception e){
                e.printStackTrace();
            }
        }else {
            objTwoCost = 1.0;
        }

        integerSolution.setObjective(1,objTwoCost);

        ind++;
        System.out.println("["+Thread.currentThread().getId()+"]:"+" Individuo: " + attributes);
        System.out.println("["+Thread.currentThread().getId()+"]:"+" Fitness2 del individuo: " + objTwoCost);
        System.out.println("["+Thread.currentThread().getId()+"]:"+" Terminó evaluación, van: " + ind);


    }

}
