package bgu.spl.mics.application.objects;

/**
 * Passive object representing a Deep Learning model.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Model {

    public enum Status {PreTrained, Training, Trained, Tested}
    public enum TestResult {Good, Bad}

    private String name;
    private Data data;
    private Student student;
    private Status status;
    private TestResult result;

    public Model(String _name, Data _data){
        name = _name;
        data = _data;
        status = Status.PreTrained;
        result = null;
    }

    public void setStudent(Student _student) {
        student = _student;
    }

    public String getName() {return name;}

    public Student getStudent() {return student;}

    public Status getStatus() {return status;}

    public TestResult getResult() {return result;}

    public Data getData() {return data;}

    public void setResult(TestResult _result) {
        result = _result;
    }

    public void setStatus(Status _status) {
        status = _status;
    }

    public void setStudentNull(){
        student= null;
    }

}
