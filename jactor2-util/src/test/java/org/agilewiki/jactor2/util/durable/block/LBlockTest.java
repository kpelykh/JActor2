package org.agilewiki.jactor2.util.durable.block;

import junit.framework.TestCase;
import org.agilewiki.jactor2.core.facilities.Facility;
import org.agilewiki.jactor2.core.reactors.NonBlockingReactor;
import org.agilewiki.jactor2.util.durable.Durables;
import org.agilewiki.jactor2.util.durable.FactoryLocator;
import org.agilewiki.jactor2.util.durable.incDes.Root;

public class LBlockTest extends TestCase {
    public void test()
            throws Exception {
        Facility facility = Durables.createFacility();
        try {
            FactoryLocator factoryLocator = Durables.getFactoryLocator(facility);
            Root rj = (Root) Durables.newSerializable(
                    factoryLocator,
                    Root.FACTORY_NAME,
                    new NonBlockingReactor(facility));
            LBlock lb1 = new LBlock();
            lb1.setRootJid(rj);
            byte[] bs = lb1.serialize();

            int hl = lb1.headerLength();
            int rjl = rj.getSerializedLength();
            assertEquals(bs.length, hl + rjl);

            byte[] h = new byte[hl];
            System.arraycopy(bs, 0, h, 0, hl);
            byte[] sd = new byte[rjl];
            System.arraycopy(bs, hl, sd, 0, rjl);

            LBlock lb2 = new LBlock();
            int rjl2 = lb2.setHeaderBytes(h);
            lb2.setRootBytes(sd);
            Root rj2 = lb2.getRoot(factoryLocator, new NonBlockingReactor(facility), null);
        } finally {
            facility.close();
        }
    }
}
