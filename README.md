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

# Links

* [WildFly][wildfly]
* [WildFly Swarm][swarm]
* [Eclipse MicroProfile Health][microprofile-health]


[wildfly]: https://wildlfy.org/
[swarm]: http://wildfly-swarm.io/
[microprofile-health]: https://github.com/eclipse/microprofile-health/
