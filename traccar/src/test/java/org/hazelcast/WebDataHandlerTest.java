package org.hazelcast;

import org.junit.Test;
import org.hazelcast.config.Config;
import org.hazelcast.config.Keys;
import org.hazelcast.model.Position;

import static org.junit.Assert.assertEquals;

public class WebDataHandlerTest extends ProtocolTest {

    @Test
    public void testFormatRequest() throws Exception {

        Config config = new Config();
        config.setString(Keys.FORWARD_URL, "http://localhost/?fixTime={fixTime}&gprmc={gprmc}&name={name}");

        Position position = position("2016-01-01 01:02:03.000", true, 20, 30);

        WebDataHandler handler = new WebDataHandler(config, Context.getIdentityManager(), null, null);

        assertEquals(
                "http://localhost/?fixTime=1451610123000&gprmc=$GPRMC,010203.000,A,2000.0000,N,03000.0000,E,0.00,0.00,010116,,*05&name=test",
                handler.formatRequest(position));

    }

}
