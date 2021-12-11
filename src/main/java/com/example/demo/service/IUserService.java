package com.example.demo.service;

import com.example.demo.persistence.model.User;
import com.example.demo.web.dto.UserDto;

public interface IUserService {

    User registerNewUserAccount(UserDto userDto);

}
