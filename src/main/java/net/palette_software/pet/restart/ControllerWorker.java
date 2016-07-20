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

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

class ControllerWorker {

    //set apache balancer member status to drain
    static void drain(BalancerManagerManagedWorker w, int simulation) throws Exception {
        HelperHttpClient.modifyWorker(
                CliControl.BALANCER_MANAGER_URL,
                w,
                new HashMap<String, Integer>() {{
                    put("w_status_N", 1);
                }},
                simulation
        );
    }

    //set apache balancer member status to non-disabled, non-drained
    static void reset(BalancerManagerManagedWorker w, int simulation) throws Exception {
        HelperHttpClient.modifyWorker(
                CliControl.BALANCER_MANAGER_URL,
                w,
                new HashMap<String, Integer>() {{
                    put("w_status_N", 0);
                    put("w_status_D", 0);
                }},
                simulation
        );
    }

    //kill a Worker
    static void kill(Worker w, int simulation) throws Exception {
        int pid = w.getProcessId(false).get(0);
        if (pid < 1) {
            throw new Exception("Wrong PID: " + pid);
        }
        HelperWindowsTask.killProcessByPid(pid, simulation);
    }

    //kill a list of Workers
    static void killAll(Worker w, int simulation) throws Exception {
        for (int pid : w.getProcessId(true)) {
            if (pid < 1) {
                throw new Exception("Wrong PID: " + pid);
            }
            HelperWindowsTask.killProcessByPid(pid, simulation);
        }
    }

    //restart the Cache Server via tcp socket
    static void restartCacheServer(String pw, int port, int simulation) throws Exception {
        if (simulation < 1) {
            try (
                    Socket clientSocket = new Socket("localhost", port)
            ) {
                DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                outToServer.writeBytes("AUTH " + pw + '\n' + "SHUTDOWN SAVE" + '\n');
            } catch (IOException e) {
                throw new Exception("Socket error: " + e.getMessage());
            }
        }
    }

    //restart Postgre Server from cli
    static void RestartPostgreServer(String app_path, String data_dir, int simulation) throws Exception {
        if (simulation < 1) {
            Runtime.getRuntime().exec(app_path + " stop -D \"" + data_dir + "\" -w ");
        }
    }

}