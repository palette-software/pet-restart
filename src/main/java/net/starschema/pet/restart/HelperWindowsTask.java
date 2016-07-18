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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class HelperWindowsTask {

    public HelperWindowsTask() throws Exception {
    }

    static void killProcessByPid(int toKill) throws Exception {

        if (Main.SIMULATION < 1) {
            String cmd = CliControl.TASK_KILLER + " " + toKill;
            Runtime.getRuntime().exec(cmd);
        }
    }

    static int searchForPidInWmic(String windows_process_name, Pattern pattern) throws Exception {
        String line;
        String cmd = System.getenv("windir") + "\\system32\\wbem\\wmic.exe " +
                " process where \"name='" + windows_process_name + "'\" get Processid, Commandline";
        Main.loggerFile.info("exec: " + cmd);
        Process p = Runtime.getRuntime().exec(cmd);
        BufferedReader input =
                new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((line = input.readLine()) != null) {
            Matcher m = pattern.matcher(line);
            if (m.matches()) {
                input.close();
                return Integer.parseInt(m.group(2));
            }
        }
        input.close();
        return -1;
    }

    static List<Integer> searchForPidsInWmic(String windows_process_name, Pattern pattern) throws Exception {
        String line;
        List<Integer> ports = new ArrayList<>();

        Process p = Runtime.getRuntime().exec
                (System.getenv("windir") + "\\system32\\wbem\\wmic.exe " +
                        " process where \"name='" + windows_process_name + "'\" get Processid, Commandline");
        BufferedReader input =
                new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((line = input.readLine()) != null) {
            Matcher m = pattern.matcher(line);
            if (m.matches()) {
                ports.add(Integer.parseInt(m.group(2)));
            }
        }
        input.close();
        return ports;
    }
}
