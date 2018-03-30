package scheduler;

import java.io.InvalidObjectException;
import java.util.ArrayList;

/**
 * TungHo Lin
 * txl429
 * PA7
 * Class Assignments
 */


public class Assignments {

    private final String assignmentID;

    private final int duration;

    private ArrayList<Dependency> dependencies;

    public Assignments(String assignmentID, int duration) throws IllegalArgumentException {
        this.assignmentID = assignmentID;
        if(duration < 0)
            throw new IllegalArgumentException("Invalid Assignment: duration can not be negative");
        this.duration = duration;
        this.dependencies = new ArrayList<>();
    }

    /**
     * Inner class that will be used to test the private methods
     */
    public class TestHook {
        public void checkCircularRequirement(Dependency d1, Dependency d2) throws RelationshipException {
            Assignments.checkCircularRequirement(d1, d2);
        }

        public void checkContradictingRequirement(Dependency d1, Dependency d2) throws RelationshipException {
            Assignments.checkContradictingRequirement(d1, d2);
        }

        public boolean isEndBeginBeginEnd(Dependency d1, Dependency d2) {
            return Assignments.isEndBeginBeginEnd(d1, d2);
        }
    }

    //autogenerated getter: no testing required
    public String getAssignmentID() {
        return this.assignmentID;
    }

    //autogenerated getter: no testing required
    public int getDuration() {
        return this.duration;
    }

    //autogenerated getter: no testing required
    public ArrayList<Dependency> getDependencies() {
        return dependencies;
    }

    public void addDependencies(Dependency d) throws InvalidObjectException {
        //have to check both
        if(d.getChild() == null | d.getParent() == null)
            throw new InvalidObjectException("Invalid Dependency");
        dependencies.add(d);
    }

    public void checkRelationshipError() throws RelationshipException {
        //if there is no dependency relationships or only one relationship
        if(dependencies.size() == 1)
            return;
        for (int i=0; i<dependencies.size(); i++) {
            Dependency current = dependencies.get(i);
            for (int j=i+1; j<dependencies.size(); j++) {
                Dependency other = dependencies.get(j);
                checkCircularRequirement(current, other);
                checkContradictingRequirement(current, other);
            }
        }
    }


    /* Circular Requirement: if 2 Dependency has the opposite parent and child */
    public static void checkCircularRequirement(Dependency d1, Dependency d2) throws RelationshipException {
        if(d1.getChild().getAssignmentID() == d2.getParent().getAssignmentID() &&
                d1.getParent().getAssignmentID() == d2.getChild().getAssignmentID())
            throw new RelationshipException(RelationshipException.ErrorCode.CIRCULAR_REQUIREMENT);
    }

    /* Contradicting Requirement: if 2 Dependency has the same parent and child except for End_Begin and Begin_End case */
    public static void checkContradictingRequirement(Dependency d1, Dependency d2) throws RelationshipException {
        //only true if they have same parent and child but not the special case
        if(d1.sameChildParent(d2) && !isEndBeginBeginEnd(d1, d2))
            throw new RelationshipException(RelationshipException.ErrorCode.CONTRADICTING_REQUIREMENT);
    }

    /* special case for contradicting requirement such that this case with the same parent and child is not contradicting */
    private static boolean isEndBeginBeginEnd(Dependency d1, Dependency d2) {
        if(d1.getRelationship() == Dependency.Relationship.End_Begin && d2.getRelationship() == Dependency.Relationship.Begin_End)
            return true;
        return d1.getRelationship() == Dependency.Relationship.Begin_End && d2.getRelationship() == Dependency.Relationship.End_Begin;
    }
}
