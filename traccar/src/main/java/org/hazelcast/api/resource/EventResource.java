package org.hazelcast.api.resource;

import java.sql.SQLException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.hazelcast.Context;
import org.hazelcast.api.BaseResource;
import org.hazelcast.model.Event;
import org.hazelcast.model.Geofence;
import org.hazelcast.model.Maintenance;

@Path("events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class EventResource extends BaseResource {

    @Path("{id}")
    @GET
    public Event get(@PathParam("id") long id) throws SQLException {
        Event event = Context.getDataManager().getObject(Event.class, id);
        Context.getPermissionsManager().checkDevice(getUserId(), event.getDeviceId());
        if (event.getGeofenceId() != 0) {
            Context.getPermissionsManager().checkPermission(Geofence.class, getUserId(), event.getGeofenceId());
        }
        if (event.getMaintenanceId() != 0) {
            Context.getPermissionsManager().checkPermission(Maintenance.class, getUserId(), event.getMaintenanceId());
        }
        return event;
    }

}
