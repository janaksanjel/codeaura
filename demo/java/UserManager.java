package com.demo.app;

// standard imports
import java.util.List;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.util.ArrayList;

/**
 * UserManager class
 * manages all user operations
 */
public class UserManager{
    // list of users
    private List<String> users;
    // map of user roles
    private Map<String, String> roles;

    // constructor
    public UserManager(){
        this.users = new ArrayList<>(); // init list
        this.roles = new HashMap<>();   // init map
    }

    // add a user to the list
    public void addUser(String name){
        // validate name
        if(name != null && !name.isEmpty()){
            users.add(name); // add to list
            roles.put(name, "USER"); // default role
        }
    }



    // get all users
    public List<String> getUsers(){
        return users; // return list
    }

    // check if user exists
    public boolean hasUser(String name){
        return users.contains(name); // check contains
    }
}
