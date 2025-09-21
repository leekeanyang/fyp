package com.example.fyp;

import java.util.ArrayList;
import java.util.List;

public class UserRoleState {
    String roleName;
    List<String> permissions;

    public UserRoleState(String roleName, List<String> permissions) {
        this.roleName = roleName;
        this.permissions = new ArrayList<>(permissions); // deep copy
    }
}

