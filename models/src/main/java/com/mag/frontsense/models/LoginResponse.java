package com.mag.frontsense.models;

import org.json.JSONObject;

public class LoginResponse {

    /*  0 ... OK
        1 ... user doesn't exist
        2 ... incorrect password
     */
    private int pass;
    private JSONObject idToken;

    LoginResponse(int pass, JSONObject token) {
        this.pass = pass;
        this.idToken = token;
    }

    public int getPass() {
        return pass;
    }

    public void setPass(int pass) {
        this.pass = pass;
    }

    public JSONObject getIdToken() {
        return idToken;
    }

    public void setIdToken(JSONObject idToken) {
        this.idToken = idToken;
    }
}
