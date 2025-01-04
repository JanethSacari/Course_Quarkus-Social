package org.acme.quarkussocial.rest;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.acme.quarkussocial.dto.UserRequest;

@Path("/users")
public class UserResource {

    @POST
    public Response createUser(UserRequest userRequest){
        return Response.ok().build();
    }
}
