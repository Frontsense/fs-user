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
import java.util.Map;

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

    public LoginResponse loginUser(JSONObject loginJson, Map<String, String> nonceMap) {
        MongoUser mu = new MongoUser();

        return mu.loginUser(loginJson, nonceMap);

    }

    public List<String> createUser(JSONObject registerJson) {
        MongoUser mu = new MongoUser();

        return mu.createUser(registerJson);
    }

    public String getNonce() {
        MongoUser mu = new MongoUser();

        return mu.createNonce();
    }

    public JSONObject subscribeToTask(JSONObject subscribeJSON) {
        MongoUser mu = new MongoUser();

        return mu.subscribeToTask(subscribeJSON);
    }

    public JSONObject unsubscribeTask(JSONObject data) {
        MongoUser mu = new MongoUser();

        return mu.unsubscribeTask(data);
    }
}
