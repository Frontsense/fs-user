package com.mag.frontsense.resources;

import com.mag.frontsense.beans.UsersBean;
import com.mag.frontsense.models.LoginResponse;
import com.mag.frontsense.models.User;
import org.json.JSONObject;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/user")
@ApplicationScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    private UsersBean usersBean;


    @GET
    @Path("/test")
    public Response testResponse() {
        return Response.ok("User api is up and running!").build();
    }

    @GET
    @Path("/all")
    public Response getUsers() {
        List<User> users = usersBean.allUsers();

        return Response.ok(users).build();
    }

    @POST
    @Path("/login")
    public Response loginUser(String loginInfo) {
        JSONObject loginJson = new JSONObject(loginInfo);

        LoginResponse loginResponse = usersBean.loginUser(loginJson);

        int pass = loginResponse.getPass();

        if (pass == 1 || pass == 2) {
            //user or password incorrect
            JSONObject error = new JSONObject()
                                    .put("error", "Incorrect username or password.");
            return Response.ok(error.toString()).build();
        } else {
            JSONObject idToken = loginResponse.getIdToken();
            return Response.ok(idToken.toString()).build();
        }

    }

    @POST
    @Path("/create")
    public Response createUser(String registerInfo) {
        List<String> responseCreate = usersBean.createUser(new JSONObject(registerInfo));

        JSONObject response = new JSONObject();
        if (responseCreate.size() == 0) {
            response.put("success", "Account created successfully");
        } else {
            response.put("error", responseCreate.toArray());
        }

        return Response.ok(response.toString()).build();
    }
}
