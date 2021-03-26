/*
 * Copyright 2012 - 2017 Anton Tananaev (anton )
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
package org.hazelcast.database;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.crdt.pncounter.PNCounter;
import com.hazelcast.map.IMap;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hazelcast.config.Config;
import org.hazelcast.Context;
import org.hazelcast.helper.DateUtil;
import org.hazelcast.model.Attribute;
import org.hazelcast.model.Device;
import org.hazelcast.model.Driver;
import org.hazelcast.model.Event;
import org.hazelcast.model.Geofence;
import org.hazelcast.model.Group;
import org.hazelcast.model.Maintenance;
import org.hazelcast.model.ManagedUser;
import org.hazelcast.model.Notification;
import org.hazelcast.model.Permission;
import org.hazelcast.model.BaseModel;
import org.hazelcast.model.Calendar;
import org.hazelcast.model.Command;
import org.hazelcast.model.Position;
import org.hazelcast.model.Server;
import org.hazelcast.model.Statistics;
import org.hazelcast.model.User;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DataManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataManager.class);

    public static final String ACTION_SELECT_ALL = "selectAll";
    public static final String ACTION_SELECT = "select";
    public static final String ACTION_INSERT = "insert";
    public static final String ACTION_UPDATE = "update";
    public static final String ACTION_DELETE = "delete";

    private final Config config;

    // Hazelcast Instance
    private static HazelcastInstance hz;

    private DataSource dataSource;

    private boolean generateQueries;

    private boolean forceLdap;

    public DataManager(Config config) throws Exception {
        this.config = config;

        forceLdap = config.getBoolean("ldap.force");

        // Hazelcast
        initHazelcast();

        // Database
        initDatabase();
        initDatabaseSchema();
    }

    public HazelcastInstance getHazelcastInstance() { return this.hz; }

    private void initHazelcast() {
        // Start Hazelcast Instance using the hazelcast.xml from resources / classpath
        com.hazelcast.config.Config cfg = new ClasspathXmlConfig("hazelcast.xml");

        // display our cluster name to verify the configuration file being used
        System.out.println("\n\n\t\t"+cfg.getClusterName()+"\n\n");
        // Enterprise License Key
        cfg.setLicenseKey("ENT#10Nodes#dOfy209WBgUNnZQkiM6l1TEKSYCbJjAPXwu8G5qDmH14000101021390101101100011010008100901109101");


        hz = Hazelcast.newHazelcastInstance(cfg);
    }

    private void initDatabase() throws Exception {

        String jndiName = config.getString("database.jndi");

        if (jndiName != null) {

            dataSource = (DataSource) new InitialContext().lookup(jndiName);

        } else {

            String driverFile = config.getString("database.driverFile");
            if (driverFile != null) {
                ClassLoader classLoader = ClassLoader.getSystemClassLoader();
                try {
                    Method method = classLoader.getClass().getDeclaredMethod("addURL", URL.class);
                    method.setAccessible(true);
                    method.invoke(classLoader, new File(driverFile).toURI().toURL());
                } catch (NoSuchMethodException e) {
                    Method method = classLoader.getClass()
                            .getDeclaredMethod("appendToClassPathForInstrumentation", String.class);
                    method.setAccessible(true);
                    method.invoke(classLoader, driverFile);
                }
            }

            String driver = config.getString("database.driver");
            if (driver != null) {
                Class.forName(driver);
            }

            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setDriverClassName(config.getString("database.driver"));
            hikariConfig.setJdbcUrl(config.getString("database.url"));
            hikariConfig.setUsername(config.getString("database.user"));
            hikariConfig.setPassword(config.getString("database.password"));
            hikariConfig.setConnectionInitSql(config.getString("database.checkConnection", "SELECT 1"));
            hikariConfig.setIdleTimeout(600000);

            int maxPoolSize = config.getInteger("database.maxPoolSize");

            if (maxPoolSize != 0) {
                hikariConfig.setMaximumPoolSize(maxPoolSize);
            }

            generateQueries = config.getBoolean("database.generateQueries");

            dataSource = new HikariDataSource(hikariConfig);

        }
    }

    public static String constructObjectQuery(String action, Class<?> clazz, boolean extended) {
        switch (action) {
            case ACTION_INSERT:
            case ACTION_UPDATE:
                StringBuilder result = new StringBuilder();
                StringBuilder fields = new StringBuilder();
                StringBuilder values = new StringBuilder();

                Set<Method> methods = new HashSet<>(Arrays.asList(clazz.getMethods()));
                methods.removeAll(Arrays.asList(Object.class.getMethods()));
                methods.removeAll(Arrays.asList(BaseModel.class.getMethods()));
                for (Method method : methods) {
                    boolean skip;
                    if (extended) {
                        skip = !method.isAnnotationPresent(QueryExtended.class);
                    } else {
                        skip = method.isAnnotationPresent(QueryIgnore.class)
                                || method.isAnnotationPresent(QueryExtended.class) && !action.equals(ACTION_INSERT);
                    }
                    if (!skip && method.getName().startsWith("get") && method.getParameterTypes().length == 0) {
                        String name = Introspector.decapitalize(method.getName().substring(3));
                        if (action.equals(ACTION_INSERT)) {
                            fields.append(name).append(", ");
                            values.append(":").append(name).append(", ");
                        } else {
                            fields.append(name).append(" = :").append(name).append(", ");
                        }
                    }
                }
                fields.setLength(fields.length() - 2);
                if (action.equals(ACTION_INSERT)) {
                    values.setLength(values.length() - 2);
                    result.append("INSERT INTO ").append(getObjectsTableName(clazz)).append(" (");
                    result.append(fields).append(") ");
                    result.append("VALUES (").append(values).append(")");
                } else {
                    result.append("UPDATE ").append(getObjectsTableName(clazz)).append(" SET ");
                    result.append(fields);
                    result.append(" WHERE id = :id");
                }
                return result.toString();
            case ACTION_SELECT_ALL:
                return "SELECT * FROM " + getObjectsTableName(clazz);
            case ACTION_SELECT:
                return "SELECT * FROM " + getObjectsTableName(clazz) + " WHERE id = :id";
            case ACTION_DELETE:
                return "DELETE FROM " + getObjectsTableName(clazz) + " WHERE id = :id";
            default:
                throw new IllegalArgumentException("Unknown action");
        }
    }

    public static String constructPermissionQuery(String action, Class<?> owner, Class<?> property) {
        switch (action) {
            case ACTION_SELECT_ALL:
                return "SELECT " + makeNameId(owner) + ", " + makeNameId(property) + " FROM "
                        + getPermissionsTableName(owner, property);
            case ACTION_INSERT:
                return "INSERT INTO " + getPermissionsTableName(owner, property)
                        + " (" + makeNameId(owner) + ", " + makeNameId(property) + ") VALUES (:"
                        + makeNameId(owner) + ", :" + makeNameId(property) + ")";
            case ACTION_DELETE:
                return "DELETE FROM " + getPermissionsTableName(owner, property)
                        + " WHERE " + makeNameId(owner) + " = :" + makeNameId(owner)
                        + " AND " + makeNameId(property) + " = :" + makeNameId(property);
            default:
                throw new IllegalArgumentException("Unknown action");
        }
    }

    private String getQuery(String key) {
        String query = config.getString(key);
        if (query == null) {
            LOGGER.info("Query not provided: " + key);
        }
        return query;
    }

    public String getQuery(String action, Class<?> clazz) {
        return getQuery(action, clazz, false);
    }

    public String getQuery(String action, Class<?> clazz, boolean extended) {
        String queryName;
        if (action.equals(ACTION_SELECT_ALL)) {
            queryName = "database.select" + clazz.getSimpleName() + "s";
        } else {
            queryName = "database." + action.toLowerCase() + clazz.getSimpleName();
            if (extended) {
                queryName += "Extended";
            }
        }
        String query = config.getString(queryName);
        if (query == null) {
            if (generateQueries) {
                query = constructObjectQuery(action, clazz, extended);
                config.setString(queryName, query);
            } else {
                LOGGER.info("Query not provided: " + queryName);
            }
        }

        return query;
    }

    public String getQuery(String action, Class<?> owner, Class<?> property) {
        String queryName;
        switch (action) {
            case ACTION_SELECT_ALL:
                queryName = "database.select" + owner.getSimpleName() + property.getSimpleName() + "s";
                break;
            case ACTION_INSERT:
                queryName = "database.link" + owner.getSimpleName() + property.getSimpleName();
                break;
            default:
                queryName = "database.unlink" + owner.getSimpleName() + property.getSimpleName();
                break;
        }
        String query = config.getString(queryName);
        if (query == null) {
            if (generateQueries) {
                query = constructPermissionQuery(action, owner,
                        property.equals(User.class) ? ManagedUser.class : property);
                config.setString(queryName, query);
            } else {
                LOGGER.info("Query not provided: " + queryName);
            }
        }

        return query;
    }

    private static String getPermissionsTableName(Class<?> owner, Class<?> property) {
        String propertyName = property.getSimpleName();
        if (propertyName.equals("ManagedUser")) {
            propertyName = "User";
        }
        return "tc_" + Introspector.decapitalize(owner.getSimpleName())
                + "_" + Introspector.decapitalize(propertyName);
    }

    private static String getObjectsTableName(Class<?> clazz) {
        String result = "tc_" + Introspector.decapitalize(clazz.getSimpleName());
        // Add "s" ending if object name is not plural already
        if (!result.endsWith("s")) {
            result += "s";
        }
        return result;
    }

    private void initDatabaseSchema() throws SQLException, LiquibaseException {

        if (config.hasKey("database.changelog")) {

            ResourceAccessor resourceAccessor = new FileSystemResourceAccessor();

            Database database = DatabaseFactory.getInstance().openDatabase(
                    config.getString("database.url"),
                    config.getString("database.user"),
                    config.getString("database.password"),
                    config.getString("database.driver"),
                    null, null, null, resourceAccessor);

            Liquibase liquibase = new Liquibase(
                    config.getString("database.changelog"), resourceAccessor, database);

            liquibase.clearCheckSums();

            liquibase.update(new Contexts());
        }
    }

    public User login(String email, String password) throws SQLException {
        User user = QueryBuilder.create(dataSource, getQuery("database.loginUser"))
                .setString("email", email.trim())
                .executeQuerySingle(User.class);
        LdapProvider ldapProvider = Context.getLdapProvider();
        if (user != null) {
            if (ldapProvider != null && user.getLogin() != null && ldapProvider.login(user.getLogin(), password)
                    || !forceLdap && user.isPasswordValid(password)) {
                return user;
            }
        } else {
            if (ldapProvider != null && ldapProvider.login(email, password)) {
                user = ldapProvider.getUser(email);
                Context.getUsersManager().addItem(user);
                return user;
            }
        }
        return null;
    }

    public void updateDeviceStatus(Device device) throws SQLException {
        QueryBuilder.create(dataSource, getQuery(ACTION_UPDATE, Device.class, true))
                .setObject(device)
                .executeUpdate();
    }

    public Collection<Position> getPositions(long deviceId, Date from, Date to) throws SQLException {
        return QueryBuilder.create(dataSource, getQuery("database.selectPositions"))
                .setLong("deviceId", deviceId)
                .setDate("from", from)
                .setDate("to", to)
                .executeQuery(Position.class);
    }

    public void updateLatestPosition(Position position) throws SQLException {
        QueryBuilder.create(dataSource, getQuery("database.updateLatestPosition"))
                .setDate("now", new Date())
                .setObject(position)
                .executeUpdate();
    }

    public Collection<Position> getLatestPositions() throws SQLException {
        return QueryBuilder.create(dataSource, getQuery("database.selectLatestPositions"))
                .executeQuery(Position.class);
    }

    public void clearHistory() throws SQLException {
        long historyDays = config.getInteger("database.historyDays");
        if (historyDays != 0) {
            Date timeLimit = new Date(System.currentTimeMillis() - historyDays * 24 * 3600 * 1000);
            LOGGER.info("Clearing history earlier than " + DateUtil.formatDate(timeLimit, false));
            QueryBuilder.create(dataSource, getQuery("database.deletePositions"))
                    .setDate("serverTime", timeLimit)
                    .executeUpdate();
            QueryBuilder.create(dataSource, getQuery("database.deleteEvents"))
                    .setDate("serverTime", timeLimit)
                    .executeUpdate();
        }
    }

    public Server getServer() throws SQLException {
        return QueryBuilder.create(dataSource, getQuery(ACTION_SELECT_ALL, Server.class))
                .executeQuerySingle(Server.class);
    }

    public Collection<Event> getEvents(long deviceId, Date from, Date to) throws SQLException {
        return QueryBuilder.create(dataSource, getQuery("database.selectEvents"))
                .setLong("deviceId", deviceId)
                .setDate("from", from)
                .setDate("to", to)
                .executeQuery(Event.class);
    }

    public Collection<Statistics> getStatistics(Date from, Date to) throws SQLException {
        return QueryBuilder.create(dataSource, getQuery("database.selectStatistics"))
                .setDate("from", from)
                .setDate("to", to)
                .executeQuery(Statistics.class);
    }

    public static Class<?> getClassByName(String name) throws ClassNotFoundException {
        switch (name.toLowerCase().replace("id", "")) {
            case "device":
                return Device.class;
            case "group":
                return Group.class;
            case "user":
                return User.class;
            case "manageduser":
                return ManagedUser.class;
            case "geofence":
                return Geofence.class;
            case "driver":
                return Driver.class;
            case "attribute":
                return Attribute.class;
            case "calendar":
                return Calendar.class;
            case "command":
                return Command.class;
            case "maintenance":
                return Maintenance.class;
            case "notification":
                return Notification.class;
            default:
                throw new ClassNotFoundException();
        }
    }

    private static String makeNameId(Class<?> clazz) {
        String name = clazz.getSimpleName();
        return Introspector.decapitalize(name) + (!name.contains("Id") ? "Id" : "");
    }

    public Collection<Permission> getPermissions(Class<? extends BaseModel> owner, Class<? extends BaseModel> property)
            throws SQLException, ClassNotFoundException {
        return QueryBuilder.create(dataSource, getQuery(ACTION_SELECT_ALL, owner, property))
                .executePermissionsQuery();
    }

    public void linkObject(Class<?> owner, long ownerId, Class<?> property, long propertyId, boolean link)
            throws SQLException {
        System.out.println("\tDataManager::linkObject\t\t"+property.getName()+"\t"+propertyId);

        QueryBuilder.create(dataSource, getQuery(link ? ACTION_INSERT : ACTION_DELETE, owner, property))
                .setLong(makeNameId(owner), ownerId)
                .setLong(makeNameId(property), propertyId)
                .executeUpdate();
    }

    public <T extends BaseModel> T getObject(Class<T> clazz, long entityId) throws SQLException {
        System.out.println("\tDataManager::getObject\t\t"+clazz.getName()+"\t"+entityId);

        return QueryBuilder.create(dataSource, getQuery(ACTION_SELECT, clazz))
                .setLong("id", entityId)
                .executeQuerySingle(clazz);
    }

    public <T extends BaseModel> Collection<T> getObjects(Class<T> clazz) throws SQLException {
        return QueryBuilder.create(dataSource, getQuery(ACTION_SELECT_ALL, clazz))
                .executeQuery(clazz);
    }


    public void addObject(BaseModel entity) throws SQLException {
        System.out.println("\tDataManager::addObject\t\t"+entity.getClass().getName());

        // add object to database
        entity.setId(QueryBuilder.create(dataSource, getQuery(ACTION_INSERT, entity.getClass()), true)
                .setObject(entity)
                .executeUpdate());

        // add object to hazelcast
        try {
            IMap entityMap = hz.getMap(entity.getClass().getName());
            entityMap.putAsync(entity.getId(), entity);
        }catch (Exception e){
            System.out.println( e.getMessage() );
        }
    }

    public void updateObject(BaseModel entity) throws SQLException {
        System.out.println("\tDataManager::updateObject\t\t"+entity.getClass().getName());
        QueryBuilder.create(dataSource, getQuery(ACTION_UPDATE, entity.getClass()))
                .setObject(entity)
                .executeUpdate();
        if (entity instanceof User && ((User) entity).getHashedPassword() != null) {
            QueryBuilder.create(dataSource, getQuery(ACTION_UPDATE, User.class, true))
                    .setObject(entity)
                    .executeUpdate();
        }
    }

    public void removeObject(Class<? extends BaseModel> clazz, long entityId) throws SQLException {
        System.out.println("\tDataManager::removeObject\t\t"+clazz.getName()+"\t"+entityId);
        QueryBuilder.create(dataSource, getQuery(ACTION_DELETE, clazz))
                .setLong("id", entityId)
                .executeUpdate();
    }

}
