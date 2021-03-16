/*
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

import com.hazelcast.internal.ascii.rest.HttpPostCommand;
import org.hazelcast.model.Event;
import org.hazelcast.model.Position;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public final class NotificatorSlack extends Notificator {

    private static final String USER_AGENT = "Mozilla/5.0";

    private static final String POST_URL = "https://hooks.slack.com/services/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";

    private String POST_DATA = "text=";

    @Override
    public void sendSync(long userId, Event event, Position position) {
        try {
            foo();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void foo() throws IOException {

        String url = "https://hooks.slack.com/services/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";

        URL UrlObj = new URL(url);

        HttpURLConnection connection = (HttpURLConnection) UrlObj.openConnection();


        connection.setRequestMethod("POST");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-type", "application/json");
        connection.setDoOutput(true);

        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

        String urlPostParameters = "text=something useful goes here";
        outputStream.writeBytes(urlPostParameters);

        outputStream.flush();
        outputStream.close();

        System.out.println("Send 'HTTP POST' request to : " + url);

        int responseCode = connection.getResponseCode();
        System.out.println("Response Code : " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader inputReader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = inputReader.readLine()) != null) {
                response.append(inputLine);
            }
            inputReader.close();

            System.out.println(response.toString());
        }
    }
}
