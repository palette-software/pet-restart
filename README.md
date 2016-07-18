# pet-restart

Tableau local service restarter Command-line interface utility.

# Usage

## Show help:

```java -jar pet-restart-1.0.jar```

## Grafcefully restart VizQL Workers:

```java -jar pet-restart-1.0.jar -rv```


## Non-gracefully restart VizQL Workers as fast as possible:

```java -jar pet-restart-1.0.jar -rv -f --wait 1```

## Restart VizQL Workers :

```java -jar pet-restart-1.0.jar -pg```

## Simulate restarting Cache and Repository:

```java -jar pet-restart-1.0.jar -rc -rc -s```

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