package com.sucl.redis.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author sucl
 * @date 2019/5/23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {

    private String userId;
    private String username;
    private int age;
    private String email;

    @Override
    public String toString() {
        return "{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", age=" + age +
                ", email='" + email + '\'' +
                '}';
    }
}
