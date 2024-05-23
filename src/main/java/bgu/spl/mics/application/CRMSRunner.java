package bgu.spl.mics.application;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.*;

import java.io.*;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/** This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {

    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
        LinkedList<MicroService> microServices = new LinkedList<>();
        LinkedList<MicroService> studentServices = new LinkedList<>();
        LinkedList<MicroService> gpuServices = new LinkedList<>();
        LinkedList<MicroService> cpuServices = new LinkedList<>();
        LinkedList<MicroService> conServices = new LinkedList<>();
        LinkedList<Student> students = new LinkedList<>();
        LinkedList<ConferenceInformation> conInfos = new LinkedList<>();


	    String path = args[0];
        File input = new File(path);
        JsonElement fileElement = JsonParser.parseReader(new FileReader(input));
        JsonObject fileObject = fileElement.getAsJsonObject();

        //processing time service:
        int duration = fileObject.get("Duration").getAsInt();
        int speed = fileObject.get("TickTime").getAsInt();
        TimeService timeService = new TimeService("timeService");
        timeService.setDuration(duration);
        timeService.setSpeed(speed);


        //Processing Students:
        JsonArray jsonArrayStudents = fileObject.get("Students").getAsJsonArray();
        for (JsonElement student : jsonArrayStudents) {
            JsonObject studentObject = student.getAsJsonObject();
            LinkedList<Model> models = new LinkedList<>();

            String studentName = studentObject.get("name").getAsString();
            String studentDepartment = studentObject.get("department").getAsString();
            String studentStatus = studentObject.get("status").getAsString();
            JsonArray jsonArrayModels = studentObject.get("models").getAsJsonArray();
            for (JsonElement jModel : jsonArrayModels) {
                JsonObject modelObject = jModel.getAsJsonObject();

                String modelName = modelObject.get("name").getAsString();
                String dataType = modelObject.get("type").getAsString();
                int dataSize = modelObject.get("size").getAsInt();

                Data tempData = new Data(dataType, dataSize);
                Model tempModel = new Model(modelName, tempData);
                models.add(tempModel);
            }

            Student tempStudent = new Student(studentName, studentDepartment, studentStatus, models);
            StudentService tempStudentService = new StudentService(studentName, tempStudent);
            microServices.add(tempStudentService);
            studentServices.add(tempStudentService);
            students.add(tempStudent);

        }

        //Processing ConferenceInformation:
        JsonArray jsonArrayConInfo = fileObject.get("Conferences").getAsJsonArray();
        for (JsonElement jConInfo : jsonArrayConInfo) {
            JsonObject conInfoObject = jConInfo.getAsJsonObject();
            String conName = conInfoObject.get("name").getAsString();
            int conDate = conInfoObject.get("date").getAsInt();

            ConferenceInformation tempConInfo = new ConferenceInformation(conName, conDate);
            ConferenceService tempConService = new ConferenceService(conName, tempConInfo);
            microServices.push(tempConService);
            conServices.push(tempConService);
            conInfos.add(tempConInfo);
        }

        //Processing CPUs:
        JsonArray jsonArrayCPUs = fileObject.get("CPUS").getAsJsonArray();
        int count = 0;
        for (JsonElement jCPU : jsonArrayCPUs) {
            int cores = jCPU.getAsInt();
            CPU tempCPU = new CPU(Integer.toString(count), cores);
            CPUService tempCPUService = new CPUService(tempCPU, Integer.toString(count));
            microServices.push(tempCPUService);
            cpuServices.push(tempCPUService);

            Cluster.getInstance().addCPU(tempCPU);
            count++;
        }

        //Processing GPUs:
        JsonArray jsonArrayGPUs = fileObject.get("GPUS").getAsJsonArray();
        count = 0;
        for (JsonElement jGPU : jsonArrayGPUs) {
            String gpuType = jGPU.getAsString();
            GPU tempGPU = new GPU(gpuType,Integer.toString(count));
            GPUService tempGPUService = new GPUService(Integer.toString(count), tempGPU);
            Cluster.getInstance().addGPU(tempGPU);
            microServices.push(tempGPUService);
            gpuServices.add(tempGPUService);

            count++;
        }
        microServices.add(timeService);

        ExecutorService executorService = Executors.newFixedThreadPool(microServices.size() );
        for (MicroService mc : microServices)
            executorService.execute(mc);


        Thread.sleep(duration + 2000);
	    executorService.execute(timeService);
        executorService.shutdown();


        for (Student s : students) {
            s.setModelsNull();
            if (!s.getPublications().isEmpty())
                for (Model m : s.getPublications())
                    m.setStudentNull();
        }
        for (ConferenceInformation c : conInfos)
            c.setTimeTickToNull();

        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Writer writer = new FileWriter("../output.json");
            gson.toJson(new Output(conInfos, students, Cluster.getInstance().getCpuTimeTicks(), Cluster.getInstance().getGpuTimeTicks(), Cluster.getInstance().getDbProcessed()), writer);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.exit(1);

    }


        public static class Output {
            private LinkedList<Student> students;
            private LinkedList<ConferenceInformation> conferenceInformations;
            private int cpuTime;
            private int gpuTime;
            private int processedDB;

            public Output(LinkedList _conferenceInformations, LinkedList _students, int cpu, int gpu, int db) {
                students = _students;
                conferenceInformations = _conferenceInformations;
                cpuTime = cpu;
                gpuTime = gpu;
                processedDB = db;
            }
        }

    }





