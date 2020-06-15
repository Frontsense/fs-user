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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/user")
@ApplicationScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@CrossOrigin(name="http://localhost:8100")
public class UserResource {

    private Map<String, String> nonceMap = new HashMap<>();

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
    @Path("/nonce")
    public Response optionsNonce() {
        return Response.ok()
                .header("Access-Control-Allow-Origin", "http://localhost:8100")
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                .header("Access-Control-Allow-Headers", "Origin, Content-Type, Accept")
                .build();
    }


    @POST
    @Path("/nonce")
    public Response getNonce(String nonceInfo) {
        JSONObject nonceJson = new JSONObject(nonceInfo);
        String nonce = usersBean.getNonce();
        nonceMap.put(nonceJson.getString("email"), nonce);

        return Response.ok(nonce)
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

        LoginResponse loginResponse = usersBean.loginUser(loginJson, nonceMap);

        int pass = loginResponse.getPass();

        JSONObject returnJson = null;

        if (pass == 1 || pass == 2) {
            //user or password incorrect
            JSONObject error = new JSONObject()
                                    .put("error", "Incorrect username or password.");
            returnJson = error;
        } else {
            JSONObject idToken = new JSONObject()
                                        .put("success", loginResponse.getIdToken());
            returnJson = idToken;
        }

        nonceMap.remove(loginJson.getString("email"));

        return Response.ok(returnJson.toString())
                .header("Access-Control-Allow-Origin", "http://localhost:8100")
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                .header("Access-Control-Allow-Headers", "Origin, Content-Type, Accept")
                .build();
    }


    @OPTIONS
    @Path("/create")
    public Response optionsCreate() {
        return Response.ok()
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
                .header("Access-Control-Allow-Origin", "http://localhost:8100")
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                .header("Access-Control-Allow-Headers", "Origin, Content-Type, Accept")
                .build();
    }
}
