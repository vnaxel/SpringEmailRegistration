package com.example.demo.service;

import com.example.demo.persistence.model.User;
import com.example.demo.persistence.model.VerificationToken;
import com.example.demo.web.dto.UserDto;

public interface IUserService {

    User registerNewUserAccount(UserDto userDto);

    User getUser(String verificationToken);

    void createVerificationToken(User user, String token);

    void saveRegisteredUser(User user);

    VerificationToken getVerificationToken(String VerificationToken);

}
