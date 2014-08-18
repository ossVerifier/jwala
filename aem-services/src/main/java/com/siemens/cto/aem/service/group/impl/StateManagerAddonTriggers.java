package com.siemens.cto.aem.service.group.impl;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.User;

// Keep a list of JVMs that triggered state updates - used for cached periodic FSM handling
class StateManagerAddonTriggers<SIGNALENUM> {
    private Map<SIGNALENUM, Deque<Identifier<?>>> signals = new ConcurrentHashMap<>();

    public boolean anySignalled(SIGNALENUM... sigList) {
        for(SIGNALENUM s : sigList) { 
            Deque<Identifier<?>> result = signals.get(s);
            if(result != null && result.size() > 0) return true;
        }
        return false;
    }
    
    public void addSignal(SIGNALENUM s, Identifier<?> id) {
        Deque<Identifier<?>> result = signals.get(s);
        if(result == null) { 
            result = new ConcurrentLinkedDeque<>();
            result.add(id);
            signals.put(s, result);
        } else { 
            result.add(id);
        }
    }        

    public void drain() {
        for(Deque<Identifier<?>> sigqueue: signals.values()) {
            sigqueue.clear();
        }
    }
    
    
    @Override public String toString() {
        StringBuffer buf  = new StringBuffer();
        boolean[] comma = {false,false};
        for(Map.Entry<SIGNALENUM, Deque<Identifier<?>>> sig : signals.entrySet()) {
            if(sig.getValue() != null && !sig.getValue().isEmpty()) {
                
                if(comma[0]) buf.append(", "); else comma[0] = true;
                buf.append(sig.getKey().toString());
                buf.append(" : ");
                comma[1] = false;
                for(Identifier<?> id : sig.getValue()) {
                    if(id != null) {
                        if(comma[1]) buf.append(", "); else comma[1] = true;
                        buf.append(id);
                    }
                }
                if(!comma[1]) { 
                    buf.append("n/a"); 
                }
            }
        }
        return buf.toString();
    }
}