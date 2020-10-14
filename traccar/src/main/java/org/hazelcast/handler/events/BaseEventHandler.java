/*
 * Copyright 2016 Anton Tananaev (anton@traccar.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hazelcast.handler.events;

import java.util.Map;

import org.hazelcast.BaseDataHandler;
import org.hazelcast.Context;
import org.hazelcast.model.Event;
import org.hazelcast.model.Position;

public abstract class BaseEventHandler extends BaseDataHandler {

    @Override
    protected Position handlePosition(Position position) {
        Map<Event, Position> events = analyzePosition(position);
        if (events != null && Context.getNotificationManager() != null) {
            Context.getNotificationManager().updateEvents(events);
        }
        return position;
    }

    protected abstract Map<Event, Position> analyzePosition(Position position);

}
