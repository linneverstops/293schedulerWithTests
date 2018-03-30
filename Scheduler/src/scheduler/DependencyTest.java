package scheduler;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DependencyTest {

    private Assignments a, b;

    private Dependency d1, d2, d3, d4, d5;

    @Before
    public void setUp() throws Exception {
        a = new Assignments("A", 5);
        b = new Assignments("B", 5);
        d1 = new Dependency(a, Dependency.Relationship.End_Begin, b);
        d2 = new Dependency(a, Dependency.Relationship.End_Begin, b);
        d3 = new Dependency(a, Dependency.Relationship.End_Begin, a);
        d4 = new Dependency(b, Dependency.Relationship.End_Begin, a);
        d5 = new Dependency(null, null, null);
    }

    //structured basis, good data,
    @Test
    public void test_sameChildParent_nominal() throws Exception {
        assertTrue(d1.sameChildParent(d2));
    }

    //structured basis, data flow,
    @Test
    public void test_sameChildParent_firstFalse() throws Exception {
        assertFalse(d3.sameChildParent(d4));
    }

    //structured basis, data flow,
    @Test
    public void test_sameChildParent_secondFalse() throws Exception {
        assertFalse(d1.sameChildParent(d3));
    }

    //structure basis
    @Test
    public void test_sameChildParent_allFalse() throws Exception {
        assertFalse(d1.sameChildParent(d4));
    }

    //bad data
    @Test(expected = NullPointerException.class)
    public void test_sameChildParent_badData() throws Exception {
        d1.sameChildParent(d5);
    }
}