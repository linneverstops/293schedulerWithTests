package scheduler;
import java.io.InvalidObjectException;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * TungHo Lin
 * txl429
 * PA7
 * Class Scheduler
 */

public class Scheduler {

    public ArrayList<ArrayList<Assignments>> schedule;

    public Scheduler() {
        ArrayList<Assignments> innerContainer = new ArrayList<Assignments>();
        schedule = new ArrayList<>();
        schedule.add(innerContainer);
    }

    /**
     * Inner class that will be used to test the private methods
     */
    public class TestHook {
        public int getMaxDuration(ArrayList<Assignments> list) throws InvalidObjectException {
            int getMaxDuration_test = Scheduler.this.getMaxDuration(list);
            return getMaxDuration_test;
        }

        public ArrayList<Assignments> insertRootAssignments(ArrayList<ArrayList<Assignments>> schedule, ArrayList<Assignments> list) throws InvalidObjectException {
            Scheduler.this.schedule = schedule;
            ArrayList<Assignments> insertRootAssignments_test = Scheduler.this.insertRootAssignments(list);
            return insertRootAssignments_test;
        }

        public void checkError(ArrayList<Assignments> list) throws RelationshipException {
            Scheduler.this.checkError(list);
        }

        public void buildSchedule(ArrayList<ArrayList<Assignments>> schedule, ArrayList<Assignments> list) throws InvalidObjectException{
            Scheduler.this.schedule = schedule;
            Scheduler.this.buildSchedule(list);
        }

        public void insertAssignment(ArrayList<ArrayList<Assignments>> schedule, Dependency d) throws InvalidObjectException {
            Scheduler.this.schedule = schedule;
            Scheduler.this.insertAssignment(d);
        }

        public void insertChildByOrder(ArrayList<ArrayList<Assignments>> schedule, Dependency d) throws InvalidObjectException {
            Scheduler.this.schedule = schedule;
            Scheduler.this.insertChildByOrder(d);
        }

        public void addEndBeginChild(ArrayList<ArrayList<Assignments>> schedule, int parentLocation, Assignments child) throws InvalidObjectException {
            Scheduler.this.schedule = schedule;
            Scheduler.this.addEndBeginChild(parentLocation, child);
        }

        public void addBeginBeginEndEndChild(ArrayList<ArrayList<Assignments>> schedule, int parentLocation, Assignments child) throws InvalidObjectException {
            Scheduler.this.schedule = schedule;
            Scheduler.this.addBeginBeginEndEndChild(parentLocation, child);
        }

        public void addBeginEndChild(ArrayList<ArrayList<Assignments>> schedule, int parentLocation, Assignments child) throws InvalidObjectException {
            Scheduler.this.schedule = schedule;
            Scheduler.this.addBeginEndChild(parentLocation, child);
        }

        public int find(ArrayList<ArrayList<Assignments>> schedule, Assignments assignment) {
            Scheduler.this.schedule = schedule;
            int find_test = Scheduler.this.find(assignment);
            return find_test;
        }
    }

    public int deliveryTime(ArrayList<Assignments> list) throws RelationshipException, InvalidObjectException {
        int time = 0;
        ArrayList<Assignments> rootlessList = insertRootAssignments(list);
        checkError(rootlessList);
        buildSchedule(rootlessList);
        for(ArrayList<Assignments> container : schedule) {
            time += getMaxDuration(container);
        }
        return time;
    }

    private int getMaxDuration(ArrayList<Assignments> list) throws InvalidObjectException {
        int maxDuration = 0;
        for(Assignments a : list) {
            if (a.getDuration() > maxDuration)
                maxDuration = a.getDuration();
        }
        return maxDuration;
    }

    private ArrayList<Assignments> insertRootAssignments(ArrayList<Assignments> list) throws InvalidObjectException {
        ArrayList<Assignments> returnList = new ArrayList<>();
        for(Assignments a : list) {
            if(a.getDependencies().size() == 0)
                schedule.get(0).add(a);
            else
                returnList.add(a);
        }
        return returnList;
    }

