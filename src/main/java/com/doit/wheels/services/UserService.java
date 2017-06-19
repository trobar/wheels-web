package com.doit.wheels.services;


import com.doit.wheels.dao.entities.User;

import java.util.List;

public interface UserService {

    User getUser(long id);

    User saveUser(User user);

    List<User> findAll();

    User findUserByLogin(String login);
}
