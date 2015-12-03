package com.siemens.cto.aem.service.state.jms.sender;

import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.service.state.jms.sender.message.CurrentStateMessageExtractor;
import com.siemens.cto.aem.service.state.jms.sender.message.GroupCurrentStateMessageExtractor;
import com.siemens.cto.aem.service.state.jms.sender.message.JvmCurrentStateMessageExtractor;
import com.siemens.cto.aem.service.state.jms.sender.message.WebServerCurrentStateMessageExtractor;

import java.util.EnumMap;
import java.util.Map;

public enum CurrentStateExtractorMap {

    DEFAULT {
        @Override
        public Map<StateType, CurrentStateMessageExtractor> getMap() {
            final Map<StateType , CurrentStateMessageExtractor> map = new EnumMap<>(StateType.class);
            map.put(StateType.GROUP, new GroupCurrentStateMessageExtractor());
            map.put(StateType.JVM, new JvmCurrentStateMessageExtractor());
            map.put(StateType.WEB_SERVER, new WebServerCurrentStateMessageExtractor());
            return map;
        }
    };

    public abstract Map<StateType, CurrentStateMessageExtractor> getMap();
}
