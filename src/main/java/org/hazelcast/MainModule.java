/*
 * Copyright 2018 - 2019 Anton Tananaev (anton )
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.hazelcast.config.Config;
import org.hazelcast.config.Keys;
import org.hazelcast.database.AttributesManager;
import org.hazelcast.database.CalendarManager;
import org.hazelcast.database.DataManager;
import org.hazelcast.database.DeviceManager;
import org.hazelcast.database.GeofenceManager;
import org.hazelcast.database.IdentityManager;
import org.hazelcast.database.MaintenancesManager;
import org.hazelcast.database.StatisticsManager;
import org.hazelcast.geocoder.AddressFormat;
import org.hazelcast.geocoder.BanGeocoder;
import org.hazelcast.geocoder.BingMapsGeocoder;
import org.hazelcast.geocoder.FactualGeocoder;
import org.hazelcast.geocoder.GeocodeFarmGeocoder;
import org.hazelcast.geocoder.GeocodeXyzGeocoder;
import org.hazelcast.geocoder.Geocoder;
import org.hazelcast.geocoder.GisgraphyGeocoder;
import org.hazelcast.geocoder.GoogleGeocoder;
import org.hazelcast.geocoder.HereGeocoder;
import org.hazelcast.geocoder.MapQuestGeocoder;
import org.hazelcast.geocoder.MapmyIndiaGeocoder;
import org.hazelcast.geocoder.NominatimGeocoder;
import org.hazelcast.geocoder.OpenCageGeocoder;
import org.hazelcast.geocoder.PositionStackGeocoder;
import org.hazelcast.geocoder.TomTomGeocoder;
import org.hazelcast.geolocation.GeolocationProvider;
import org.hazelcast.geolocation.GoogleGeolocationProvider;
import org.hazelcast.geolocation.MozillaGeolocationProvider;
import org.hazelcast.geolocation.OpenCellIdGeolocationProvider;
import org.hazelcast.geolocation.UnwiredGeolocationProvider;
import org.hazelcast.handler.ComputedAttributesHandler;
import org.hazelcast.handler.CopyAttributesHandler;
import org.hazelcast.handler.DefaultDataHandler;
import org.hazelcast.handler.DistanceHandler;
import org.hazelcast.handler.EngineHoursHandler;
import org.hazelcast.handler.FilterHandler;
import org.hazelcast.handler.GeocoderHandler;
import org.hazelcast.handler.GeolocationHandler;
import org.hazelcast.handler.HemisphereHandler;
import org.hazelcast.handler.MotionHandler;
import org.hazelcast.handler.RemoteAddressHandler;
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
import org.hazelcast.reports.model.TripsConfig;

import javax.annotation.Nullable;
import javax.ws.rs.client.Client;
import io.netty.util.Timer;
import org.hazelcast.speedlimit.OverpassSpeedLimitProvider;
import org.hazelcast.speedlimit.SpeedLimitProvider;

public class MainModule extends AbstractModule {

    @Provides
    public static ObjectMapper provideObjectMapper() {
        return Context.getObjectMapper();
    }

    @Provides
    public static Config provideConfig() {
        return Context.getConfig();
    }

    @Provides
    public static DataManager provideDataManager() {
        return Context.getDataManager();
    }

    @Provides
    public static IdentityManager provideIdentityManager() {
        return Context.getIdentityManager();
    }

    @Provides
    public static Client provideClient() {
        return Context.getClient();
    }

    @Provides
    public static TripsConfig provideTripsConfig() {
        return Context.getTripsConfig();
    }

    @Provides
    public static DeviceManager provideDeviceManager() {
        return Context.getDeviceManager();
    }

    @Provides
    public static GeofenceManager provideGeofenceManager() {
        return Context.getGeofenceManager();
    }

    @Provides
    public static CalendarManager provideCalendarManager() {
        return Context.getCalendarManager();
    }

    @Provides
    public static AttributesManager provideAttributesManager() {
        return Context.getAttributesManager();
    }

    @Provides
    public static MaintenancesManager provideMaintenancesManager() {
        return Context.getMaintenancesManager();
    }

    @Singleton
    @Provides
    public static StatisticsManager provideStatisticsManager(
            Config config, DataManager dataManager, Client client, ObjectMapper objectMapper) {
        return new StatisticsManager(config, dataManager, client, objectMapper);
    }

    @Singleton
    @Provides
    public static Geocoder provideGeocoder(Config config) {
        if (config.getBoolean(Keys.GEOCODER_ENABLE)) {
            String type = config.getString(Keys.GEOCODER_TYPE, "google");
            String url = config.getString(Keys.GEOCODER_URL);
            String id = config.getString(Keys.GEOCODER_ID);
            String key = config.getString(Keys.GEOCODER_KEY);
            String language = config.getString(Keys.GEOCODER_LANGUAGE);
            String formatString = config.getString(Keys.GEOCODER_FORMAT);
            AddressFormat addressFormat = formatString != null ? new AddressFormat(formatString) : new AddressFormat();

            int cacheSize = config.getInteger(Keys.GEOCODER_CACHE_SIZE);
            switch (type) {
                case "nominatim":
                    return new NominatimGeocoder(url, key, language, cacheSize, addressFormat);
                case "gisgraphy":
                    return new GisgraphyGeocoder(url, cacheSize, addressFormat);
                case "mapquest":
                    return new MapQuestGeocoder(url, key, cacheSize, addressFormat);
                case "opencage":
                    return new OpenCageGeocoder(url, key, cacheSize, addressFormat);
                case "bingmaps":
                    return new BingMapsGeocoder(url, key, cacheSize, addressFormat);
                case "factual":
                    return new FactualGeocoder(url, key, cacheSize, addressFormat);
                case "geocodefarm":
                    return new GeocodeFarmGeocoder(key, language, cacheSize, addressFormat);
                case "geocodexyz":
                    return new GeocodeXyzGeocoder(key, cacheSize, addressFormat);
                case "ban":
                    return new BanGeocoder(cacheSize, addressFormat);
                case "here":
                    return new HereGeocoder(url, id, key, language, cacheSize, addressFormat);
                case "mapmyindia":
                    return new MapmyIndiaGeocoder(url, key, cacheSize, addressFormat);
                case "tomtom":
                    return new TomTomGeocoder(url, key, cacheSize, addressFormat);
                case "positionstack":
                    return new PositionStackGeocoder(key, cacheSize, addressFormat);
                default:
                    return new GoogleGeocoder(key, language, cacheSize, addressFormat);
            }
        }
        return null;
    }

    @Singleton
    @Provides
    public static GeolocationProvider provideGeolocationProvider(Config config) {
        if (config.getBoolean(Keys.GEOLOCATION_ENABLE)) {
            String type = config.getString(Keys.GEOLOCATION_TYPE, "mozilla");
            String url = config.getString(Keys.GEOLOCATION_URL);
            String key = config.getString(Keys.GEOLOCATION_KEY);
            switch (type) {
                case "google":
                    return new GoogleGeolocationProvider(key);
                case "opencellid":
                    return new OpenCellIdGeolocationProvider(url, key);
                case "unwired":
                    return new UnwiredGeolocationProvider(url, key);
                default:
                    return new MozillaGeolocationProvider(key);
            }
        }
        return null;
    }

    @Singleton
    @Provides
    public static SpeedLimitProvider provideSpeedLimitProvider(Config config) {
        if (config.getBoolean(Keys.SPEED_LIMIT_ENABLE)) {
            String type = config.getString(Keys.SPEED_LIMIT_TYPE, "overpass");
            String url = config.getString(Keys.SPEED_LIMIT_URL);
            switch (type) {
                case "overpass":
                default:
                    return new OverpassSpeedLimitProvider(url);
            }
        }
        return null;
    }

    @Singleton
    @Provides
    public static DistanceHandler provideDistanceHandler(Config config, IdentityManager identityManager) {
        return new DistanceHandler(config, identityManager);
    }

    @Singleton
    @Provides
    public static FilterHandler provideFilterHandler(Config config) {
        if (config.getBoolean(Keys.FILTER_ENABLE)) {
            return new FilterHandler(config);
        }
        return null;
    }

    @Singleton
    @Provides
    public static HemisphereHandler provideHemisphereHandler(Config config) {
        if (config.hasKey(Keys.LOCATION_LATITUDE_HEMISPHERE) || config.hasKey(Keys.LOCATION_LONGITUDE_HEMISPHERE)) {
            return new HemisphereHandler(config);
        }
        return null;
    }

    @Singleton
    @Provides
    public static RemoteAddressHandler provideRemoteAddressHandler(Config config) {
        if (config.getBoolean(Keys.PROCESSING_REMOTE_ADDRESS_ENABLE)) {
            return new RemoteAddressHandler();
        }
        return null;
    }

    @Singleton
    @Provides
    public static WebDataHandler provideWebDataHandler(
            Config config, IdentityManager identityManager, ObjectMapper objectMapper, Client client) {
        if (config.getBoolean(Keys.FORWARD_ENABLE)) {
            return new WebDataHandler(config, identityManager, objectMapper, client);
        }
        return null;
    }

    @Singleton
    @Provides
    public static GeolocationHandler provideGeolocationHandler(
            Config config, @Nullable GeolocationProvider geolocationProvider, StatisticsManager statisticsManager) {
        if (geolocationProvider != null) {
            return new GeolocationHandler(config, geolocationProvider, statisticsManager);
        }
        return null;
    }

    @Singleton
    @Provides
    public static GeocoderHandler provideGeocoderHandler(
            Config config, @Nullable Geocoder geocoder, IdentityManager identityManager,
            StatisticsManager statisticsManager) {
        if (geocoder != null) {
            return new GeocoderHandler(config, geocoder, identityManager, statisticsManager);
        }
        return null;
    }

    @Singleton
    @Provides
    public static SpeedLimitHandler provideSpeedLimitHandler(@Nullable SpeedLimitProvider speedLimitProvider) {
        if (speedLimitProvider != null) {
            return new SpeedLimitHandler(speedLimitProvider);
        }
        return null;
    }

    @Singleton
    @Provides
    public static MotionHandler provideMotionHandler(TripsConfig tripsConfig) {
        return new MotionHandler(tripsConfig.getSpeedThreshold());
    }

    @Singleton
    @Provides
    public static EngineHoursHandler provideEngineHoursHandler(Config config, IdentityManager identityManager) {
        if (config.getBoolean(Keys.PROCESSING_ENGINE_HOURS_ENABLE)) {
            return new EngineHoursHandler(identityManager);
        }
        return null;
    }

    @Singleton
    @Provides
    public static CopyAttributesHandler provideCopyAttributesHandler(Config config, IdentityManager identityManager) {
        if (config.getBoolean(Keys.PROCESSING_COPY_ATTRIBUTES_ENABLE)) {
            return new CopyAttributesHandler(identityManager);
        }
        return null;
    }

    @Singleton
    @Provides
    public static ComputedAttributesHandler provideComputedAttributesHandler(
            Config config, IdentityManager identityManager, AttributesManager attributesManager) {
        if (config.getBoolean(Keys.PROCESSING_COMPUTED_ATTRIBUTES_ENABLE)) {
            return new ComputedAttributesHandler(config, identityManager, attributesManager);
        }
        return null;
    }

    @Singleton
    @Provides
    public static TimeHandler provideTimeHandler(Config config) {
        if (config.hasKey(Keys.TIME_OVERRIDE)) {
            return new TimeHandler(config);
        }
        return null;
    }

    @Singleton
    @Provides
    public static DefaultDataHandler provideDefaultDataHandler(@Nullable DataManager dataManager) {
        if (dataManager != null) {
            return new DefaultDataHandler(dataManager);
        }
        return null;
    }

    @Singleton
    @Provides
    public static CommandResultEventHandler provideCommandResultEventHandler() {
        return new CommandResultEventHandler();
    }

    @Singleton
    @Provides
    public static OverspeedEventHandler provideOverspeedEventHandler(
            Config config, DeviceManager deviceManager, GeofenceManager geofenceManager) {
        return new OverspeedEventHandler(config, deviceManager, geofenceManager);
    }

    @Singleton
    @Provides
    public static FuelDropEventHandler provideFuelDropEventHandler(IdentityManager identityManager) {
        return new FuelDropEventHandler(identityManager);
    }

    @Singleton
    @Provides
    public static MotionEventHandler provideMotionEventHandler(
            IdentityManager identityManager, DeviceManager deviceManager, TripsConfig tripsConfig) {
        return new MotionEventHandler(identityManager, deviceManager, tripsConfig);
    }

    @Singleton
    @Provides
    public static GeofenceEventHandler provideGeofenceEventHandler(
            IdentityManager identityManager, GeofenceManager geofenceManager, CalendarManager calendarManager) {
        return new GeofenceEventHandler(identityManager, geofenceManager, calendarManager);
    }

    @Singleton
    @Provides
    public static AlertEventHandler provideAlertEventHandler(Config config, IdentityManager identityManager) {
        return new AlertEventHandler(config, identityManager);
    }

    @Singleton
    @Provides
    public static IgnitionEventHandler provideIgnitionEventHandler(IdentityManager identityManager) {
        return new IgnitionEventHandler(identityManager);
    }

    @Singleton
    @Provides
    public static MaintenanceEventHandler provideMaintenanceEventHandler(
            IdentityManager identityManager, MaintenancesManager maintenancesManager) {
        return new MaintenanceEventHandler(identityManager, maintenancesManager);
    }

    @Singleton
    @Provides
    public static DriverEventHandler provideDriverEventHandler(IdentityManager identityManager) {
        return new DriverEventHandler(identityManager);
    }

    @Singleton
    @Provides
    public static Timer provideTimer() {
        return GlobalTimer.getTimer();
    }

    @Override
    protected void configure() {
        binder().requireExplicitBindings();
    }

}
