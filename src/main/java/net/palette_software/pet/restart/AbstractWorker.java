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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

abstract class AbstractWorker implements Worker {

    /**
     * @return the process name in tasklist
     */
    public abstract String getWindowsProcessName();

    /**
     * returns the pid(s) of task(s) of a given executable(s)
     * @param multiple if false, only one pid will be return.
     * @param search_process_regex string represents to identify tasks in Wmic.
     * @return pid(s)
     */
    List<Integer> getProcessIdHelper(boolean multiple, String search_process_regex) throws Exception {
        Pattern pattern = Pattern.compile(search_process_regex, Pattern.MULTILINE | Pattern.DOTALL);
        List<Integer> pids = new ArrayList<>();
        if (!multiple) {
            int pid = HelperWindowsTask.searchForPidInWmic(getWindowsProcessName(), pattern);
            pids.add(pid);
        } else {
            pids = HelperWindowsTask.searchForPidsInWmic(getWindowsProcessName(), pattern);
        }
        if (pids.size() < 1) {
            throw new Exception("Cannot find PID of the worker");
        }
        return pids;
    }
}
