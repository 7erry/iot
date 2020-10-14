package org.hazelcast.handler;

import org.junit.Test;
import org.hazelcast.config.Config;
import org.hazelcast.model.Position;

import static org.junit.Assert.assertEquals;

public class DistanceHandlerTest {

    @Test
    public void testCalculateDistance() {

        DistanceHandler distanceHandler = new DistanceHandler(new Config(), null);

        Position position = distanceHandler.handlePosition(new Position());

        assertEquals(0.0, position.getAttributes().get(Position.KEY_DISTANCE));
        assertEquals(0.0, position.getAttributes().get(Position.KEY_TOTAL_DISTANCE));

        position.set(Position.KEY_DISTANCE, 100);

        position = distanceHandler.handlePosition(position);

        assertEquals(100.0, position.getAttributes().get(Position.KEY_DISTANCE));
        assertEquals(100.0, position.getAttributes().get(Position.KEY_TOTAL_DISTANCE));

    }

}
