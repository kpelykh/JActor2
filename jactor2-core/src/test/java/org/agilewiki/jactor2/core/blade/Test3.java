package org.agilewiki.jactor2.core.blade;

import junit.framework.TestCase;
import org.agilewiki.jactor2.core.facilities.Facility;

/**
 * Test code.
 */
public class Test3 extends TestCase {
    public void testI() throws Exception {
        final Facility facility = new Facility();
        final BladeC bladeC = new BladeC(facility);
        final String result = bladeC.throwAReq().call();
        assertEquals("java.lang.SecurityException: thrown on request", result);
        facility.close();
    }
}
