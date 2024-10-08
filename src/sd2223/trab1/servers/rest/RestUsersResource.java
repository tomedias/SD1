package sd2223.trab1.servers.rest;

import sd2223.trab1.api.User;
import sd2223.trab1.api.java.Users;
import sd2223.trab1.api.rest.UsersService;
import sd2223.trab1.servers.java.JavaUsers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class RestUsersResource extends RestResource implements UsersService {

    final Users impl;
    public RestUsersResource() {
        this.impl = new JavaUsers(RestUsersServer.getDomain());
    }

    @Override
    public String createUser(User user) {

        return super.fromJavaResult( impl.createUser( user));
    }

    @Override
    public User getUser(String name, String pwd) {
        return super.fromJavaResult( impl.getUser(name, pwd));
    }

    @Override
    public void verifyPassword(String name, String pwd) {
        super.fromJavaResult( impl.verifyPassword(name, pwd));
    }

    @Override
    public User updateUser(String name, String pwd, User user) {
        return super.fromJavaResult(impl.updateUser(name, pwd, user));
    }

    @Override
    public User deleteUser(String name, String pwd) {

        User u = super.fromJavaResult(impl.deleteUser(name, pwd));

        return u;
    }

    @Override
    public List<User> searchUsers(String pattern) {
        return  super.fromJavaResult(impl.searchUsers(pattern));
    }


}
