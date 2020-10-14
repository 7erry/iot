/*
 * Copyright 2016 - 2018 Anton Tananaev (anton@traccar.org)
 * Copyright 2017 - 2018 Andrey Kunitsyn (andrey@traccar.org)
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
package org.hazelcast.notificators;

import org.hazelcast.Context;
import org.hazelcast.model.Event;
import org.hazelcast.model.Position;
import org.hazelcast.notification.FullMessage;
import org.hazelcast.notification.MessageException;
import org.hazelcast.notification.NotificationFormatter;

import javax.mail.MessagingException;

public final class NotificatorMail extends Notificator {

    @Override
    public void sendSync(long userId, Event event, Position position) throws MessageException {
        try {
            FullMessage message = NotificationFormatter.formatFullMessage(userId, event, position);
            Context.getMailManager().sendMessage(userId, message.getSubject(), message.getBody());
        } catch (MessagingException e) {
            throw new MessageException(e);
        }
    }

}
