/*
 * Copyright 2016 Anton Tananaev (anton )
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
package org.hazelcast.model;

import java.text.ParseException;

import org.hazelcast.Context;
import org.hazelcast.database.QueryIgnore;
import org.hazelcast.geofence.GeofenceCircle;
import org.hazelcast.geofence.GeofenceGeometry;
import org.hazelcast.geofence.GeofencePolygon;
import org.hazelcast.geofence.GeofencePolyline;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Geofence extends ScheduledModel {

    public static final String TYPE_GEOFENCE_CILCLE = "geofenceCircle";
    public static final String TYPE_GEOFENCE_POLYGON = "geofencePolygon";
    public static final String TYPE_GEOFENCE_POLYLINE = "geofencePolyline";

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String area;

    public String getArea() {
        return area;
    }

    public void setArea(String area) throws ParseException {

        if (area.startsWith("CIRCLE")) {
            geometry = new GeofenceCircle(area);
        } else if (area.startsWith("POLYGON")) {
            geometry = new GeofencePolygon(area);
        } else if (area.startsWith("LINESTRING")) {
            final double distance = getDouble("polylineDistance");
            geometry = new GeofencePolyline(area, distance > 0 ? distance
                    : Context.getConfig().getDouble("geofence.polylineDistance", 25));
        } else {
            throw new ParseException("Unknown geometry type", 0);
        }

        this.area = area;
    }

    private GeofenceGeometry geometry;

    @QueryIgnore
    @JsonIgnore
    public GeofenceGeometry getGeometry() {
        return geometry;
    }

    @QueryIgnore
    @JsonIgnore
    public void setGeometry(GeofenceGeometry geometry) {
        area = geometry.toWkt();
        this.geometry = geometry;
    }
}
