package org.hazelcast.handler.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.junit.Test;
import org.hazelcast.BaseTest;
import org.hazelcast.TestIdentityManager;
import org.hazelcast.config.Config;
import org.hazelcast.model.Event;
import org.hazelcast.model.Position;

public class AlertEventHandlerTest extends BaseTest {

    @Test
    public void testAlertEventHandler() {
        
        AlertEventHandler alertEventHandler = new AlertEventHandler(new Config(), new TestIdentityManager());
        
        Position position = new Position();
        position.set(Position.KEY_ALARM, Position.ALARM_GENERAL);
        Map<Event, Position> events = alertEventHandler.analyzePosition(position);
        assertNotNull(events);
        Event event = events.keySet().iterator().next();
        assertEquals(Event.TYPE_ALARM, event.getType());
    }

}
