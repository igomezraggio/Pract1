package uy.fing;

/**
 * Created by igomez on 8/6/2016.
 */
public class Item {

    private Double obj1;
    private Double obj2;

    public Item(Double obj1,Double obj2) {
        this.obj1 = obj1;
        this.obj2 = obj2;
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
}