    private void checkError(ArrayList<Assignments> list) throws RelationshipException {
        for(Assignments a : list) {
            a.checkRelationshipError();
        }
    }

    private void buildSchedule(ArrayList<Assignments> list) throws InvalidObjectException {
        for(Assignments a : list) {
            ArrayList<Dependency> dependencies = a.getDependencies();
            for(Dependency d : dependencies) {
                insertAssignment(d);
            }
        }
    }

    private void insertAssignment(Dependency d) throws InvalidObjectException {
        Assignments child = d.getChild();
        Assignments parent = d.getParent();
        //if the child Assignment does not already exist in schedule
        if(find(child) == -1) {
            //if the parent Assignment does not already exist in schedule
            if(find(parent) == -1)
                //insert the parent Assignment at the first Container in schedule
                schedule.get(0).add(parent);
            insertChildByOrder(d);
        }
        //if both child and parent Assignments already exist(that's why I used &)
        else if(find(child) != -1 & find(parent) != -1) {
            //do nothing because either there is a dependency same as this one that's already been added,
            //or this is a ContradictingReq(same child/parent) or CircularReq(opposite parent/child)
        }
    }

    //precondition: parent is already inserted/exists in schedule
    private void insertChildByOrder(Dependency d) throws InvalidObjectException {
        Dependency.Relationship r = d.getRelationship();
        if(r == null)
            throw new InvalidObjectException("No relationship is present, cannot insert child by order");
        Assignments parent = d.getParent();
        Assignments child = d.getChild();
        int parentLocation = find(parent);
        if (r == Dependency.Relationship.End_Begin)
            addEndBeginChild(parentLocation, child);
        else if(r == Dependency.Relationship.Begin_Begin || r == Dependency.Relationship.End_End)
            addBeginBeginEndEndChild(parentLocation, child);
        else //no other possible case except Begin_End
            addBeginEndChild(parentLocation, child);
    }

    private void addEndBeginChild(int parentLocation, Assignments child) throws InvalidObjectException {
        //parent not in acceptable range
        if(parentLocation >= schedule.size() || parentLocation < 0)
            throw new InvalidObjectException("Parent Location is out of range, cannot add End-Begin child.");
        //End_begin: child after parent
        int childLocation = parentLocation + 1;
        //if schedule is not long enough
        if(schedule.size() <= childLocation)
            //add a new container to the end of schedule
            schedule.add(new ArrayList<>());
        schedule.get(childLocation).add(child);
    }

    private void addBeginBeginEndEndChild(int parentLocation, Assignments child) throws InvalidObjectException {
        //parent not in acceptable range
        if(parentLocation >= schedule.size() || parentLocation < 0)
            throw new InvalidObjectException("Parent Location is out of range, cannot add Begin-Begin or End-End child.");
        schedule.get(parentLocation).add(child);
    }

    private void addBeginEndChild(int parentLocation, Assignments child) throws InvalidObjectException {
        //throw InvalidObjectException if parent Location is not in the acceptable range
        if(parentLocation >= schedule.size() || parentLocation < 0)
            throw new InvalidObjectException("Parent Location is out of range, cannot add Begin-End child.");
        int childLocation = parentLocation - 1;
        //if parent is at the 1st Container
        if(childLocation < 0) {
            //add a new Container at the front and change the childLocation
            schedule.add(0, new ArrayList<>());
            childLocation++;
        }
        schedule.get(childLocation).add(child);
    }

    private int find(Assignments assignment) {
        for(ArrayList<Assignments> a : schedule) {
            for (Assignments current : a) {
                if (assignment.equals(current))
                    //return the index of the Container containing the assignment
                    return schedule.indexOf(a);
            }
        }
        //if there is no match, return -1
        return -1;
    }

}
