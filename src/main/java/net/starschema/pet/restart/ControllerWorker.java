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

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

class ControllerWorker {

    static void drain(BalancerManagerManagedWorker w) throws Exception {
        HttpClientHelper.modifyWorker(
                CliControl.BALANCER_MANAGER_URL,
                w,
                new HashMap<String, Integer>() {{
                    put("w_status_N", 1);
                }}
        );
    }

    static void reset(BalancerManagerManagedWorker w) throws Exception {
        HttpClientHelper.modifyWorker(
                CliControl.BALANCER_MANAGER_URL,
                w,
                new HashMap<String, Integer>() {{
                    put("w_status_N", 0);
                    put("w_status_D", 0);
                }}
        );
    }

    static void kill(Worker w) throws Exception {
        int pid = w.getProcessId(false).get(0);
        if (pid < 1) {
            throw new Exception("Wrong PID: " + pid);
        }
        HelperWindowsTask.killProcessByPid(pid);
    }

    static void killAll(Worker w) throws Exception {
        for (int pid : w.getProcessId(true)) {
            if (pid < 1) {
                throw new Exception("Wrong PID: " + pid);
            }
            HelperWindowsTask.killProcessByPid(pid);
        }
    }

    static void restartCacheServer(String pw, int port) throws Exception {

        if (Main.SIMULATION < 1) {

            try (Socket clientSocket = new Socket("localhost", port)) {
                DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                outToServer.writeBytes("AUTH " + pw + '\n' + "SHUTDOWN SAVE" + '\n');
            } catch (IOException e) {
                throw new Exception("Socket error: " + e.getMessage());
            }
        }
    }

    static void RestartPostgreServer(String app_path, String data_dir) throws Exception {
        if (Main.SIMULATION < 1) {
            Runtime.getRuntime().exec(app_path + " stop -D \"" + data_dir + "\" -w ");
        }
    }

}