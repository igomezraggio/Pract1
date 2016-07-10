package uy.fing;

import org.uma.jmetal.problem.impl.AbstractBinaryProblem;
import org.uma.jmetal.problem.impl.AbstractIntegerProblem;
import org.uma.jmetal.solution.BinarySolution;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.util.binarySet.BinarySet;

import java.util.ArrayList;

/**
 * Created by igomez on 08/07/2016.
 */
public class AttributesProblem extends AbstractIntegerProblem {

    private Integer objOneCost = 0;

    public AttributesProblem(){

        this.setNumberOfVariables(10);
        this.setNumberOfObjectives(1);
        this.setName("AttributesProblem");
        ArrayList upp = new ArrayList<Integer>();
        ArrayList loww = new ArrayList<Integer>();
        for (int i = 0; i < 10; i++ ){
            upp.add(i,1);
            loww.add(i,0);
        }

        this.setUpperLimit(upp);
        this.setLowerLimit(loww);
    }

    @Override
    public void evaluate(IntegerSolution integerSolution) {
        int vectorLength = integerSolution.getNumberOfVariables();
        objOneCost = 0;
        for(int i = 0; i < vectorLength; i++){
            objOneCost += integerSolution.getVariableValue(i);
        }

        if(objOneCost == 0){
            integerSolution.setObjective(0,objOneCost+10000); //Tratar de descartar soluciones no factibles. Que no tengan variables seleccionadas.
        }else{
            integerSolution.setObjective(0,objOneCost);
        }

    }

}
