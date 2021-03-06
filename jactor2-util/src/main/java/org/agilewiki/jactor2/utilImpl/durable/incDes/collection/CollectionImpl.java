package org.agilewiki.jactor2.utilImpl.durable.incDes.collection;

import org.agilewiki.jactor2.core.messages.AsyncRequest;
import org.agilewiki.jactor2.core.reactors.Reactor;
import org.agilewiki.jactor2.util.Ancestor;
import org.agilewiki.jactor2.util.durable.JASerializable;
import org.agilewiki.jactor2.util.durable.incDes.Collection;
import org.agilewiki.jactor2.util.durable.incDes.JAInteger;
import org.agilewiki.jactor2.utilImpl.durable.AppendableBytes;
import org.agilewiki.jactor2.utilImpl.durable.FactoryImpl;
import org.agilewiki.jactor2.utilImpl.durable.ReadableBytes;
import org.agilewiki.jactor2.utilImpl.durable.incDes.IncDesImpl;

/**
 * A collection of JID actors.
 */
abstract public class CollectionImpl<ENTRY_TYPE extends JASerializable>
        extends IncDesImpl
        implements Collection<ENTRY_TYPE> {

    /**
     * The size of the serialized data (exclusive of its length header).
     */
    protected int len;

    @Override
    public AsyncRequest<Integer> sizeReq() {
        return new AsyncBladeRequest<Integer>() {
            @Override
            protected void processAsyncRequest() throws Exception {
                processAsyncResponse(size());
            }
        };
    }

    @Override
    public AsyncRequest<ENTRY_TYPE> iGetReq(final int _i) {
        return new AsyncBladeRequest<ENTRY_TYPE>() {
            @Override
            protected void processAsyncRequest() throws Exception {
                processAsyncResponse(iGet(_i));
            }
        };
    }

    @Override
    public AsyncRequest<Void> iSetReq(final int _i, final byte[] _bytes) {
        return new AsyncBladeRequest<Void>() {
            @Override
            protected void processAsyncRequest() throws Exception {
                iSet(_i, _bytes);
                processAsyncResponse(null);
            }
        };
    }

    /**
     * Skip over the length at the beginning of the serialized data.
     *
     * @param readableBytes Holds the serialized data.
     */
    protected void skipLen(ReadableBytes readableBytes) {
        readableBytes.skip(JAInteger.LENGTH);
    }

    /**
     * Returns the size of the serialized data (exclusive of its length header).
     *
     * @param readableBytes Holds the serialized data.
     * @return The size of the serialized data (exclusive of its length header).
     */
    protected int loadLen(ReadableBytes readableBytes) {
        return readableBytes.readInt();
    }

    /**
     * Writes the size of the serialized data (exclusive of its length header).
     *
     * @param appendableBytes The object written to.
     */
    protected void saveLen(AppendableBytes appendableBytes) {
        appendableBytes.writeInt(len);
    }

    /**
     * Process a change in the persistent data.
     *
     * @param lengthChange The change in the size of the serialized data.
     */
    @Override
    public void change(int lengthChange) {
        len += lengthChange;
        super.change(lengthChange);
    }

    /**
     * Resolves a JID pathname, returning a JID actor or null.
     *
     * @param pathname A JID pathname.
     * @return A JID actor or null.
     */
    @Override
    public JASerializable resolvePathname(String pathname)
            throws Exception {
        if (pathname.length() == 0) {
            throw new IllegalArgumentException("empty string");
        }
        int s = pathname.indexOf("/");
        if (s == -1)
            s = pathname.length();
        if (s == 0)
            throw new IllegalArgumentException("pathname " + pathname);
        String ns = pathname.substring(0, s);
        int n = 0;
        try {
            n = Integer.parseInt(ns);
        } catch (Exception ex) {
            throw new IllegalArgumentException("pathname " + pathname);
        }
        if (n < 0 || n >= size())
            throw new IllegalArgumentException("pathname " + pathname);
        JASerializable jid = iGet(n);
        if (s == pathname.length())
            return jid;
        return jid.getDurable().resolvePathname(pathname.substring(s + 1));
    }

    public void initialize(final Reactor reactor, Ancestor parent, FactoryImpl factory)
            throws Exception {
        super.initialize(reactor, parent, factory);
    }
}
