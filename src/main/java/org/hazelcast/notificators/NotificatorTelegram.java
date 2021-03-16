/*
 * Copyright 2019 Anton Tananaev (anton )
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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hazelcast.Context;
import org.hazelcast.model.Event;
import org.hazelcast.model.Position;
import org.hazelcast.notification.NotificationFormatter;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.InvocationCallback;

public class NotificatorTelegram extends Notificator {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificatorTelegram.class);

    private String url;
    private String chatId;

    public static class Message {
        @JsonProperty("chat_id")
        private String chatId;
        @JsonProperty("text")
        private String text;
        @JsonProperty("parse_mode")
        private String parseMode = "html";
    }

    public NotificatorTelegram() {
        url = String.format(
                "https://api.telegram.org/bot%s/sendMessage",
                Context.getConfig().getString("notificator.telegram.key"));
        chatId = Context.getConfig().getString("notificator.telegram.chatId");
    }

    @Override
    public void sendSync(long userId, Event event, Position position) {

        Message message = new Message();
        message.chatId = chatId;
        message.text = NotificationFormatter.formatShortMessage(userId, event, position);

        Context.getClient().target(url).request()
                .async().post(Entity.json(message), new InvocationCallback<Object>() {
            @Override
            public void completed(Object o) {
            }

            @Override
            public void failed(Throwable throwable) {
                LOGGER.warn("Telegram API error", throwable);
            }
        });
    }

    @Override
    public void sendAsync(long userId, Event event, Position position) {
        sendSync(userId, event, position);
    }

}
