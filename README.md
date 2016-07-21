# Palette Enterprise Tabadmin Restart Plugin (pet-restart) [![Build Status](https://travis-ci.org/palette-software/pet-restart.svg?branch=master)](https://travis-ci.org/palette-software/pet-restart)

Tableau Server local service restarter Command-line interface utility.

# Installation

- Download and install JDK 1.8 and Maven if you have not installed them already.

http://www.oracle.com/technetwork/java/javase/downloads/index.html

https://maven.apache.org/download.cgi

- Download the source of pet-restart.

- Check your JAVA_HOME Environmental variable:

Right click My Computer and select Properties. Select Advanced tab, select Environment Variables. Check and if it is necessary create or edit JAVA_HOME to point to your javac.exe's directory e.g.: `C:\Program Files\Java\jdk1.8.0_91`
	
- Enable Balancer Manager in Tableau Server's Gateway configuration template (default location: `C:\Program Files\Tableau\Tableau Server\(version number)\templates\httpd.conf.templ` ). 

To do this, find this part

```
<Location /balancer-manager>
SetHandler balancer-manager
Require host 127.0.0.1
</Location>
```

and change the Require host part to this RequireAny version:

```
<Location /balancer-manager>
SetHandler balancer-manager
<RequireAny>
Require ip ::1
Require ip 127.0.0.1
</RequireAny>
</Location>
```

- Stop the server (from Tableau Server bin folder, e.g.: `C:\Program Files\Tableau\Tableau Server\9.3\bin`).

```
tabadmin stop
```

- Enable the JMX Ports if they are not yet enabled:

```
tabadmin set service.jmx_enabled true
```

- Configure then start again Tableau Server:

```
tabadmin configure
tabadmin start
```

- Compile pet-restart from it's source directory:

```
mvn clean package
```

- After a successful build, the JAR you have to use will be in a new directory called target. 

# Usage

## Show help:

```
java -jar pet-restart-1.0.jar
```

## Simulate restarting Cache and Repository:

```java -jar pet-restart-1.0.jar -rc -pg -s```

Simulation doesn't restart any of the processes, but rather goes through all the steps before an actual restart. It is <b>recommended</b> to run a simulation before issuing any restart commands to avoid possible failures. An example where JMX is disabled on vizqlserver:

```
C:\Users\palette\Java\pet-restart>java -jar target/pet-restart-1.1-SNAPSHOT.jar -s -r
Running simulation.
Restarting Repository
Restarting Cache Server(s)
There are 2 ports
Restarting Cache server at port 6379
Restarting Cache server at port 6380
Locating local-vizportal workers from balancer-manager
vizqlserver null http://localhost:8600
vizqlserver null http://localhost:8601
Restarting worker
Switching worker to Draining mode
Sending stop signal to process 164764
Switch worker to Non-disabled mode
Restart complete
Restarting worker
Switching worker to Draining mode
Sending stop signal to process 164764
Switch worker to Non-disabled mode
Restart complete
Locating vizqlserver-cluster workers from balancer-manager
JMX connection error.
Retrying after 60 seconds...
JMX connection error.
Retrying after 60 seconds...
JMX connection error.
Retrying after 60 seconds...
Failed to retrieve RMIServer stub: javax.naming.ServiceUnavailableException [Root exception is java.rmi.ConnectException: Connection refused to host: 192.168.224.137; nested exception is:
       java.net.ConnectException: Connection refused: connect]
```

## Gracefully restart VizQL Workers:

```
java -jar pet-restart-1.0.jar -rv
```

## Non-gracefully restart VizQL Workers as fast as possible:

```
java -jar pet-restart-1.0.jar -rv -f --wait 1
```

## Restart the Repository:

```
java -jar pet-restart-1.0.jar -pg
```

<<<<<<< HEAD
## Simulate restarting Cache and Repository:

```
java -jar pet-restart-1.0.jar -rc -pg -s
```

=======
>>>>>>> b7c62c7b96384864eff71aa14867f05740d4d451
# Switches

Switch | Arguments | Comments |
--- | --- |--- |
` -f,--force ` | | Disable JMX, send signals immediately (non-graceful).
`  --force-restart-timeout `| Seconds | Force restart timeout. Default is 240 seconds.
` -h,--help ` | | Show this help.
` --jmx-polling-time ` | Seconds | JMX data polling time. Default is 60 seconds.
` -pg,--reload-postgres ` | | Send reload signal to repository.
` -r,--restart ` | | Restart all processes one-by-one.
` -ra,--reload-apache ` | | Reload gateway rules.
` -rb,--restart-backgrounder ` | | Restart Backgrounder workers.
` -rc,--restart-cache ` | | Restart Cache Server.
` -rd,--restart-dataserver ` | | Restart Data Server workers.
` -rp,--restart-vizportal ` | | Restart Vizportal workers.
` -rv,--restart-vizql ` | |Restart VizQL workers.
` -s,--simulation ` | | Simulate all the restarts.
` --tabsvc-config-dir ` | Absolute path to directory | Path to tabsvc configs
` -v,--version ` | | Print version information.
` --wait `  | Seconds | Waiting time between jobs. Default is 30 seconds.
` --wait-errors `  | Seconds | Waiting time after errors/retries. Default is 60 seconds.

# Limitations

In current format, pet-restart only works on a single node configuration.
