package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.PublishResultsEvent;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing information on a conference.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class ConferenceInformation {

    private String name;
    private int date;
    private AtomicInteger timeTick;
    private LinkedList<PublishResultsEvent> results;               //the papers to be published via broadcast at date time

    public ConferenceInformation(String _name, int _date){
        name = _name;
        date = _date;
        results = new LinkedList<>();
        timeTick = new AtomicInteger(1);
    }

    public boolean updateTimeTick() {
        int val;
        do {
            val = timeTick.get();
        }
        while (!timeTick.compareAndSet(val, val + 1));
        if (timeTick.get() == date) {
            return true;
        }
        else
            return false;
    }

    public int getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public void addEvent(PublishResultsEvent e){
        results.push(e);
    }

    public LinkedList<Model> turnIntoBC(){
        LinkedList<Model> models = new LinkedList<>();
        for (PublishResultsEvent e : results){
            models.push(e.getModel());
        }
        return (models);
    }

    public void setTimeTickToNull(){
        timeTick = null;
    }
}
