# wildfly-microprofile-health

[WildFly][wildfly] Extension for [Eclipse MicroProfile Health][microprofile-health].

# Instructions

* Compile and install the [Eclipse MicroProfile Health][microprofile-health] project.
* Compile and install this project:

```
mvn clean install
```

# Project structure

* [extension](extension/) - WildFly Extension that provides the `microprofile-health` subsystem.
* [feature-pack](feature-pack/) - Feature pack that bundles the extension with the JBoss Modules required to run it in WildFly.
* [dist](dist/) - A distribution of WildFly with the microprofile-health extension installed (in its standalone-microprofile.xml configuration)
* [examples](examples/) - Examples of an applications that provides Health Check Procedures

# Usage

Start WildFly with the microprofile-health extension installed:

```
$> export JBOSS_HOME=dist/target/microprofile-health-x-x-x
$> cd $JBOSS_HOME
$> ./bin/standalone.sh -c standalone-microprofile.xml
...
13:45:33,808 INFO  [org.wildfly.extension.microprofile.health] (ServerService Thread Pool -- 47) EMPHEALTH0001: Activating Eclipse MicroProfile Health Subsystem
...
13:45:35,010 INFO  [org.jboss.as] (Controller Boot Thread) WFLYSRV0025: WildFly Core 3.0.0.Beta11 "Kenny" started in 3507ms - Started 246 of 292 services (88 services are lazy, passive or on-demand)
```

You can perform a health check using WildFly CLI Console by invoking the `/subsystem=microprofile-health:check` operation

```

$> $JBOSS_HOME/bin/jboss-cli.sh -c
[standalone@localhost:9990 /] /subsystem=microprofile-health:check
{
    "outcome" => "success",
    "result" => {
        "checks" => [{
            "id" => "heap-memory",
            "result" => "UP",
            "data" => {
                "max" => "477626368",
                "used" => "156216336"
            }
        }],
        "outcome" => "UP"
    }
}
```

Alternatively, you can use HTTP to perform a health check using the URL [http://localhost:8080/health/](http://localhost:8080/health/):

```
{
   "checks":[
      {
         "id":"heap-memory",
         "result":"UP",
         "data":{
            "max":"477626368",
            "used":"160137128"
         }
      }
   ],
   "outcome":"UP"
}
```

If you now deploy the `demo.war` (from the examples/simple directory), its health check procedures will also be taken into account to determine the overal healthiness of the server:

```
$> $JBOSS_HOME/bin/jboss-cli.sh -c
[standalone@localhost:9990 /] deploy $WORKDIR/wildfly-microprofile-health/examples/simple/target/demo.war
```

You go again to [http://localhost:8080/health/](http://localhost:8080/health/), you will see that the `outcome` will be `down` when the `random` health check procedure defined by the Web application is (randomly) down:

```
{
   "checks":[
      {
         "id":"heap-memory",
         "result":"UP",
         "data":{
            "max":"477626368",
            "used":"174963568"
         }
      },
      {
         "id":"random",
         "result":"DOWN",
         "data":{
            "foo":"bar"
         }
      }
   ],
   "outcome":"DOWN"
}
```

Note that when the overall outcome is down, the HTTP returns a `503 Service Unavailable` status code along with the JSON payload.
If the overall outome is down, the returned status code in in the 20x range.

# Links

* [WildFly][wildfly]
* [WildFly Swarm][swarm]
* [Eclipse MicroProfile Health][microprofile-health]


[wildfly]: https://wildlfy.org/
[swarm]: http://wildfly-swarm.io/
[microprofile-health]: https://github.com/eclipse/microprofile-health/
