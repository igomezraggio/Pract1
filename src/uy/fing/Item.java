package uy.fing;

import java.util.StringTokenizer;

/**
 * Created by igomez on 8/6/2016.
 */
public class Item {

    private Double obj1;
    private Double obj2;
    private String genome;

    public Item(Double obj1,Double obj2, String genome) {
        this.obj1 = obj1;
        this.obj2 = obj2;
        this.genome = genome;
    }

    public Double getObj1() {
        return obj1;
    }

    public void setObj1(Double obj1) {
        this.obj1 = obj1;
    }

    public Double getObj2() {
        return obj2;
    }

    public void setObj2(Double obj2) {
        this.obj2 = obj2;
    }

    public String getGenome() {
        return genome;
    }

    public void setGenome(String genotipe) {
        this.genome = genotipe;
    }

    public String getAttributes(){
        StringTokenizer tokenizer = new StringTokenizer(genome, " ");
        String atts = "";
        String token;
        int index = 1;

        while(tokenizer.hasMoreTokens()){

            token = tokenizer.nextToken();
            if (Integer.valueOf(token) == 1){
                atts += index +",";
            }
            index++;
        }
        return atts.substring(0, atts.length()-1);
    }
}
