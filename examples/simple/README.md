# Simple MicroProfile Health Example

# Installation

* Deploy the `simple.war` archive in WildFly

# Usage

The `demo.war` is a simple Web application that returns a `Hello, World` string from http://localhost:8080/simple/hello/.

It also defines a `HealthCheck` procedures that returns `UP` 80% of the time it is invoked.
If you repeatedly check http://localhost:8080/health, you will notice that the overall
`outcome` is `UP` 80% of the time and `DOWN` otherwise depending on the `state` of the `randome` check.