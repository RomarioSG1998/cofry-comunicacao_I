//package com.app_testy_financa.demo.service;
//
//import com.app_testy_financa.demo.Model.user;
//import com.app_testy_financa.demo.Repository.userRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.converter.StringHttpMessageConverter;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Service;
//
//@Service
//public class UserService {
//    @Autowired
//    userRepository userRepository;
//    @Autowired
//    private JdbcTemplate JdbcTemplate;
//
//    user User= new user();
//
//    public user createUser(user User){
//        String sql="INSERT INTO user (first_name, second_name, email, password) VALUES (?, ?, ?, ?)";
//        JdbcTemplate.update(sql,
//                User.getFirstName(),
//                User.getSecondName(),
//                User.getEmail(),
//                User.getPassword()
//        );
//        return User;
//    }
//
//}
