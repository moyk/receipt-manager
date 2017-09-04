package controllers;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Context;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import io.dropwizard.jersey.sessions.Session;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class netidcontroller {

    @GET
    @Path("/netid")
    public String netid() {
        return "ym443";
    }
}
