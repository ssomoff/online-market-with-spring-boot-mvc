package com.somoff.shopproject.services.interfaces;

import com.somoff.shopproject.entities.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IUserService {
    void addUser(User user);
    int updateUser(User user, User userForm, String password, MultipartFile file) throws IOException;
    User findByEmail(String email);
    int createUser(User user, String address2, String rePassword, MultipartFile file) throws IOException;
    boolean createAdmin();
    List<User> getUsers();
}
