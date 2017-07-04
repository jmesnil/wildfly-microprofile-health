package org.wildfly.swarm.microprofile.config.health;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;


@Path("/hello")
public class HelloWorldEndpoint {

	@GET
	@Produces("text/plain")
	public String doGet() {
		System.out.println("HelloWorldEndpoint.doGet");
		return "Hello, World";
	}
}