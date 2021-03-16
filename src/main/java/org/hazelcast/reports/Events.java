/*
 * Copyright 2016 - 2018 Anton Tananaev (anton )
 * Copyright 2016 - 2018 Andrey Kunitsyn (andrey )
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
package org.hazelcast.reports;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.poi.ss.util.WorkbookUtil;
import org.hazelcast.Context;
import org.hazelcast.model.Device;
import org.hazelcast.model.Event;
import org.hazelcast.model.Geofence;
import org.hazelcast.model.Group;
import org.hazelcast.model.Maintenance;
import org.hazelcast.reports.model.DeviceReport;

public final class Events {

    private Events() {
    }

    public static Collection<Event> getObjects(long userId, Collection<Long> deviceIds, Collection<Long> groupIds,
            Collection<String> types, Date from, Date to) throws SQLException {
        ReportUtils.checkPeriodLimit(from, to);
        ArrayList<Event> result = new ArrayList<>();
        for (long deviceId: ReportUtils.getDeviceList(deviceIds, groupIds)) {
            Context.getPermissionsManager().checkDevice(userId, deviceId);
            Collection<Event> events = Context.getDataManager().getEvents(deviceId, from, to);
            boolean all = types.isEmpty() || types.contains(Event.ALL_EVENTS);
            for (Event event : events) {
                if (all || types.contains(event.getType())) {
                    long geofenceId = event.getGeofenceId();
                    long maintenanceId = event.getMaintenanceId();
                    if ((geofenceId == 0 || Context.getGeofenceManager().checkItemPermission(userId, geofenceId))
                            && (maintenanceId == 0
                            || Context.getMaintenancesManager().checkItemPermission(userId, maintenanceId))) {
                       result.add(event);
                    }
                }
            }
        }
        return result;
    }

    public static void getExcel(OutputStream outputStream,
            long userId, Collection<Long> deviceIds, Collection<Long> groupIds,
            Collection<String> types, Date from, Date to) throws SQLException, IOException {
        ReportUtils.checkPeriodLimit(from, to);
        ArrayList<DeviceReport> devicesEvents = new ArrayList<>();
        ArrayList<String> sheetNames = new ArrayList<>();
        HashMap<Long, String> geofenceNames = new HashMap<>();
        HashMap<Long, String> maintenanceNames = new HashMap<>();
        for (long deviceId: ReportUtils.getDeviceList(deviceIds, groupIds)) {
            Context.getPermissionsManager().checkDevice(userId, deviceId);
            Collection<Event> events = Context.getDataManager().getEvents(deviceId, from, to);
            boolean all = types.isEmpty() || types.contains(Event.ALL_EVENTS);
            for (Iterator<Event> iterator = events.iterator(); iterator.hasNext();) {
                Event event = iterator.next();
                if (all || types.contains(event.getType())) {
                    long geofenceId = event.getGeofenceId();
                    long maintenanceId = event.getMaintenanceId();
                    if (geofenceId != 0) {
                        if (Context.getGeofenceManager().checkItemPermission(userId, geofenceId)) {
                            Geofence geofence = Context.getGeofenceManager().getById(geofenceId);
                            if (geofence != null) {
                                geofenceNames.put(geofenceId, geofence.getName());
                            }
                        } else {
                            iterator.remove();
                        }
                    } else if (maintenanceId != 0) {
                        if (Context.getMaintenancesManager().checkItemPermission(userId, maintenanceId)) {
                            Maintenance maintenance = Context.getMaintenancesManager().getById(maintenanceId);
                            if (maintenance != null) {
                                maintenanceNames.put(maintenanceId, maintenance.getName());
                            }
                        } else {
                            iterator.remove();
                        }
                    }
                } else {
                    iterator.remove();
                }
            }
            DeviceReport deviceEvents = new DeviceReport();
            Device device = Context.getIdentityManager().getById(deviceId);
            deviceEvents.setDeviceName(device.getName());
            sheetNames.add(WorkbookUtil.createSafeSheetName(deviceEvents.getDeviceName()));
            if (device.getGroupId() != 0) {
                Group group = Context.getGroupsManager().getById(device.getGroupId());
                if (group != null) {
                    deviceEvents.setGroupName(group.getName());
                }
            }
            deviceEvents.setObjects(events);
            devicesEvents.add(deviceEvents);
        }
        String templatePath = Context.getConfig().getString("report.templatesPath",
                "templates/export/");
        try (InputStream inputStream = new FileInputStream(templatePath + "/events.xlsx")) {
            org.jxls.common.Context jxlsContext = ReportUtils.initializeContext(userId);
            jxlsContext.putVar("devices", devicesEvents);
            jxlsContext.putVar("sheetNames", sheetNames);
            jxlsContext.putVar("geofenceNames", geofenceNames);
            jxlsContext.putVar("maintenanceNames", maintenanceNames);
            jxlsContext.putVar("from", from);
            jxlsContext.putVar("to", to);
            ReportUtils.processTemplateWithSheets(inputStream, outputStream, jxlsContext);
        }
    }
}
