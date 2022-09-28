import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ProcessorSimulator extends Simulator
    {
    private static int cycle = 0;
    private static int num_Processor = 0;
    private static int num_Task = 0;
    private static final List<Processor> processors = new ArrayList<>();
    private static final List<Task> createdTask = new ArrayList<>();
    private static final List<Task> waitingTask = new ArrayList<>();
    private static final List<Task> finishedTasks = new ArrayList<>();
    private static final StringBuilder outputString = new StringBuilder();

    protected void input()
        {
        try
            {
            File myObj = new File("input.txt");
            Scanner myReader = new Scanner(myObj);
            int i = 1;
            while (myReader.hasNextLine())
                {
                String data = myReader.nextLine();
                if (i == 1)
                    {
                    num_Processor = Integer.parseInt(data);
                    for (int j = 0; j < num_Processor; j++)
                        {
                        Processor pro = new Processor();
                        processors.add(pro);
                        }
                    }
                else if (i == 2)
                    {
                    num_Task = Integer.parseInt(data);
                    }
                else
                    {
                    String[] numbers = data.split(" ");
                    boolean priority = numbers[2].equals("high");
                    Task t = new Task(Integer.parseInt(numbers[0]), Integer.parseInt(numbers[1]), priority);
                    createdTask.add(t);
                    }
                i++;
                }
            myReader.close();
            Scheduler.sortCreation_time();
            num_Task = createdTask.size();
            System.out.println("the file reading successful !! ");
            }
        catch (FileNotFoundException e)
            {
            System.out.println("The system cannot find the file specified !!");
            //e.printStackTrace();
            }

        }

    protected void output()
        {
        try
            {
            FileWriter myWriter = new FileWriter("output.txt");
            myWriter.write("The Total Simulation Time : " + finishedTasks.get(finishedTasks.size() - 1).getCompletion_time() + " .\n\n");
            myWriter.append("Completion Times For All Tasks : " + "\n\n");
            for (Task finishedTask : finishedTasks)
                {
                myWriter.append(finishedTask.toString()).append("\n");
                }
            myWriter.append("Events For All Cycles : " + "\n\n");
            myWriter.append(String.valueOf(outputString));
            myWriter.close();
            }
        catch (IOException e)
            {
            System.out.println("The system cannot find the file specified !!");
            //e.printStackTrace();

            }

        }

    protected void run()
        {
        input();
        Scheduler.getObj().start();
        output();
        }

    private static class Scheduler
        {

        private static Scheduler obj;

        private Scheduler()
            {
            }
        public static Scheduler getObj()
            {
            if (obj == null)
                {
                obj = new Scheduler();
                }
            return obj;
            }

        public void start()
            {
            while (num_Task != finishedTasks.size())
                {
                checkTaskReady();
                assignTasks();
                saveCycle();
                executeAllProcessors();
                cycle++;
                }
            }

        private void checkTaskReady()
            {
            for (int i = 0; i < createdTask.size(); i++)
                {
                if (createdTask.get(i).getCreation_time() == cycle)
                    {
                    waitingTask.add(createdTask.get(i));
                    createdTask.remove(i);
                    i--;
                    }
                else
                    {
                    break;
                    }
                }
            }

        private void assignTasks()
            {
            for (int i = 0; i < waitingTask.size(); i++)
                {
                i -= orderTask(waitingTask.get(i));
                }
            }

        private int orderTask(Task task)
            {
            /*
             * return 1 if the task was assigned to a processor
             * return 0 otherwise
             * */
            if (isAnyProcessIdle())
                {
                Objects.requireNonNull(getProcessIdle()).setTask(task);
                waitingTask.remove(task);
                return 1;
                }
            else
                {
                if (task.isPriorityHigh())
                    {
                    if (isAnyProcessLow())
                        {
                        waitingTask.add(0, Objects.requireNonNull(getProcessLow()).getTask());
                        getProcessLow().setTask(task);
                        waitingTask.remove(task);
                        return 1;
                        }
                    else if (isAnyTaskLessReqTime(task))
                        {
                        waitingTask.add(0, Objects.requireNonNull(getAnyTaskLessReqTime(task)).getTask());
                        Objects.requireNonNull(getAnyTaskLessReqTime(task)).setTask(task);
                        waitingTask.remove(task);
                        return 1;
                        }
                    }
                }
            return 0;
            }

        private Processor getAnyTaskLessReqTime(Task task)
            {
            for (Processor processor : processors)
                {
                if (processor.task.getRequested_time() < task.requested_time)
                    {
                    return processor;
                    }
                }
            return null;
            }

        private boolean isAnyTaskLessReqTime(Task task)
            {
            for (Processor processor : processors)
                {
                if (processor.task.getRequested_time() < task.requested_time)
                    {
                    return true;
                    }
                }
            return false;
            }

        private boolean isAnyProcessIdle()
            {
            for (Processor processor : processors)
                if (Objects.equals(processor.getState(), "idle"))
                    {
                    return true;
                    }
            return false;
            }

        private boolean isAnyProcessLow()
            {
            for (Processor processor : processors)
                {
                if (!processor.task.getPriority())
                    {
                    return true;
                    }
                }
            return false;
            }

        private Processor getProcessIdle()
            {
            for (Processor processor : processors)
                {
                if (processor.getState().equals("idle"))
                    {
                    return processor;
                    }
                }
            return null;
            }

        private Processor getProcessLow()
            {
            for (Processor processor : processors)
                {
                if (!processor.task.getPriority())
                    {
                    return processor;
                    }
                }
            return null;
            }

        private static void executeAllProcessors()
            {
            for (Processor processor : processors)
                {
                if (processor.state.equals("busy"))
                    {
                    processor.execute();
                    }
                }
            }

        private static void sortCreation_time()
            {
            createdTask.sort(Comparator.comparingInt(o -> o.creation_time));
            }

        private void saveCycle()
            {
            outputString.append("Cycle : " + cycle + "\n\n");
            for (int i = 0; i < num_Processor; i++)
                {
                if (processors.get(i).task != null)
                    {
                    outputString.append("Processor ID : " + processors.get(i).getId() + " || Processor State : " + processors.get(i).getState() + " || Assigned Task ID : " + processors.get(i).task.getId() + " || Requested_Time : " + processors.get(i).task.getRequested_time() + " . \n\n");
                    }
                else
                    {
                    outputString.append("Processor ID : " + processors.get(i).getId() + " || Processor State : " + processors.get(i).getState() + " || Assigned Task ID : " + processors.get(i).task + " || Requested_Time : " + "null" + " . \n\n");
                    }
                }
            outputString.append("\n");
            }
        }

    private static class Processor
        {
        private static int id_Counter = 1;
        private final int id;
        private String state;
        private Task task;

        private Processor()
            {
            id = id_Counter;
            id_Counter++;
            task = null;
            state = "idle";
            }

        private Task getTask()
            {
            return task;
            }

        private String getState()
            {
            return state;
            }

        private int getId()
            {
            return id;
            }

        private void setTask(Task t)
            {
            task = t;
            state = "busy";
            }

        private void setState(String state)
            {
            this.state = state;
            }

        private void execute()
            {
            task.setRequested_time(task.getRequested_time() - 1);
            checkIfFinished();
            }

        private void checkIfFinished()
            {
            if (task.getRequested_time() == 0)
                {
                task.setCompletion_time(cycle);
                task.setState("completed");
                finishedTasks.add(task);
                //remove from queue
                task = null;
                setState("idle");
                }
            }

        }

    private static class Task
        {
        private final int creation_time;
        private final int first_requested_time;
        private final int id;
        private final boolean priority;
        private int requested_time;
        private int Completion_time;
        private static int id_Counter = 1;
        private String state = "waiting";

        private Task(int creation_time, int requested_time, boolean priority)
            {
            id = id_Counter;
            id_Counter++;
            this.creation_time = creation_time;
            this.requested_time = requested_time;
            this.priority = priority;
            first_requested_time = this.requested_time;
            }

        private int getId()
            {
            return id;
            }

        private int getCreation_time()
            {
            return creation_time;
            }

        private int getRequested_time()
            {
            return requested_time;
            }

        private boolean getPriority()
            {
            return priority;
            }

        private boolean isPriorityHigh()
            {
            return getPriority();
            }

        private String getState()
            {
            return state;
            }

        private int getCompletion_time()
            {
            return Completion_time;
            }

        private void setRequested_time(int requested_time)
            {
            this.requested_time = requested_time;
            }

        private void setState(String state)
            {
            this.state = state;
            }

        private void setCompletion_time(int completion_time)
            {
            Completion_time = completion_time;
            }

        @Override
        public String toString()
            {
            return " Task_ID= " + id + " || Creation_Time= " + creation_time + " || Requested_Time= " + first_requested_time + " || Completion_Time= " + Completion_time + " || Priority=" + ((priority) ? "High" : "Low") + "\n";
            }
        }

    }

