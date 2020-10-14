package org.hazelcast.handler.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.junit.Test;
import org.hazelcast.BaseTest;
import org.hazelcast.model.Event;
import org.hazelcast.model.Position;

public class CommandResultEventHandlerTest extends BaseTest {

    @Test
    public void testCommandResultEventHandler() throws Exception {
        
        CommandResultEventHandler commandResultEventHandler = new CommandResultEventHandler();
        
        Position position = new Position();
        position.set(Position.KEY_RESULT, "Test Result");
        Map<Event, Position> events = commandResultEventHandler.analyzePosition(position);
        assertNotNull(events);
        Event event = events.keySet().iterator().next();
        assertEquals(Event.TYPE_COMMAND_RESULT, event.getType());
    }

}
