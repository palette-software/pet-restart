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

## Simulate restarting Cache and Repository:

```
java -jar pet-restart-1.0.jar -rc -pg -s
```

# Switches

Switch | Arguments | Comments |
--- | --- |--- |
` -f,--force ` | | Disable JMX, send signals immediately (non-graceful).
`  --force-restart-timeout `| Seconds | Force restart timeout.
` -h,--help ` | | Show this help.
` --jmx-polling-time ` | Seconds | JMX data polling time.
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
` --wait `  | Seconds | Waiting time between jobs
` --wait-errors `  | Seconds | Waiting time after errors/retries

# Limitations

In current format, pet-restart only works on a single node configuration.
