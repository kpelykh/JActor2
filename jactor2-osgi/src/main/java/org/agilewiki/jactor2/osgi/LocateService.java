package org.agilewiki.jactor2.osgi;

import org.agilewiki.jactor2.api.BoundRequest;
import org.agilewiki.jactor2.api.Mailbox;
import org.agilewiki.jactor2.api.BoundRequestBase;
import org.agilewiki.jactor2.api.Transport;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;

import java.util.Map;

/**
 * Locates (or waits for) a service.
 */
public class LocateService<T> implements ServiceChangeReceiver<T> {

    /**
     * The mailbox.
     */
    private Mailbox mailbox;

    /**
     * The service tracker actor.
     */
    private JAServiceTracker<T> tracker;

    /**
     * The transport for returning the service.
     */
    private Transport<T> transport;

    /**
     * Create a LocateService actor.
     *
     * @param _mailbox The actor mailbox.
     * @param clazz    Class name of the desired service.
     */
    public LocateService(Mailbox _mailbox, String clazz) throws Exception {
        mailbox = _mailbox;
        tracker = new JAServiceTracker(mailbox, clazz);
    }

    /**
     * Returns a boundRequest to locate the service.
     *
     * @return The boundRequest.
     */
    public BoundRequest<T> getReq() {
        return new BoundRequestBase<T>(mailbox) {
            @Override
            public void processRequest(final Transport<T> _transport) throws Exception {
                tracker.startReq(LocateService.this).signal();
                transport = _transport;
            }
        };
    }

    @Override
    public void serviceChange(ServiceEvent _event,
                              Map<ServiceReference, T> _tracked,
                              Transport _transport)
            throws Exception {
        _transport.processResponse(null);
        if (_tracked.size() > 0 && transport != null) {
            T service = _tracked.values().iterator().next();
            transport.processResponse(service);
            transport = null;
            tracker.close();
            tracker = null;
        }
    }

    @Override
    public Mailbox getMailbox() {
        return mailbox;
    }
}