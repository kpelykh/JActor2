package org.agilewiki.jactor2.api;

/**
 * An Event implements an operation that is to be performed on an actor in a thread-safe way,
 * without having been bound to a specific target actor.
 *
 * @param <RESPONSE_TYPE>     The class of the result returned when this Event is processed.
 * @param <TARGET_ACTOR_TYPE> The class of the actor that will be used when this Event is processed.
 */
public interface Event<RESPONSE_TYPE, TARGET_ACTOR_TYPE extends Actor>
        extends _Request<RESPONSE_TYPE, TARGET_ACTOR_TYPE> {

    /**
     * Passes this Request to the target Mailbox without a return address.
     * No result is passed back and if an exception is thrown while processing this Request,
     * that exception is simply logged as a warning.
     *
     * @param _targetActor The actor being operated on.
     */
    public void signal(final TARGET_ACTOR_TYPE _targetActor) throws Exception;
}