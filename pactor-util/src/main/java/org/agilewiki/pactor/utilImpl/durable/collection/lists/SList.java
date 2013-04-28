package org.agilewiki.pactor.utilImpl.durable.collection.lists;

import org.agilewiki.pactor.api.Mailbox;
import org.agilewiki.pactor.api.Request;
import org.agilewiki.pactor.api.RequestBase;
import org.agilewiki.pactor.api.Transport;
import org.agilewiki.pactor.util.Ancestor;
import org.agilewiki.pactor.util.durable.*;
import org.agilewiki.pactor.utilImpl.durable.FactoryImpl;
import org.agilewiki.pactor.utilImpl.durable.IncDesImpl;
import org.agilewiki.pactor.utilImpl.durable.collection.CollectionImpl;

import java.util.ArrayList;

/**
 * Holds an ArrayList of JID actors, all of the same type.
 */
public class SList<ENTRY_TYPE extends PASerializable>
        extends CollectionImpl<ENTRY_TYPE>
        implements PAList<ENTRY_TYPE> {

    public int initialCapacity = 10;

    /**
     * IncDesImpl factory of the elements.
     */
    protected Factory entryFactory;

    /**
     * A list of JID actors.
     */
    protected ArrayList<ENTRY_TYPE> list;

    private Request<Void> emptyReq;

    public Request<Void> emptyReq() {
        return emptyReq;
    }

    /**
     * Returns the size of the collection.
     *
     * @return The size of the collection.
     */
    @Override
    public int size()
            throws Exception {
        initializeList();
        return list.size();
    }

    /**
     * Returns the ith JID component.
     *
     * @param i The index of the element of interest.
     *          If negative, the index used is increased by the size of the collection,
     *          so that -1 returns the last element.
     * @return The ith JID component, or null if the index is out of range.
     */
    @Override
    public ENTRY_TYPE iGet(int i)
            throws Exception {
        initializeList();
        if (i < 0)
            i += list.size();
        if (i < 0 || i >= list.size())
            return null;
        return list.get(i);
    }

    /**
     * Returns the number of bytes needed to serialize the persistent data.
     *
     * @return The minimum size of the byte array needed to serialize the persistent data.
     */
    @Override
    public int getSerializedLength() {
        return Durables.INT_LENGTH * 2 + len;
    }

    /**
     * Load the serialized data into the JID.
     *
     * @param readableBytes Holds the serialized data.
     */
    @Override
    public void load(ReadableBytes readableBytes) {
        super.load(readableBytes);
        len = loadLen(readableBytes);
        list = null;
        readableBytes.skip(Durables.INT_LENGTH + len);
    }

    /**
     * Returns the IncDesFactory for all the elements in the list.
     *
     * @return The IncDesFactory for of all the elements in the list.
     */
    protected Factory getEntryFactory()
            throws Exception {
        if (entryFactory == null)
            throw new IllegalStateException("entryFactory uninitialized");
        return entryFactory;
    }

    /**
     * Perform lazy initialization.
     *
     * @throws Exception Any exceptions thrown during initialization.
     */
    protected void initializeList()
            throws Exception {
        if (list != null)
            return;
        entryFactory = getEntryFactory();
        if (!isSerialized()) {
            list = new ArrayList<ENTRY_TYPE>();
            return;
        }
        ReadableBytes readableBytes = readable();
        skipLen(readableBytes);
        int count = readableBytes.readInt();
        list = new ArrayList<ENTRY_TYPE>(count);
        int i = 0;
        while (i < count) {
            ENTRY_TYPE elementJid = (ENTRY_TYPE) createSubordinate(entryFactory, this, readableBytes);
            list.add(elementJid);
            i += 1;
        }
    }

    /**
     * Serialize the persistent data.
     *
     * @param appendableBytes The wrapped byte array into which the persistent data is to be serialized.
     */
    @Override
    protected void serialize(AppendableBytes appendableBytes)
            throws Exception {
        saveLen(appendableBytes);
        appendableBytes.writeInt(size());
        int i = 0;
        while (i < size()) {
            iGet(i).getDurable().save(appendableBytes);
            i += 1;
        }
    }

    /**
     * Resolves a JID pathname, returning a JID actor or null.
     *
     * @param pathname A JID pathname.
     * @return A JID actor or null.
     * @throws Exception Any uncaught exception which occurred while processing the request.
     */
    @Override
    public PASerializable resolvePathname(String pathname)
            throws Exception {
        initializeList();
        return super.resolvePathname(pathname);
    }

    /**
     * Creates a JID actor and loads its serialized data.
     *
     * @param i     The index of the desired element.
     * @param bytes Holds the serialized data.
     * @throws Exception Any exceptions thrown while processing the request.
     */
    @Override
    public void iSet(int i, byte[] bytes)
            throws Exception {
        initializeList();
        if (i < 0)
            i += list.size();
        if (i < 0 || i >= list.size())
            throw new IllegalArgumentException();
        PASerializable elementJid = createSubordinate(entryFactory, this, bytes);
        PASerializable oldElementJid = iGet(i);
        ((IncDesImpl) oldElementJid.getDurable()).setContainerJid(null);
        list.set(i, (ENTRY_TYPE) elementJid);
        change(elementJid.getDurable().getSerializedLength() -
                oldElementJid.getDurable().getSerializedLength());
    }

    @Override
    public Request<Void> iAddReq(final int _i, final byte[] _bytes) {
        return new RequestBase<Void>(getMailbox()) {
            @Override
            public void processRequest(Transport<Void> _rp) throws Exception {
                iAdd(_i, _bytes);
                _rp.processResponse(null);
            }
        };
    }

    @Override
    public void iAdd(int i, byte[] bytes)
            throws Exception {
        initializeList();
        if (i < 0)
            i = size() + 1 + i;
        PASerializable jid = createSubordinate(entryFactory, this, bytes);
        int c = jid.getDurable().getSerializedLength();
        list.add(i, (ENTRY_TYPE) jid);
        change(c);
    }

    @Override
    public Request<Void> iAddReq(final int _i) {
        return new RequestBase<Void>(getMailbox()) {
            @Override
            public void processRequest(Transport<Void> _rp) throws Exception {
                iAdd(_i);
                _rp.processResponse(null);
            }
        };
    }

    @Override
    public void iAdd(int i)
            throws Exception {
        initializeList();
        if (i < 0)
            i = size() + 1 + i;
        PASerializable jid = createSubordinate(entryFactory, this);
        int c = jid.getDurable().getSerializedLength();
        list.add(i, (ENTRY_TYPE) jid);
        change(c);
    }

    @Override
    public void empty()
            throws Exception {
        int c = 0;
        int i = 0;
        int s = size();
        while (i < s) {
            PASerializable jid = iGet(i);
            ((IncDesImpl) jid.getDurable()).setContainerJid(null);
            c -= jid.getDurable().getSerializedLength();
            i += 1;
        }
        list.clear();
        change(c);
    }

    @Override
    public Request<Void> iRemoveReq(final int _i) {
        return new RequestBase<Void>(getMailbox()) {
            @Override
            public void processRequest(Transport<Void> _rp) throws Exception {
                iRemove(_i);
                _rp.processResponse(null);
            }
        };
    }

    @Override
    public void iRemove(int i)
            throws Exception {
        int s = size();
        if (i < 0)
            i += s;
        if (i < 0 || i >= s)
            throw new IllegalArgumentException();
        PASerializable jid = (IncDesImpl) iGet(i);
        ((IncDesImpl) jid.getDurable()).setContainerJid(null);
        int c = -jid.getDurable().getSerializedLength();
        list.remove(i);
        change(c);
    }

    public void initialize(final Mailbox mailbox, Ancestor parent, FactoryImpl factory) {
        super.initialize(mailbox, parent, factory);
        emptyReq = new RequestBase<Void>(getMailbox()) {
            @Override
            public void processRequest(Transport<Void> _rp) throws Exception {
                empty();
                _rp.processResponse(null);
            }
        };
    }
}
