package org.agilewiki.jactor2.core.messaging;

import org.agilewiki.jactor2.core.Actor;
import org.agilewiki.jactor2.core.processing.MessageProcessor;
import org.agilewiki.jactor2.core.processing.MessageProcessorBase;

/**
 * An Event instance is used to pass one-way messages to any number of Actor objects.
 * Event messages are unbuffered and are sent immediately. The net effect of sending
 * an event to an actor is that Event.processEvent, an application-specific method,
 * is called in a thread-safe way from the actor's MessageProcessor's own thread.
 * <p>
 * As neither message buffering nor thread migration are used, events may be slower,
 * in terms of both latency and throughput, than a request. On the other hand, when
 * the target MessageProcessor is atomic, event processing is not delayed until a response is
 * assigned to a prior request.
 * </p>
 * <p>
 * Some care needs to be taken with the parameters passed to the constructor of an
 * event. Either the constructor needs to perform a deep copy of these parameters
 * or the application must take care not to change the contents of these parameters,
 * as their will likely be accessed from a different thread when the target actor
 * is operated on.
 * </p>
 * <h3>Sample Usage:</h3>
 * <pre>
 * import org.agilewiki.jactor2.core.ActorBase;
 * import org.agilewiki.jactor2.core.context.JAContext;
 * import org.agilewiki.jactor2.core.processing.MessageProcessor;
 * import org.agilewiki.jactor2.core.processing.NonBlockingMessageProcessor;
 *
 * public class EventSample {
 *
 *     public static void main(String[] args) throws Exception {
 *
 *         //A context with one thread.
 *         final JAContext jaContext = new JAContext(1);
 *
 *         //Create a SampleActor1 instance.
 *         SampleActor1 sampleActor1 = new SampleActor1(new NonBlockingMessageProcessor(jaContext));
 *
 *         //Print "finished" and exit when the event is processed by SampleActor1.
 *         new FinEvent("finished").signal(sampleActor1);
 *
 *         //Hang until exit.
 *         Thread.sleep(1000000);
 *
 *         }
 *     }
 *
 *     class SampleActor1 extends ActorBase {
 *
 *         SampleActor1(final MessageProcessor _messageProcessor) throws Exception {
 *             initialize(_messageProcessor);
 *         }
 *
 *         void fin(final String msg) throws Exception {
 *             System.out.println(msg);
 *             System.exit(0);
 *         }
 *     }
 *
 *     //When a FinEvent is passed to an actor, the fin method is called.
 *     class FinEvent extends Event<SampleActor1> {
 *         private final String msg;
 *
 *         FinEvent(final String _msg) {
 *             msg = _msg;
 *         }
 *
 *         {@literal @}Override
 *         public void processEvent(SampleActor1 _targetActor) throws Exception {
 *             _targetActor.fin(msg);
 *         }
 *     }
 *
 * Output:
 * finished
 * </pre>
 *
 * @param <TARGET_ACTOR_TYPE> The class of the actor that will be targeted when this Event is passed.
 */
public abstract class Event<TARGET_ACTOR_TYPE extends Actor> {

    /**
     * Passes an event message immediately to the target MessageProcessor for subsequent processing
     * by the thread of the that message processor. No result is passed back and if an exception is
     * thrown while processing the event,that exception is simply logged as a warning.
     *
     * @param _targetActor The actor to be operated on.
     */
    final public void signal(final TARGET_ACTOR_TYPE _targetActor) throws Exception {
        final EventMessage message = new EventMessage(_targetActor);
        ((MessageProcessorBase) _targetActor.getMessageProcessor()).unbufferedAddMessage(message, false);
    }

    /**
     * The processEvent method will be invoked by the target MessageProcessor on its own thread
     * when this event is processed.
     *
     * @param _targetActor The actor to be operated on.
     */
    abstract public void processEvent(final TARGET_ACTOR_TYPE _targetActor)
            throws Exception;

    /**
     * The message subclass used to pass events. Event messages are not reused, with a
     * new event message being created each time Event.signal is called.
     */
    final private class EventMessage implements Message {

        /**
         * The actor to be operated on.
         */
        final TARGET_ACTOR_TYPE targetActor;

        /**
         * Create an EventMessage.
         *
         * @param _targetActor The actor to be operated on.
         */
        EventMessage(final TARGET_ACTOR_TYPE _targetActor) {
            targetActor = _targetActor;
        }

        @Override
        public boolean isForeign() {
            return false;
        }

        @Override
        public boolean isResponsePending() {
            return false;
        }

        @Override
        public void close() throws Exception {
        }

        @Override
        public void eval(final MessageProcessor _targetMessageProcessor) {
            MessageProcessorBase targetMessageProcessor = (MessageProcessorBase) _targetMessageProcessor;
            targetMessageProcessor.setExceptionHandler(null);
            targetMessageProcessor.setCurrentMessage(this);
            try {
                processEvent(targetActor);
            } catch (final Throwable t) {
                processThrowable(targetMessageProcessor, t);
            }
        }

        @Override
        public void processThrowable(final MessageProcessor _activeMessageProcessor, final Throwable _t) {
            MessageProcessorBase activeMessageProcessor = (MessageProcessorBase) _activeMessageProcessor;
            ExceptionHandler exceptionHandler = activeMessageProcessor.getExceptionHandler();
            if (exceptionHandler != null) {
                try {
                    exceptionHandler.processException(_t);
                } catch (final Throwable u) {
                    activeMessageProcessor.getLogger().error("Exception handler unable to process throwable "
                            + exceptionHandler.getClass().getName(), u);
                    activeMessageProcessor.getLogger().error("Thrown by exception handler and uncaught "
                            + exceptionHandler.getClass().getName(), _t);
                }
            }
        }
    }
}
