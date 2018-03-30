package scheduler;

import org.junit.Before;
import org.junit.Test;
import java.io.InvalidObjectException;
import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class SchedulerTest {

    private ArrayList<ArrayList<Assignments>> schedule;

    private ArrayList<Assignments> list;

    private Assignments a, b, c, d, e, f, g;

    private Dependency d1, d2, d3, d4, d5, d6, d7, d8, d1a, d3a, d1b, d1c, d2b;

    private Scheduler sampleObj;

    private Scheduler.TestHook sampleTestObj;

    @Before
    public void setUp() throws Exception {
        schedule = new ArrayList<>();
        schedule.add(new ArrayList<>());
        sampleObj = new Scheduler();
        sampleTestObj = sampleObj.new TestHook();
        a = new Assignments("A", 3);
        b = new Assignments("B", 0);
        c = new Assignments("C", 8);
        d = new Assignments("D", 10000);
        e = new Assignments("E", 6);
        f = new Assignments("F", 5);
        g = new Assignments("G", 10);
        d1 = new Dependency(a, Dependency.Relationship.End_Begin, b);  //d1 and d2b are CircularReq
        d1a = new Dependency(a, Dependency.Relationship.Begin_End, e);
        d1b = new Dependency(a, Dependency.Relationship.End_Begin, e); //d1a and d1b are not ContradictingReq(BeginEndEndBegin)
        d1c = new Dependency(a, Dependency.Relationship.End_End, e); //d1a and d1c are ContradictingReq
        d2 = new Dependency(b, Dependency.Relationship.End_Begin, c);
        d2b = new Dependency(b, Dependency.Relationship.Begin_End, a);
        d3 = new Dependency(c, Dependency.Relationship.End_Begin, e);
        d3a = new Dependency(c, Dependency.Relationship.Begin_End, f);
        d4 = new Dependency(d, Dependency.Relationship.End_Begin, a);
        d5 = new Dependency(e, Dependency.Relationship.End_Begin, f);
        d6 = new Dependency(f, Dependency.Relationship.End_Begin, g);
        d7 = new Dependency(g, Dependency.Relationship.Begin_End, a);
        d8 = new Dependency(b, Dependency.Relationship.Begin_Begin, e);
        list = new ArrayList<>(); //empty list
    }

    //structured basis
    @Test
    public void test_deliverTime_normalConditions() throws InvalidObjectException, RelationshipException {
        Dependency dd1 = new Dependency(a, Dependency.Relationship.End_End, b);
        Dependency dd2 = new Dependency(c, Dependency.Relationship.End_Begin, a);
        Dependency dd3 = new Dependency(d, Dependency.Relationship.Begin_Begin, c);
        Dependency dd4 = new Dependency(e, Dependency.Relationship.Begin_End, a);
        a.addDependencies(dd1);
        //b has no dependencies
        c.addDependencies(dd2);
        d.addDependencies(dd3);
        e.addDependencies(dd4);
        ArrayList<Assignments> alist = new ArrayList<>();
        alist.add(a);
        alist.add(b);
        alist.add(c);
        alist.add(d);
        alist.add(e);
        assertEquals(sampleObj.deliveryTime(alist), 10009);
        /*
        Operation Rundown:
        b is added to Container 0 because it is a root Assignment
        a End-End b so b is added in the same Container as a
        c End-Begin a so c is after a; c is added in Container 1
        d Begin-Begin c so d is added in the same Container as c
        e Begin-End a so e is added before a
        Container 0: e (Max Duration = 6)
        Container 1: a, b (Max Duration = 3)
        Container 2: c, d (Max Duration = 10000)
        Therefore, deliver time = 10009 s
        */
    }


    //structured basis: nominal, good data
    @Test
    public void test_getMaxDuration_nominal() throws InvalidObjectException {
        list.add(a);
        assertEquals(sampleTestObj.getMaxDuration(list), 3);
    }

    //bad data/compound boundaries: passed in an empty list
    @Test
    public void test_getMaxDuration_badDataEmptyList() throws InvalidObjectException {
        assertEquals(sampleTestObj.getMaxDuration(list), 0);
        //empty list will return 0 duration
    }

    //bad data: passed in null obj
    @Test(expected = NullPointerException.class)
    public void test_getMaxDuration_badDataNullObj() throws InvalidObjectException {
        sampleTestObj.getMaxDuration(null);
    }

    //structured basis: if one duration is not larger than the other duration, it will be ignored
    //compound boundaries: large number
    @Test
    public void test_getMaxDuration_nonZeroValues() throws InvalidObjectException {
        list.add(a);
        list.add(d);
        assertEquals(sampleTestObj.getMaxDuration(list), 10000);
    }

    //boundaries: zero; compound boundaries: all zeroes
    @Test
    public void test_getMaxDuration_zeroValues() throws InvalidObjectException {
        list.add(b);
        list.add(c);
        assertEquals(sampleTestObj.getMaxDuration(list), 8);
    }

    //structured basis, data flow: adding 2 same Assignments to list
    @Test
    public void test_getMaxDuration_sameAssignments() throws InvalidObjectException {
        list.add(a);
        list.add(a);
        assertEquals(sampleTestObj.getMaxDuration(list), 3);
    }

    //structured basis, good data
    //boundaries: 1 (1 assignment is added)
    @Test
    public void test_insertRootAssignments_oneRootAssignment() throws InvalidObjectException {
        list.add(a);
        sampleTestObj.insertRootAssignments(schedule, list);
        assertEquals(schedule.get(0).get(0), a);
    }

    //structured basis: if all assignments in the list are not root(root means that an assignment that has no dependency)
    //boundaries: 0 (no assignments is added)
    //compound boundaries: 0 (minimum assignments is added)
    @Test
    public void test_insertRootAssignments_noRootAssignment() throws InvalidObjectException{
        f.addDependencies(d6);
        g.addDependencies(d7);
        list.add(f);
        list.add(g);
        sampleTestObj.insertRootAssignments(schedule, list);
        assertTrue(schedule.get(0).isEmpty());
    }

    //structured basis: if all the assignments have no dependencies (if branch is true)
    //compound boundaries (multiple root assignments is added)
    @Test
    public void test_insertRootAssignments_multipleRootAssignment() throws InvalidObjectException {
        list.add(a);
        list.add(b);
        list.add(c);
        list.add(d);
        list.add(e);
        list.add(f);
        list.add(g);
        sampleTestObj.insertRootAssignments(schedule, list);
        assertEquals(schedule.get(0).get(0), a);
        assertEquals(schedule.get(0).get(1), b);
        assertEquals(schedule.get(0).get(2), c);
        assertEquals(schedule.get(0).get(3), d);
        assertEquals(schedule.get(0).get(4), e);
        assertEquals(schedule.get(0).get(5), f);
        assertEquals(schedule.get(0).get(6), g);
    }

    //structured basis: if all assignments are non-root(if branch is false)
    //compound boundaries (multiple non-root assignments)
    @Test
    public void test_insertRootAssignments_multipleNonRootAssignment() throws InvalidObjectException {
        a.addDependencies(d1);
        b.addDependencies(d2);
        c.addDependencies(d3);
        d.addDependencies(d4);
        e.addDependencies(d5);
        f.addDependencies(d6);
        g.addDependencies(d7);
        list.add(a);
        list.add(b);
        list.add(c);
        list.add(d);
        list.add(e);
        list.add(f);
        list.add(g);
        sampleTestObj.insertRootAssignments(schedule, list);
        assertTrue(schedule.get(0).isEmpty());
        //nothing gets added because none of the assignments are root
    }

    //bad data/compound boundaries: passed in an empty list
    @Test
    public void test_insertRootAssignments_badDataemptyList() throws InvalidObjectException {
        assertTrue(sampleTestObj.insertRootAssignments(schedule, list).isEmpty());
        //passing in an empty list will return an empty list
    }

    //bad data: passed in a null obj
    @Test(expected = NullPointerException.class)
    public void test_insertRootAssignments_badDatanullObj() throws InvalidObjectException {
        sampleTestObj.insertRootAssignments(schedule, null);
    }

    //structured basis: entered the for loop
    //good data
    //boundaries: 1(only assignment in the list)
    @Test
    public void test_checkError_nominal() throws InvalidObjectException, RelationshipException {
        a.addDependencies(d1);
        list.add(a);
        sampleTestObj.checkError(list);
        //because there is only one dependency in a, no RelationshipException is thrown because there is no
        //comparison happening
    }

    //boundaries: multiple assignments in the list that do not produce an error
    @Test
    public void test_checkError_multiple() throws InvalidObjectException, RelationshipException{
        a.addDependencies(d1);
        a.addDependencies(d1a);
        a.addDependencies(d1b);
        b.addDependencies(d2);
        c.addDependencies(d3);
        c.addDependencies(d3a);
        list.add(a);
        list.add(b);
        list.add(c);
        sampleTestObj.checkError(list);
        //no circular requirement and no contradicting requirement
    }

    //data flow: multiple assignments that should result in a Circular Req
    //this test does not pass because checkError only check the Dependency list of ONE Assignment but not all Dependency
    //of all Assignments
    @Test (expected = RelationshipException.class)
    public void test_checkError_multipleCircularReq() throws InvalidObjectException, RelationshipException {
        System.err.println("this test does not pass because checkError only check the Dependency list of ONE Assignment " +
                "but not all Dependency of all Assignments. This is something that is not considered in the psuedo code" +
                "so the failure of this test reflects the error");
        a.addDependencies(d1);
        a.addDependencies(d1a);
        a.addDependencies(d1b);
        b.addDependencies(d2);
        b.addDependencies(d2b);
        c.addDependencies(d3);
        c.addDependencies(d3a);
        list.add(a);
        list.add(b);
        list.add(c);
        sampleTestObj.checkError(list);
    }

    //data flow: multiple assignments that should result in a ContradictingReq
    @Test
    public void test_checkError_multipleContradictingReq() throws InvalidObjectException, RelationshipException {
        //Assignments.checkContradictingRequirement(d1a, d1c);
        a.addDependencies(d1a);
        a.addDependencies(d1c);
        b.addDependencies(d2);
        b.addDependencies(d2b);
        c.addDependencies(d3);
        c.addDependencies(d3a);
        list.add(a);
        list.add(b);
        list.add(c);
        try {
            sampleTestObj.checkError(list);
        }
        catch (RelationshipException r) {
            assertThat(r.toString(), is("RelationshipException{errorCode=CONTRADICTING_REQUIREMENT}"));
        }
    }

    //structured basis: does not enter the for loop(no assignments in list)
    //bad data/compound boundaries: pass in an empty list
    @Test
    public void test_checkError_badDataEmptyList() throws RelationshipException {
        sampleTestObj.checkError(list);
    }

    //structured basis
    //good data
    @Test
    public void test_buildSchedule_oneAssignment() throws InvalidObjectException {
        schedule.get(0).add(a); //a at 0
        //d2b = new Dependency(b, Dependency.Relationship.Begin_End, a); \
        //b after a
        b.addDependencies(d2b);
        //d4 = new Dependency(d, Dependency.Relationship.End_Begin, a);
        //d before a
        d.addDependencies(d4);
        ArrayList<Assignments> alist = new ArrayList<>();
        alist.add(b);
        alist.add(d);
        sampleTestObj.buildSchedule(schedule, alist);
        assertEquals(sampleTestObj.find(schedule, d), 2);
        assertEquals(sampleTestObj.find(schedule, a), 1);
        assertEquals(sampleTestObj.find(schedule, b), 0);
    }

    //bad data: empty list
    //data flow
    @Test
    public void test_buildSchedule_badData() throws InvalidObjectException {
        schedule.get(0).add(a); //a at 0
        schedule.add(new ArrayList<>());
        schedule.get(1).add(b);
        ArrayList<Assignments> alist = new ArrayList<>();
        sampleTestObj.buildSchedule(schedule, alist);
        assertEquals(sampleTestObj.find(schedule, a), 0);
        assertEquals(sampleTestObj.find(schedule, b), 1);
        assertEquals(schedule.size(), 2); //3 tests to show that nothing changed
    }

    //structured basis: if neither parent nor child exists
    @Test
    public void test_insertAssignment_neitherExist() throws InvalidObjectException {
        //d1 = new Dependency(a, Dependency.Relationship.End_Begin, b);
        sampleTestObj.insertAssignment(schedule, d1);
        //parent(b) does not exist in schedule so it is placed at index 0,
        //it remains at index 0 because the End-Begin child comes after the parent
        assertEquals(sampleTestObj.find(schedule, b), 0);
        //child comes after parent
        assertEquals(sampleTestObj.find(schedule, a), 1);
    }

    //data flow: if parent exists and child doesn't
    @Test
    public void test_insertAssignment_parentExist() throws InvalidObjectException {
        //d2b = new Dependency(b, Dependency.Relationship.Begin_End, a);
        schedule.get(0).add(c);
        schedule.add(new ArrayList<>());
        schedule.get(1).add(a); //parent a is at index 1
        sampleTestObj.insertAssignment(schedule, d2b);
        //a would still be at 1
        assertEquals(sampleTestObj.find(schedule, a), 1);
        //b would come before a so it would be at 0
        assertEquals(sampleTestObj.find(schedule, b), 0);
    }

    //structured basis: if both parent and child exists
    //the position of the parent and child will remain as it is(as explained in Scheduler)

    //data flow: if child exists and parent doesn't
    //pseudo code and implementation did not include/consider this condition


    //structured basis: if the relationship is End-Begin
    //good data
    @Test
    public void test_insertChildByOrder_EndBeginChild() throws InvalidObjectException {
        //End-Begin: child after parent
        schedule.get(0).add(b); //put the parent(b) at 0
        // d1 = new Dependency(a, Dependency.Relationship.End_Begin, b);
        sampleTestObj.insertChildByOrder(schedule, d1); //a is inserted after b
        assertEquals(sampleTestObj.find(schedule, a), 1); //a should be at 1
    }

    //structured basis: if the relationship is Begin-Begin or End-End
    //good data
    @Test
    public void test_insertChildByOrder_beginBeginEndEndChild() throws InvalidObjectException {
        //Begin-Begin or End-End: child at the same Container with parent
        schedule.get(0).add(e); //e is at 0
        //d1c = new Dependency(a, Dependency.Relationship.End_End, e)
        //d8 = new Dependency(b, Dependency.Relationship.Begin_Begin, e);
        sampleTestObj.insertChildByOrder(schedule, d1c); //a should also be inserted at 0
        assertEquals(sampleTestObj.find(schedule, a), 0);
        sampleTestObj.insertChildByOrder(schedule, d8); //b should also be inserted at 0
        assertEquals(sampleTestObj.find(schedule, b), 0);
    }

    //structured basis: if the relationship is Begin-End
    //good data
    @Test
    public void test_insertChildByOrder_beginEndChild() throws InvalidObjectException {
        //Begin-End: child inserted before parent
        schedule.get(0).add(e); //e is at 0
        //d1a = new Dependency(a, Dependency.Relationship.Begin_End, e);
        sampleTestObj.insertChildByOrder(schedule, d1a); //a should also be inserted at 1
        assertEquals(sampleTestObj.find(schedule, a), 0); //a is at 0
        assertEquals(sampleTestObj.find(schedule, e), 1); //e is pushed to 1
    }

    //bad data
    //structured basis: if the relationship is null
    @Test
    public void test_insertChildByOrder_nullRelationship() {
        //Begin-End: child inserted before parent
        schedule.get(0).add(b); //b is at 0
        Dependency d_null = new Dependency(a, null, b);
        try {
            sampleTestObj.insertChildByOrder(schedule, d_null);
        }
        catch (InvalidObjectException ioe) {
            assertThat(ioe.toString(), is("java.io.InvalidObjectException: No relationship is present, cannot insert child by order"));
        }
    }

    //structured basis: parent index is valid
    //boundaries: parent index = 0
    @Test
    public void test_addEndBeginChild_goodDataZeroIndex() throws InvalidObjectException {
        schedule.get(0).add(a); //if a is at 0
        sampleTestObj.addEndBeginChild(schedule, 0, b); //add b after a
        assertEquals(sampleTestObj.find(schedule, b), 1); //b should be at 1
    }

    //good data
    //boundaries: parent index > 0
    @Test
    public void test_addEndBeginChild_goodDataPositiveIndex() throws InvalidObjectException {
        schedule.add(new ArrayList<>());
        schedule.get(0).add(a); //a is at 0
        schedule.get(1).add(b); //b is at 1
        sampleTestObj.addEndBeginChild(schedule, 1, c); //c should be after b
        assertEquals(sampleTestObj.find(schedule, c), 2); // c should be at 2
    }

    //bad data
    //structured basis: if the parent index is invalid
    //compound boundaries: large out of bounds positive parent index
    @Test
    public void test_addEndBeginChild_largePositiveIndex() {
        schedule.add(new ArrayList<>());
        schedule.get(0).add(a); //a is at 0
        schedule.get(1).add(b); //b is at 1
        try{
            sampleTestObj.addEndBeginChild(schedule, 100000, c);
        }
        catch (InvalidObjectException ioe) {
            assertThat(ioe.toString(), is("java.io.InvalidObjectException: Parent Location is out of range, cannot add End-Begin child."));
        }
    }

    //bad data
    //compound boundaries: large out of bounds negative parent index
    //boundaries: parent index < 0
    @Test
    public void test_addEndBeginChild_largeNegativeIndex() {
        schedule.add(new ArrayList<>());
        schedule.get(0).add(a); //a is at 0
        schedule.get(1).add(b); //b is at 1
        try{
            sampleTestObj.addEndBeginChild(schedule, -100000, c);
        }
        catch (InvalidObjectException ioe) {
            assertThat(ioe.toString(), is("java.io.InvalidObjectException: Parent Location is out of range, cannot add End-Begin child."));
        }
    }

    //structured basis: parent index is valid
    //good data
    //boundaries: parent index at 0
    @Test
    public void test_addBeginBeginEndEndChild_goodDataZeroIndex() throws InvalidObjectException {
        schedule.get(0).add(a); //a at 0
        sampleTestObj.addBeginEndChild(schedule, 0, b); //a pushed to 1, b at 0
        sampleTestObj.addBeginBeginEndEndChild(schedule, 0, c); //c will be added to the Container containing parent
        assertEquals(sampleTestObj.find(schedule, c), 0); //c should be at 0
    }

    //good data
    //boundaries: parent index > 0
    @Test
    public void test_addBeginBeginEndEndChild_goodDataPositiveIndex() throws InvalidObjectException {
        schedule.get(0).add(a); //a at 0
        sampleTestObj.addBeginEndChild(schedule, 0, b); //a pushed to 1, b at 0
        sampleTestObj.addBeginBeginEndEndChild(schedule, 1, c); //c will be added to the Container containing parent
        assertEquals(sampleTestObj.find(schedule, c), 1); //c should be at 1
    }

    //bad data
    //structured basis: parent index is invalid
    //boundaries parent index < 0
    //compound boundaries: large negative parent indexes
    @Test
    public void test_addBeginBeginEndEndChild_badDataLargeNegativeIndex() {
        schedule.get(0).add(a); //a at 0
        try {
            sampleTestObj.addBeginEndChild(schedule, 0, b); //a pushed to 1, b at 0
            sampleTestObj.addBeginBeginEndEndChild(schedule, 10000000, c);
        }
        catch (InvalidObjectException ioe1) {
            assertThat(ioe1.toString(), is("java.io.InvalidObjectException: Parent Location is out of range, cannot add Begin-Begin or End-End child."));
        }
    }

    //compound boundaries: large positive parent indexes
    @Test
    public void test_addBeginBeginEndEndChild_badDataLargePositiveIndex() {
        schedule.get(0).add(a); //a at 0
        try {
            sampleTestObj.addBeginEndChild(schedule, 0, b); //a pushed to 1, b at 0
            sampleTestObj.addBeginBeginEndEndChild(schedule, -10000000, c);
        }
        catch (InvalidObjectException ioe1) {
            assertThat(ioe1.toString(), is("java.io.InvalidObjectException: Parent Location is out of range, cannot add Begin-Begin or End-End child."));
        }
    }

    //structured basis: if the child location is >= 0
    //good data: parent location - 1 is larger or equal to 0
    //boundaries: index at 0
    @Test
    public void test_addBeginEndChild_goodData() throws InvalidObjectException {
        sampleTestObj.addBeginEndChild(schedule, 0, a);
        sampleTestObj.addBeginEndChild(schedule, 1, b);
        assertEquals(sampleTestObj.find(schedule, b), 0);
        //if parent location is 1, child location should be at 0.
        //Therefore, a should be found at index 0.
    }

    //structured basis: if parent location is larger or equal to schedule size
    //compound boundaries: large parentLocation/out of bounds index
    @Test
    public void test_addBeginEndChild_badDataOutOfBoundsIndex() {
        try {
            sampleTestObj.addBeginEndChild(schedule, 100000, b);
        }
        catch (InvalidObjectException ioe) {
            assertThat(ioe.toString(), is("java.io.InvalidObjectException: Parent Location is out of range, cannot add Begin-End child."));
        }
    }

    //good data
    //boundaries: index > 0
    @Test
    public void test_addBeginEndChild_goodDataPositiveIndex() throws InvalidObjectException {
        schedule.get(0).add(a);
        sampleTestObj.addBeginEndChild(schedule, 0, b); //a will be pushed to 1
        sampleTestObj.addBeginEndChild(schedule, 0, c); //b to 1, a to 2
        sampleTestObj.addBeginEndChild(schedule, 2, d); //d will be added in 1
        assertEquals(sampleTestObj.find(schedule, d), 1);
    }

    //structured basis : if child index < 0
    //bad data
    //boundaries: index < 0
    @Test
    public void test_addBeginEndChild_badDataNegativeIndex() throws InvalidObjectException {
        sampleTestObj.addBeginEndChild(schedule, 0, a);
        assertEquals(sampleTestObj.find(schedule, a), 0);
        //if parent location is 0, parent will be pushed back and child will be inserted at index 0
    }

    //compound boundaries: large negative parent location
    @Test
    public void test_addBeginEndChild_badDataLargeNegativeIndex() {
        try {
            sampleTestObj.addBeginEndChild(schedule, -1000000, a);
        }
        catch (InvalidObjectException ioe) {
            assertThat(ioe.toString(), is("java.io.InvalidObjectException: Parent Location is out of range, cannot add Begin-End child."));
        }
    }

    //Structured basis, data flow: the Assignments that are included in the Dependencies exist and those who aren't, don't.
    //boundaries/compound boundaries: the index of the Container containing the assignments that exist = 0/1
    //both Container 0 and 1 are visited and the corresponding Assignments are found
    @Test
    public void test_find_nominal() throws InvalidObjectException {
        a.addDependencies(d1);
        a.addDependencies(d1a);
        a.addDependencies(d1b);
        b.addDependencies(d2);
        c.addDependencies(d3);
        c.addDependencies(d3a);
        list.add(a);
        list.add(b);
        list.add(c);
        sampleTestObj.buildSchedule(schedule, list);
        assertEquals(sampleTestObj.find(schedule, a), 1);
        assertEquals(sampleTestObj.find(schedule, b), 0);
        assertEquals(sampleTestObj.find(schedule, c), 1);
        assertEquals(sampleTestObj.find(schedule, e), 0); //a, b, c, e exist in the dependencies while d, f doesn't
        assertEquals(sampleTestObj.find(schedule, d), -1); //cannot find d
        assertEquals(sampleTestObj.find(schedule, f), -1);
    }

    //good data
    @Test
    public void test_find_goodData() throws InvalidObjectException {
        a.addDependencies(d1a);
        list.add(a);
        sampleTestObj.buildSchedule(schedule, list); //a goes first in the first Container
        assertEquals(sampleTestObj.find(schedule, a), 0);
    }

    //bad data: pass in Assignments that do not exist in the list
    //boundaries/compound boundaries: if the Assignment does not exist, index == -1 (negative)
    @Test
    public void test_find_badDataEmptyList() throws InvalidObjectException {
        sampleTestObj.buildSchedule(schedule, list);
        assertEquals(sampleTestObj.find(schedule, a), -1);
        assertEquals(sampleTestObj.find(schedule, b), -1);
        assertEquals(sampleTestObj.find(schedule, c), -1);
    }

    //bad data: pass in a null Obj
    @Test(expected = NullPointerException.class)
    public void test_find_badDataNullObj() throws InvalidObjectException {
        a.addDependencies(d1);
        list.add(a);
        sampleTestObj.buildSchedule(schedule, list);
        sampleTestObj.find(schedule, null);
    }

}