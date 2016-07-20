/*
The MIT License (MIT)
Copyright (c) 2016, Palette Software

Permission is hereby granted, free of charge, to any person obtaining a copy of this
software and associated documentation files (the "Software"), to deal in the Software
without restriction, including without limitation the rights to use, copy, modify,
merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to the following
conditions:

The above copyright notice and this permission notice shall be included in all copies
or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package net.palette_software.pet.restart;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class HelperHttpClient {

    //accepted http post keys to interact with Balancer Manager
    private enum BalancerManagerAcceptedPostKeys {
        w_status_N,
        w_status_D
    }

    //check given string is acceptable to interact with Balancer Manager.
    private static boolean BalancerManagerAcceptedPostKeysContains(String needle) {
        for (BalancerManagerAcceptedPostKeys c : BalancerManagerAcceptedPostKeys.values()) {
            if (c.name().equals(needle)) {
                return true;
            }
        }
        return false;
    }

    //get targetURL's HTML
    static String getPage(String targetURL) throws Exception {
        Request x = Request.Get(targetURL);
        Response y = x.execute();
        Content z = y.returnContent();
        return z.toString();
    }

    //Modify a Balancer manager managed worker's status in mod_balancer using http request(s)
    static void modifyWorker(String targetURL, BalancerManagerManagedWorker w, HashMap<String, Integer> switches, int simulation) throws Exception {
        try (
                CloseableHttpClient client = HttpClients.createDefault()
        ) {

            if (!(simulation < 1)) {
                return;
            }

            //connect to Balancer Manager
            HttpPost httpPost = new HttpPost(targetURL);

            List<NameValuePair> params = new ArrayList<>();

            //check the switches map for the keys are acceptable to interact with Balancer Manager
            for (Map.Entry<String, Integer> entry : switches.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue().toString();
                if (!BalancerManagerAcceptedPostKeysContains(key)) {
                    HelperLogger.loggerStdOut.info("Bad key in modifyWorker (" + key + "," + value + ")");
                    continue;
                }
                params.add(new BasicNameValuePair(key, value));
            }

            //add the necessary parameters
            params.add(new BasicNameValuePair("b", w.getBalancerMemberName()));
            params.add(new BasicNameValuePair("w", w.getName()));
            params.add(new BasicNameValuePair("nonce", w.getNonce()));
            httpPost.setEntity(new UrlEncodedFormEntity(params));

            //send the request
            CloseableHttpResponse response = client.execute(httpPost);

            //throw an exception is the result status is abnormal.
            int responseCode = response.getStatusLine().getStatusCode();
            if (200 != responseCode) {
                throw new Exception("Balancer-manager returned a http response of " + responseCode);
            }
        }
    }

    //get Workers from the given cluster from Balancer Manager.
    static List<BalancerManagerManagedWorker> getWorkersFromHtml(String body, String clusterName, String jmxObjectName) throws Exception {

        String regex;
        Pattern p;
        String nonce = "";
        String[] bodySlpit;
        Matcher m;
        int jmxPort;

        //search for the cluster's nonce string
        regex = "<h3>.*&nonce=([^\"]+)\">balancer://" + clusterName + "</a>.*";
        p = Pattern.compile(regex, Pattern.MULTILINE | Pattern.DOTALL);
        bodySlpit = body.split("\n");
        for (String s : bodySlpit) {
            m = p.matcher(s);
            if (m.matches()) {
                nonce = m.group(1);
                break;
            }
        }

        if (nonce.equals("")) {
            throw new Exception("Cannot found the worker load balancer in balancer-manager");
        }

        List<BalancerManagerManagedWorker> workers = new ArrayList<>();

        //Search for the Workers' name
        for (String s : bodySlpit) {
            regex = "<td><a href=\"/balancer-manager\\?b=" + clusterName + "&w=([^&]+)&nonce=" + nonce + "[^<]*</a></td><td>([^<]+)?.*";
            p = Pattern.compile(regex, Pattern.MULTILINE | Pattern.DOTALL);
            m = p.matcher(s);
            if (m.matches()) {
                String memberName = m.group(1);
                String route = m.group(2);

                regex = ".*:([0-9]+)";
                p = Pattern.compile(regex, Pattern.MULTILINE | Pattern.DOTALL);
                m = p.matcher(memberName);

                if (!m.matches()) {
                    throw new Exception("Cannot found the workers' port");
                }

                //check the jmx ports if we need it
                if (!CliControl.FORCE_RESTARTS && !Objects.equals(jmxObjectName, "")) {

                    //calculate JMX port
                    jmxPort = Integer.parseInt(m.group(1)) + 300;

                    //check if port exists
                    try (
                            HelperJmxClient jmxClient = new HelperJmxClient()
                    ) {

                        int count = 0;
                        String error = "";
                        while (count++ < 3) {
                            String jMXServiceURL = "service:jmx:rmi:///jndi/rmi://:" + jmxPort + "/jmxrmi";
                            jmxClient.connectService(jMXServiceURL);
                            if (!jmxClient.checkBeanExists(jmxObjectName)) {
                                error = "Cannot found the required MBean " + jMXServiceURL + ":" + jmxObjectName;
                                HelperLogger.loggerStdOut.info(error + "\nRetrying after " + CliControl.WAIT_AFTER_ERROR + " seconds...");
                                CliControl.sleep(CliControl.WAIT_AFTER_ERROR);
                            } else {
                                error = "";
                                break;
                            }
                        }
                        if (!Objects.equals(error, "")) {
                            throw new Exception(error);
                        }

                    }
                } else {
                    jmxPort = -1;
                }

                //add Worker into workers
                if (Objects.equals(clusterName, "vizqlserver-cluster")) {
                    workers.add(new WorkerVizql(memberName, route, nonce, jmxPort));
                } else if (Objects.equals(clusterName, "dataserver-cluster")) {
                    workers.add(new WorkerDataServer(memberName, route, nonce, jmxPort));
                } else if (Objects.equals(clusterName, "local-vizportal")) {
                    workers.add(new WorkerVizportal(memberName, route, nonce, jmxPort));
                }
            }
        }
        return workers;
    }
}
