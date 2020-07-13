package com.mag.frontsense.models;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MongoUser {

    private final String DBUser         = "root";
    private final String DBPassword     = "jXi5B86rnboc2SQC";
    private final String DBName         = "fs-users";
    private final String DBCollection   = "users";

    private MongoClient connectDB() {
        MongoClientURI uri = new MongoClientURI("mongodb+srv://"+ DBUser +":"+ DBPassword +"@cluster0-9m5aj.mongodb.net/"+ DBName +"?retryWrites=true&w=majority");

        return new MongoClient(uri);
    }

    public List<User> getAllUsers() {
        MongoClient client = connectDB();
        MongoDatabase db = client.getDatabase(DBName);
        MongoCollection<Document> userCollection = db.getCollection(DBCollection);

        List<User> results = new ArrayList<>();

        for(Document curr : userCollection.find()) {
            User currUser = new User(curr.getInteger("userId"),
                                curr.getString("username"),
                                curr.getString("email")
                        );

            results.add(currUser);
        }

        return results;
    }

    public String getUserTasks(Integer userId) {
        MongoClient client = connectDB();
        MongoDatabase db = client.getDatabase(DBName);
        MongoCollection<Document> userCollection = db.getCollection(DBCollection);

        String result = "[]";

        Bson filterUserId = Filters.eq("userId", userId);
        Document resultFilter = userCollection.find(filterUserId).first();
        if (resultFilter != null) {
            if (resultFilter.getString("tasks") != null)
                result = resultFilter.getString("tasks");
        }

        return result;
    }

    public String createNonce() {
        return Sha256.toHexString(Sha256.getNextSalt());
    }

    public LoginResponse loginUser(JSONObject loginJson, Map<String, String> nonceMap) {
        MongoClient client = connectDB();
        MongoDatabase db = client.getDatabase(DBName);
        MongoCollection<Document> userCollection = db.getCollection(DBCollection);

        /*  idToken {
                "email": .....
                "user": .....
            }
         */

        Bson filterEmail = Filters.eq("email", loginJson.getString("email"));
        Document resultEmail = userCollection.find(filterEmail).first();

        if (resultEmail == null) {
            return new LoginResponse(1, null);
        }

        //hash (snonce, cnonce, password)
        try {
            String clientPassword = loginJson.getString("password");

            String snonce = nonceMap.get(loginJson.getString("email"));
            String cnonce = loginJson.getString("cNonce");

            String storedPasword = resultEmail.getString("password");
            String hashedPassword = Sha256.toHexString(Sha256.getSHA(snonce + cnonce + storedPasword));

            //match hashedPassword with stored
            boolean correctPassword = clientPassword.equals(hashedPassword);
            if (!correctPassword) {
                return new LoginResponse(2, null);
            }

            JSONObject idToken = new JSONObject()
                                    .put("email", resultEmail.getString("email"))
                                    .put("user", resultEmail.getString("username"))
                                    .put("id", resultEmail.getInteger("userId"));

            return new LoginResponse(0, idToken);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return new LoginResponse(1, null);
    }

    public List<String> createUser(JSONObject registerJson) {
        MongoClient client = connectDB();
        MongoDatabase db = client.getDatabase(DBName);
        MongoCollection<Document> userCollection = db.getCollection(DBCollection);

        List<String> result = new ArrayList<String>();

        //check if user or email is taken
        Bson filterEmail = Filters.eq("email", registerJson.getString("email"));
        Document resultEmail = userCollection.find(filterEmail).first();
        if (resultEmail != null) {
            result.add("email");
        }

        Bson filterUser = Filters.eq("username", registerJson.getString("username"));
        Document resultUser = userCollection.find(filterUser).first();
        if (resultUser != null) {
            result.add("username");
        }

        if (resultEmail != null || resultUser != null) {
            return result;
        }

        //get last user id
        int last = userCollection.find().sort(new BasicDBObject("userId", -1)).first().getInteger("userId");

        String password = registerJson.getString("password");
        Document newUser = Document.parse(registerJson.toString());
        newUser.append("userId", last + 1);
        newUser.append("accType", 1);

        userCollection.insertOne(newUser);


        return result;
    }

    public JSONObject subscribeToTask(JSONObject subscribeJSON) {
        MongoClient client = connectDB();
        MongoDatabase db = client.getDatabase(DBName);
        MongoCollection<Document> userCollection = db.getCollection(DBCollection);

        Bson filterUser = Filters.eq("userId", subscribeJSON.getInt("userId"));
        Document resultFilter = userCollection.find(filterUser).first();

        JSONObject response = new JSONObject();

        if (resultFilter != null) {
            String tasks = resultFilter.getString("tasks");

            if (tasks == null) {
                JSONArray tasksArray = new JSONArray();
                tasksArray.put(subscribeJSON.getInt("taskId"));

                resultFilter.put("tasks", tasksArray.toString());

                userCollection.replaceOne(filterUser, resultFilter);
            } else {
                //check if already subscribed
                JSONArray tasksArray = new JSONArray(tasks);

                for (Object element : tasksArray) {
                    int el = (int) element;
                    if (el == subscribeJSON.getInt("taskId")) {
                        response.put("error", "Already subscribed to that task");
                        return response;
                    }
                }

                tasksArray.put(subscribeJSON.getInt("taskId"));
                resultFilter.remove("tasks");
                resultFilter.put("tasks", tasksArray.toString());

                userCollection.replaceOne(filterUser, resultFilter);
            }
        }
        response.put("success", "Task subscribed successfully");
        return response;
    }

    public JSONObject unsubscribeTask(JSONObject data) {
        MongoClient client = connectDB();
        MongoDatabase db = client.getDatabase(DBName);
        MongoCollection<Document> userCollection = db.getCollection(DBCollection);

        Bson filterUser = Filters.eq("userId", data.getInt("userId"));
        Document resultFilter = userCollection.find(filterUser).first();

        JSONObject response = new JSONObject();
        boolean hasTask = false;
        if (resultFilter != null) {
            String tasks = resultFilter.getString("tasks");

            if (tasks == null) {
                response.put("error", "User has no subscribed tasks");
                return response;
            } else {
                JSONArray tasksArray = new JSONArray(tasks);


                for(int i=0; i<tasksArray.length(); i++) {
                    if ((int) tasksArray.get(i) == data.getInt("taskId")) {
                        tasksArray.remove(i);
                        hasTask = true;
                    }
                }

                resultFilter.remove("tasks");
                resultFilter.put("tasks", tasksArray.toString());
                userCollection.replaceOne(filterUser, resultFilter);
            }
        }
        if (hasTask) {
            response.put("success", "Task unsubscribed successfully");
        } else {
            response.put("error", "Not subscribed to this task");
        }

        return response;
    }

}
