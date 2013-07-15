package org.agilewiki.jactor2.general.messaging;

import org.agilewiki.jactor2.api.BoundRequest;
import org.agilewiki.jactor2.api.Mailbox;
import org.agilewiki.jactor2.api.BoundRequestBase;
import org.agilewiki.jactor2.api.Transport;

/**
 * Test code.
 */
public class Actor2 {
    private final Mailbox mailbox;

    public Actor2(final Mailbox mbox) {
        this.mailbox = mbox;
    }

    public BoundRequest<String> hi2(final Actor1 actor1) {
        return new BoundRequestBase<String>(mailbox) {
            @Override
            public void processRequest(
                    final Transport<String> responseProcessor)
                    throws Exception {
                actor1.hi1.send(mailbox, responseProcessor);
            }
        };
    }
}