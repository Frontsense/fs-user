package com.mag.frontsense.resources;

import com.kumuluz.ee.cors.annotations.CrossOrigin;
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
@CrossOrigin(name="http://localhost:8100")
public class UserResource {

    @Inject
    private UsersBean usersBean;

    @GET
    @Path("/test")
    @CrossOrigin(name="http://localhost:8100")
    public Response testResponse() {
        return Response.ok("User api is up and running!")
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                .header("Access-Control-Allow-Headers", "Origin, Content-Type, Accept")
                .build();
    }

    @GET
    @Path("/all")
    public Response getUsers() {
        List<User> users = usersBean.allUsers();

        return Response.ok(users)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                .header("Access-Control-Allow-Headers", "Origin, Content-Type, Accept")
                .build();
    }

    @OPTIONS
    @Path("/login")
    public Response optionsLogin() {
        return Response.ok()
                .header("Access-Control-Allow-Origin", "http://localhost:8100")
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                .header("Access-Control-Allow-Headers", "Origin, Content-Type, Accept")
                .build();
    }

    @POST
    @Path("/login")
    public Response loginUser(String loginInfo) {
        JSONObject loginJson = new JSONObject(loginInfo);

        LoginResponse loginResponse = usersBean.loginUser(loginJson);

        int pass = loginResponse.getPass();

        JSONObject returnJson = null;

        if (pass == 1 || pass == 2) {
            //user or password incorrect
            JSONObject error = new JSONObject()
                                    .put("error", "Incorrect username or password.");
            returnJson = error;
        } else {
//            JSONObject idToken = loginResponse.getIdToken();
            JSONObject idToken = new JSONObject()
                                        .put("success", loginResponse.getIdToken());
            returnJson = idToken;
        }

        return Response.ok(returnJson.toString())
                .header("Access-Control-Allow-Origin", "http://localhost:8100")
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                .header("Access-Control-Allow-Headers", "Origin, Content-Type, Accept")
                .build();
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

        return Response.ok(response.toString())
                .build();
    }
}
