package bgu.spl.mics.application.objects;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Data {

    /**
     * Enum representing the Data type.
     */
    enum Type {Images, Text, Tabular}

    private Type type;
    private AtomicInteger processed;            //
    private int size;

    public Data(String _type, int _size){
        if (_type.equals("Images") || _type.equals("images")) type = Type.Images;
        else if (_type.equals("Text")) type = Type.Text;
        else type = Type.Tabular;
        size = _size;
        processed = new AtomicInteger(0);
    }

    public Type getType(){
        return type;
    }

    public int getProcessed(){
        return processed.get();
    }

    public int getSize(){ return size; }

    public void increaseProcessed(){
        int val;
        do { val = processed.get(); }
        while (!processed.compareAndSet(val, val + 1000));
    }

}
