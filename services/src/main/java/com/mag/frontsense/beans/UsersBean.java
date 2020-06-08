package com.mag.frontsense.beans;


import com.mag.frontsense.models.LoginResponse;
import com.mag.frontsense.models.User;
import com.mag.frontsense.models.MongoUser;
import org.json.JSONObject;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.util.List;

@RequestScoped
public class UsersBean {

    private Client httpClient;

    @Inject
    private UsersBean usersBean;

    @PostConstruct
    private void init() {
        httpClient = ClientBuilder.newClient();
    }

    public List<User> allUsers() {
        MongoUser mu = new MongoUser();

        return mu.getAllUsers();
    }

    public LoginResponse loginUser(JSONObject loginJson) {
        MongoUser mu = new MongoUser();

        return mu.loginUser(loginJson);

    }
}
