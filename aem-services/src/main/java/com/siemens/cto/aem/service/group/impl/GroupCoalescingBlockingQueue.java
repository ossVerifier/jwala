package com.siemens.cto.aem.service.group.impl;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;

public class GroupCoalescingBlockingQueue implements BlockingQueue<Message<Identifier<Group>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupCoalescingBlockingQueue.class);

    private final int capacity;
    private final ConcurrentHashMap<Long, Message<Identifier<Group>>> chm;
    public static Comparator<Message<Identifier<Group>>> ordering = new Comparator<Message<Identifier<Group>>>() {

        @Override
        public int compare(Message<Identifier<Group>> o1, Message<Identifier<Group>> o2) {
            return o1.getPayload().getId().compareTo(o2.getPayload().getId());
        }

    };

    public GroupCoalescingBlockingQueue(int capacity) {
        this.capacity = capacity;
        chm = new ConcurrentHashMap<>(capacity);
    }

    public GroupCoalescingBlockingQueue() {
        this(20);
    }

    Identifier<Group> last;

     @Override
     public Message<Identifier<Group>> poll(long timeout, java.util.concurrent.TimeUnit unit) throws InterruptedException
     {
         long end= System.currentTimeMillis() + unit.convert(timeout, TimeUnit.MILLISECONDS);

         Map.Entry<Long, Message<Identifier<Group>>> entry = null;
         long delta = -1;
         do {
             try {
                 entry = chm.entrySet().iterator().next();
             } catch(NoSuchElementException ne) {
                 LOGGER.debug("No more messages", ne);
                 delta = end - System.currentTimeMillis();
                 if(delta >0) {
                     if(delta >20) {
                         Thread.sleep(20);
                     }
                     else {
                         Thread.sleep(delta);
                     }
                 }
                 continue;
             }
         } while(delta >0 && entry == null);

         if(entry == null) {
             return null;
         }

         Message<Identifier<Group>> result = chm.remove(entry.getKey());

         if(result == null) {
             return null;
         }

         return result;
     }
    public Message<Identifier<Group>> poll() {
        Map.Entry<Long, Message<Identifier<Group>>> entry = null;
        try {
            entry = chm.entrySet().iterator().next();
        } catch(NoSuchElementException ne) {
            LOGGER.debug("No more messages", ne);
            return null;
        }

        return chm.remove(entry.getKey());
     }

    @Override
    public Message<Identifier<Group>> remove() {
        return poll();
    }

    @Override
    public Message<Identifier<Group>> element() {
        return poll();
    }

    @Override
    public Message<Identifier<Group>> peek() {
        return chm.entrySet().iterator().next().getValue();
    }

    @Override
    public int size() {
        return chm.size();
    }

    @Override
    public boolean isEmpty() {
        return chm.isEmpty();
    }

    @Override
    public Iterator<Message<Identifier<Group>>> iterator() {
        return chm.values().iterator();
    }

    @Override
    public Object[] toArray() {
        return chm.values().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return chm.values().toArray(a);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return chm.values().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Message<Identifier<Group>>> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        chm.clear();
    }

    @Override
    public boolean add(Message<Identifier<Group>> e) {
        chm.putIfAbsent(e.getPayload().getId(), e);
        return true;
    }

    @Override
    public boolean offer(Message<Identifier<Group>> e) {
        chm.putIfAbsent(e.getPayload().getId(), e);
        return true;
    }

    @Override
    public void put(Message<Identifier<Group>> e) throws InterruptedException {
        chm.putIfAbsent(e.getPayload().getId(), e);
    }

    @Override
    public boolean offer(Message<Identifier<Group>> e, long timeout, TimeUnit unit) throws InterruptedException {
       chm.putIfAbsent(e.getPayload().getId(), e);
       return true;
    }

    @Override
    public Message<Identifier<Group>> take() throws InterruptedException {
        return poll();
    }

    @Override
    public int remainingCapacity() {
        return capacity-chm.size();
    }

    @Override
    public boolean remove(Object o) {
        return null != chm.remove(o);
    }

    @Override
    public boolean contains(Object o) {
        return chm.containsKey(o);
    }

    @Override
    public int drainTo(Collection<? super Message<Identifier<Group>>> c) {
        Message<Identifier<Group>> msg;

        int count = 0;
        while(null != (msg = poll())) {
            ++count;
            c.add(msg);
        }

        return count;
    }

    @Override
    public int drainTo(Collection<? super Message<Identifier<Group>>> c, int maxElements) {
        Message<Identifier<Group>> msg;

        int count = 0;
        while(count < maxElements && null != (msg = poll())) {
            ++count;
            c.add(msg);
        }

        return count;
    }
}
