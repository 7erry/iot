/*
 * Copyright 2016 Anton Tananaev (anton@traccar.org)
 * Copyright 2016 Andrey Kunitsyn (andrey@traccar.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

Ext.define('Traccar.model.ReportSummary', {
    extend: 'Ext.data.Model',
    identifier: 'negative',

    fields: [{
        name: 'deviceId',
        type: 'int'
    }, {
        name: 'deviceName',
        type: 'string'
    }, {
        name: 'maxSpeed',
        type: 'float',
        convert: Traccar.AttributeFormatter.getConverter('speed')
    }, {
        name: 'averageSpeed',
        type: 'float',
        convert: Traccar.AttributeFormatter.getConverter('speed')
    }, {
        name: 'distance',
        type: 'float',
        convert: Traccar.AttributeFormatter.getConverter('distance')
    }, {
        name: 'startOdometer',
        type: 'float',
        convert: Traccar.AttributeFormatter.getConverter('distance')
    }, {
        name: 'endOdometer',
        type: 'float',
        convert: Traccar.AttributeFormatter.getConverter('distance')
    }, {
        name: 'engineHours',
        type: 'int'
    }, {
        name: 'spentFuel',
        type: 'float',
        convert: Traccar.AttributeFormatter.getConverter('spentFuel')
    }]
});
