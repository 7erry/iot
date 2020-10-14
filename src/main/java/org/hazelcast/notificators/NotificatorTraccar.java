package org.hazelcast.notificators;

import org.hazelcast.Context;

public class NotificatorTraccar extends NotificatorFirebase {

    public NotificatorTraccar() {
        super(
                "https://www.traccar.org/push/",
                Context.getConfig().getString("notificator.traccar.key"));
    }

}
