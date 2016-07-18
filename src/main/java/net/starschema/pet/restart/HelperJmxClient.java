/*
The MIT License (MIT)
Copyright (c) 2016, Starschema Ltd

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

package net.starschema.pet.restart;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import java.io.IOException;
import java.util.Objects;

// JMX Client Helper to check and query Tableau's MBeans
class HelperJmxClient implements AutoCloseable {

    private JMXConnector jmxc;
    private JMXServiceURL url;

    HelperJmxClient() {
        this.jmxc = null;
        this.url = null;
    }

    //check if objectName a Tableau MBean
    boolean checkBeanExists(String objectName) throws Exception {

        //check the connection
        if (jmxc == null || url == null) {
            throw new Exception("Cannot check Mbean without connection");
        }
        MBeanServerConnection mbsc = getBeans();
        try {

            //try to invoke getPerformanceMetrics from objectName.
            // If it won't fail, objectName ins a valid Tableau MBean
            mbsc.invoke(new ObjectName(objectName), "getPerformanceMetrics", new Object[]{}, new String[]{});
        } catch (InstanceNotFoundException e) {
            return false;
        }
        return true;
    }

    //get the number of objectName's active sessions.
    String getActiveSessions(String objectName) throws Exception {
        return getPerformanceMetrics(objectName, "ActiveSessions");
    }

    //return a variable for the Tableu jmx server's getPerformanceMetrics.
    private String getPerformanceMetrics(String objectName, String variableName) throws Exception {
        MBeanServerConnection mbsc = getBeans();

        CompositeData invoked = (CompositeData) mbsc.invoke(new ObjectName(objectName), "getPerformanceMetrics", new Object[]{}, new String[]{});

        return (invoked.get(variableName).toString());
    }

    //close jmxc
    public void close() throws IOException {
        if (this.jmxc != null) {
            this.jmxc.close();
        }
    }

    //connect jmxc
    void connectService(String JMXServiceURL) throws Exception {

        int count = 0;
        String error = "";
        while (count++ < 3) {
            try {
                url = new JMXServiceURL(JMXServiceURL);
                jmxc = JMXConnectorFactory.connect(url, null);
                error = "";
                break;
            } catch (IOException e) {
                error = e.getMessage();
                Main.loggerStdOut.info("IO error:" + error + "\nRetrying after " + CliControl.WAIT_AFTER_ERROR + " seconds...");
                CliControl.sleep(CliControl.WAIT_AFTER_ERROR);
            }
        }
        if (!Objects.equals(error, "")) {
            throw new Exception(error);
        }

    }

    //Get MBeabns from jmxc
    private MBeanServerConnection getBeans() throws Exception {
        return jmxc.getMBeanServerConnection();
    }
}