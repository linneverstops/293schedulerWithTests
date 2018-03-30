package scheduler;
import org.junit.Before;
import org.junit.Test;

import java.io.InvalidObjectException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class AssignmentsTest {

    private Assignments a, b;

    private Dependency d1, d2, d3, d4, d5, d6, d7;

    private Assignments sampleObj;

    private Assignments.TestHook sampleTestObj;

    @Before
    public void setUp() throws Exception {
        a = new Assignments("A", 5);
        b = new Assignments("B", 6);
        d1 = new Dependency(a, Dependency.Relationship.End_Begin, b);
        d2 = new Dependency(a, Dependency.Relationship.Begin_Begin, b);
        d3 = new Dependency(b, Dependency.Relationship.End_End, a);
        d4 = new Dependency(null, null, null);
        d5 = new Dependency(b, Dependency.Relationship.Begin_End, b);
        d6 = new Dependency(a, Dependency.Relationship.End_Begin, a);
        d7 = new Dependency(a, Dependency.Relationship.Begin_End, b);
        sampleObj = new Assignments("C", 7);
        sampleTestObj = sampleObj.new TestHook();
    }

    //structured basis: nominal, bad data
    @Test(expected = InvalidObjectException.class)
    public void test_addDependencies_badData() throws InvalidObjectException {
        a.addDependencies(d4);
    }

    //structured basis
    @Test
    public void test_addDependencies_normal() throws InvalidObjectException {
        a.addDependencies(d1);
        assertEquals(a.getDependencies().get(0), d1);
    }


    //structured basis: nominal, good data
    @Test
    public void test_checkRelationshipError_nominal() throws RelationshipException, InvalidObjectException {
        b.addDependencies(d3);
        b.checkRelationshipError();
        //since size is 1, no exception should be thrown
    }

    //bad data, boundaries(dependencies size = 0), compound boundaries(dependencies size = minimum)
    @Test
    public void test_checkRelationshipError_badData() throws RelationshipException {
        a.checkRelationshipError();
        //will not throw NullPointer or other exceptions because the path will skip all the loops
    }

    //structured basis: contradicting requirement
    @Test
    public void test_checkRelationshipError_contradictingReq() throws InvalidObjectException {
        a.addDependencies(d1);
        a.addDependencies(d2);
        try {
            a.checkRelationshipError();
        }
        catch (RelationshipException r) {
            assertThat(r.toString(), is("RelationshipException{errorCode=CONTRADICTING_REQUIREMENT}"));
        }
    }

    //structured basis: circular requirement
    @Test
    public void test_checkRelationshipError_circularReq() throws InvalidObjectException {
        b.addDependencies(d1);
        b.addDependencies(d3);
        try {
            b.checkRelationshipError();
        }
        catch(RelationshipException r) {
            assertThat(r.toString(), is("RelationshipException{errorCode=CIRCULAR_REQUIREMENT}"));
        }
    }

    //structured basis: nominal all true, good data
    @Test
    public void test_checkCircularRequirement_nominal() throws RelationshipException {
        try {
            sampleTestObj.checkCircularRequirement(d1, d3);
        }
        catch (RelationshipException r) {
            assertThat(r.toString(), is("RelationshipException{errorCode=CIRCULAR_REQUIREMENT}"));
        }
    }

    //structured basis: all false
    @Test
    public void test_checkCircularRequirement_allFalse() throws RelationshipException {
        sampleTestObj.checkCircularRequirement(d1, d2);
    }

    //data flow: first condition false, second true (d1 child != d2 parent)
    @Test
    public void test_checkCircularRequirement_firstFalse() throws RelationshipException {
        sampleTestObj.checkCircularRequirement(d1, d5);
    }

    //data flow: first condition true, second false(d1 parent != d2 child)
    @Test
    public void test_checkCircularRequirement_secondFalse() throws RelationshipException {
        sampleTestObj.checkCircularRequirement(d1, d6);
    }

    //bad data
    @Test(expected = NullPointerException.class)
    public void test_checkCircularRequirement_badData() throws RelationshipException {
        sampleTestObj.checkCircularRequirement(d1, d4);
    }

    //structured  basis: nominal: all true, good data
    @Test
    public void test_checkContradictingRequirement_nominal() throws RelationshipException {
        try {
            sampleTestObj.checkContradictingRequirement(d1, d2);
        }
        catch (RelationshipException r) {
            assertThat(r.toString(), is("RelationshipException{errorCode=CONTRADICTING_REQUIREMENT}"));
        }
    }

    //structured basis: all false(differentChildParent and isEndBeginBeginEnd)
    @Test
    public void test_checkContradictingRequirement_allFalse() throws RelationshipException {
        sampleTestObj.checkContradictingRequirement(d1, d5);
    }

    //data flow: first false, second true(differentChildParent and not EndBeginBeginEnd)
    @Test
    public void test_checkContradictingRequirement_firstFalse() throws RelationshipException {
        sampleTestObj.checkContradictingRequirement(d1, d3);
    }

    //data flow: first true, second false(sameChildParent and isEndBeginBeginEnd)
    @Test
    public void test_checkContradictingRequirement_secondFalse() throws RelationshipException {
        sampleTestObj.checkContradictingRequirement(d1, d7);
    }

    //bad data
    @Test(expected = NullPointerException.class)
    public void test_checkContradictingRequirement_badData() throws RelationshipException {
        sampleTestObj.checkContradictingRequirement(d1, d4);
    }

    //structured basis: nominal first if branch all true, good data (d1: End_Begin; d2: Begin_End)
    @Test
    public void test_isEndBeginBeginEnd_firstIfNominal() {
        assertTrue(sampleTestObj.isEndBeginBeginEnd(d1, d5));
    }

    //structured basis: first if branch all false (d1: not End_Begin; d2: not Begin_End)
    @Test
    public void test_isEndBeginBeginEnd_firstIfAllFalse() {
        assertFalse(sampleTestObj.isEndBeginBeginEnd(d2, d3));
    }

    //data flow: first if branch first false second true (d1: not End_Begin; d2: Begin_End)
    @Test
    public void test_isEndBeginBeginEnd_firstIfFirstFalse() {
        assertFalse(sampleTestObj.isEndBeginBeginEnd(d2, d5));
    }

    //data flow: first if branch first true second false (d1: End_Begin; d2: not Begin_End)
    @Test
    public void test_isEndBeginBeginEnd_firstIfSecondFalse() {
        assertFalse(sampleTestObj.isEndBeginBeginEnd(d1, d3));
    }

    //structured basis: nominal outside branch(not if) all true, good data (d1: Begin_End; d2: End_Begin)
    @Test
    public void test_isEndBeginBeginEnd_notIfNominal() {
        assertTrue(sampleTestObj.isEndBeginBeginEnd(d5, d1));
    }

    //structured basis: outside branch all false (d1: not Begin_End; d2: not End_Begin)
    @Test
    public void test_isEndBeginBeginEnd_notIfAllFalse() {
        assertFalse(sampleTestObj.isEndBeginBeginEnd(d2, d3));
    }

    //data flow: outside branch first false second true (d1: not Begin_End; d2: End_Begin)
    @Test
    public void test_isEndBeginBeginEnd_notIfFirstFalse() {
        assertFalse(sampleTestObj.isEndBeginBeginEnd(d2, d1));
    }

    //data flow: outside branch first true second false (d1: Begin_End; d2: not End_Begin)
    @Test
    public void test_isEndBeginBeginEnd_notIfSecondFalse() {
        assertFalse(sampleTestObj.isEndBeginBeginEnd(d5, d3));
    }

    //bad data
    @Test
    public void test_isEndBeginBeginEnd_badData() {
        assertFalse(sampleTestObj.isEndBeginBeginEnd(d4, d3));
        //will return false because null can be compared to non-null String
    }


}