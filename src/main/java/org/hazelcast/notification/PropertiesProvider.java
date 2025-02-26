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
package org.hazelcast.notification;

import org.hazelcast.config.Config;
import org.hazelcast.model.ExtendedModel;

public class PropertiesProvider {

    private Config config;

    private ExtendedModel extendedModel;

    public PropertiesProvider(Config config) {
        this.config = config;
    }

    public PropertiesProvider(ExtendedModel extendedModel) {
        this.extendedModel = extendedModel;
    }

    public String getString(String key) {
        if (config != null) {
            return config.getString(key);
        } else {
            return extendedModel.getString(key);
        }
    }

    public String getString(String key, String defaultValue) {
        String value = getString(key);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    public int getInteger(String key, int defaultValue) {
        if (config != null) {
            return config.getInteger(key, defaultValue);
        } else {
            Object result = extendedModel.getAttributes().get(key);
            if (result != null) {
                return result instanceof String ? Integer.parseInt((String) result) : (Integer) result;
            } else {
                return defaultValue;
            }
        }
    }

    public Boolean getBoolean(String key) {
        if (config != null) {
            if (config.hasKey(key)) {
                return config.getBoolean(key);
            } else {
                return null;
            }
        } else {
            Object result = extendedModel.getAttributes().get(key);
            if (result != null) {
                return result instanceof String ? Boolean.valueOf((String) result) : (Boolean) result;
            } else {
                return null;
            }
        }
    }

}
