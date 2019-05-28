package com.sucl.redis.web;

import com.sucl.redis.model.User;
import com.sucl.redis.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author sucl
 * @date 2019/5/23
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public User getUser(@PathVariable("id") String id){
        return userService.getUser(id);
    }

    @GetMapping
    public List<User> getUsers(){
        return userService.getUsers();
    }

    @PostMapping
    public User saveUser(User user){
        return userService.saveUser(user);
    }

    @PostMapping(params = {"userId"})
    public User updateUser(User user){
        return userService.updateUser(user);
    }

    @DeleteMapping("/{id}")
    public int deleteUser(@PathVariable("id") String id){
        return userService.deleteUser(id);
    }

}
