package org.agilewiki.jactor.general.exceptions;

import junit.framework.TestCase;
import org.agilewiki.jactor.api.Mailbox;
import org.agilewiki.jactor.api.MailboxFactory;
import org.agilewiki.jactor.impl.DefaultMailboxFactoryImpl;

/**
 * Test code.
 */
public class Test1 extends TestCase {
    public void testI() throws Exception {
        final MailboxFactory mailboxFactory = new DefaultMailboxFactoryImpl();
        final Mailbox mailbox = mailboxFactory.createMailbox(true);
        final ActorA actorA = new ActorA(mailbox);
        try {
            actorA.throwRequest.call();
        } catch (final SecurityException se) {
            mailboxFactory.close();
            return;
        }
        throw new Exception("Security exception was not caught");
    }

}