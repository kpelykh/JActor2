package org.agilewiki.jactor2.core.messages.eventBus;

import org.agilewiki.jactor2.core.messages.Event;

//An event sent to StatusListener blades.
public class StatusUpdate extends Event<StatusListener> {
    //The revised status.
    public final String newStatus;

    //Create a StatusUpdate event.
    public StatusUpdate(final String _newStatus) {
        newStatus = _newStatus;
    }

    //Invokes the statusUpdate method on a StatusListener blade.
    @Override
    protected void processEvent(StatusListener _targetBlade) throws Exception {
        _targetBlade.statusUpdate(this);
    }
}
