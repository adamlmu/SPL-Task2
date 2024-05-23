package bgu.spl.mics.application.objects;

import java.util.LinkedList;

/**
 * Passive object representing single student.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Student {

    /**
     * Enum representing the Degree the student is studying for.
     */
    enum Degree {MSc, PhD}

    private String name;
    private String department;
    private Degree degree;
    private LinkedList<Model> publications;
    private int papersRead;
    private LinkedList<Model> models;

    public Student(String _name, String _department, String _degree, LinkedList<Model> _models){
        name = _name;
        department = _department;
        if (_degree.equals("MSc")) degree = Degree.MSc;
        else degree = Degree.PhD;
        publications = new LinkedList<>();
        papersRead = 0;
        models = _models;
        for (Model m : models){
            m.setStudent(this);
        }
    }

    public void setModelsNull(){
        models = null;
    }

    public String getName(){ return name;  }

    public String getDepartment(){ return department; }

    public Degree getDegree(){ return degree; }

    public LinkedList<Model> getPublications(){ return publications; }

    public int getPapersRead(){ return papersRead; }

    public Model popModel(){
        if (!models.isEmpty())
            return models.pop();
        else return null;
    }

    public LinkedList<Model> getModels() {
        return models;
    }

    public void addPublications(Model publication) {
        publications.add(publication);
    }

    public void setPapersRead(int papersRead) {
        this.papersRead = papersRead;
    }

    public void addModel(Model _model){ models.push(_model); }

}
