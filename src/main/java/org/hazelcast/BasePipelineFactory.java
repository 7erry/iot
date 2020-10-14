/*
 * Copyright 2012 - 2019 Anton Tananaev (anton@traccar.org)
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
package org.hazelcast;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hazelcast.config.Keys;
import org.hazelcast.handler.DefaultDataHandler;
import org.hazelcast.handler.SpeedLimitHandler;
import org.hazelcast.handler.TimeHandler;
import org.hazelcast.handler.events.AlertEventHandler;
import org.hazelcast.handler.events.CommandResultEventHandler;
import org.hazelcast.handler.events.DriverEventHandler;
import org.hazelcast.handler.events.FuelDropEventHandler;
import org.hazelcast.handler.events.GeofenceEventHandler;
import org.hazelcast.handler.events.IgnitionEventHandler;
import org.hazelcast.handler.events.MaintenanceEventHandler;
import org.hazelcast.handler.events.MotionEventHandler;
import org.hazelcast.handler.events.OverspeedEventHandler;
import org.hazelcast.handler.ComputedAttributesHandler;
import org.hazelcast.handler.CopyAttributesHandler;
import org.hazelcast.handler.DistanceHandler;
import org.hazelcast.handler.EngineHoursHandler;
import org.hazelcast.handler.FilterHandler;
import org.hazelcast.handler.GeocoderHandler;
import org.hazelcast.handler.GeolocationHandler;
import org.hazelcast.handler.HemisphereHandler;
import org.hazelcast.handler.MotionHandler;
import org.hazelcast.handler.NetworkMessageHandler;
import org.hazelcast.handler.OpenChannelHandler;
import org.hazelcast.handler.RemoteAddressHandler;
import org.hazelcast.handler.StandardLoggingHandler;

import java.util.Map;

public abstract class BasePipelineFactory extends ChannelInitializer<Channel> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasePipelineFactory.class);

    private final TrackerServer server;
    private final String protocol;
    private boolean eventsEnabled;
    private int timeout;

    public BasePipelineFactory(TrackerServer server, String protocol) {
        this.server = server;
        this.protocol = protocol;
        eventsEnabled = Context.getConfig().getBoolean(Keys.EVENT_ENABLE);
        timeout = Context.getConfig().getInteger(Keys.PROTOCOL_TIMEOUT.withPrefix(protocol));
        if (timeout == 0) {
            timeout = Context.getConfig().getInteger(Keys.SERVER_TIMEOUT);
        }
    }

    protected abstract void addProtocolHandlers(PipelineBuilder pipeline);

    @SafeVarargs
    private final void addHandlers(ChannelPipeline pipeline, Class<? extends ChannelHandler>... handlerClasses) {
        for (Class<? extends ChannelHandler> handlerClass : handlerClasses) {
            if (handlerClass != null) {
                pipeline.addLast(Main.getInjector().getInstance(handlerClass));
            }
        }
    }

    public static <T extends ChannelHandler> T getHandler(ChannelPipeline pipeline, Class<T> clazz) {
        for (Map.Entry<String, ChannelHandler> handlerEntry : pipeline) {
            ChannelHandler handler = handlerEntry.getValue();
            if (handler instanceof WrapperInboundHandler) {
                handler = ((WrapperInboundHandler) handler).getWrappedHandler();
            } else if (handler instanceof WrapperOutboundHandler) {
                handler = ((WrapperOutboundHandler) handler).getWrappedHandler();
            }
            if (clazz.isAssignableFrom(handler.getClass())) {
                return (T) handler;
            }
        }
        return null;
    }

    @Override
    protected void initChannel(Channel channel) {
        final ChannelPipeline pipeline = channel.pipeline();

        if (timeout > 0 && !server.isDatagram()) {
            pipeline.addLast(new IdleStateHandler(timeout, 0, 0));
        }
        pipeline.addLast(new OpenChannelHandler(server));
        pipeline.addLast(new NetworkMessageHandler());
        pipeline.addLast(new StandardLoggingHandler(protocol));

        addProtocolHandlers(handler -> {
            if (!(handler instanceof BaseProtocolDecoder || handler instanceof BaseProtocolEncoder)) {
                if (handler instanceof ChannelInboundHandler) {
                    handler = new WrapperInboundHandler((ChannelInboundHandler) handler);
                } else {
                    handler = new WrapperOutboundHandler((ChannelOutboundHandler) handler);
                }
            }
            pipeline.addLast(handler);
        });

        addHandlers(
                pipeline,
                TimeHandler.class,
                GeolocationHandler.class,
                HemisphereHandler.class,
                DistanceHandler.class,
                RemoteAddressHandler.class);

        addDynamicHandlers(pipeline);

        addHandlers(
                pipeline,
                FilterHandler.class,
                GeocoderHandler.class,
                SpeedLimitHandler.class,
                MotionHandler.class,
                CopyAttributesHandler.class,
                EngineHoursHandler.class,
                ComputedAttributesHandler.class,
                WebDataHandler.class,
                DefaultDataHandler.class);

        if (eventsEnabled) {
            addHandlers(
                    pipeline,
                    CommandResultEventHandler.class,
                    OverspeedEventHandler.class,
                    FuelDropEventHandler.class,
                    MotionEventHandler.class,
                    GeofenceEventHandler.class,
                    AlertEventHandler.class,
                    IgnitionEventHandler.class,
                    MaintenanceEventHandler.class,
                    DriverEventHandler.class);
        }

        pipeline.addLast(new MainEventHandler());
    }

    private void addDynamicHandlers(ChannelPipeline pipeline) {
        String handlers = Context.getConfig().getString(Keys.EXTRA_HANDLERS);
        if (handlers != null) {
            for (String handler : handlers.split(",")) {
                try {
                    pipeline.addLast((ChannelHandler) Class.forName(handler).getDeclaredConstructor().newInstance());
                } catch (ReflectiveOperationException error) {
                    LOGGER.warn("Dynamic handler error", error);
                }
            }
        }
    }

}
