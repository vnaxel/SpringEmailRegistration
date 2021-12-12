package com.example.demo.web.controller;

import com.example.demo.persistence.model.User;
import com.example.demo.service.IUserService;
import com.example.demo.web.dto.UserDto;
import com.example.demo.web.error.UserAlreadyExistException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.context.request.WebRequest;

import javax.validation.Valid;

@Controller
public class RegistrationController {

    @Autowired
    private IUserService userService;

    @GetMapping("/registration")
    public String showRegistrationForm(WebRequest request, Model model) {
        UserDto userDto = new UserDto();
        model.addAttribute("user", userDto);
        return "registration";
    }

    @PostMapping("/registration")
    public String registerUserAccount(@ModelAttribute("user") @Valid UserDto userDto,
                                      BindingResult result, Model model) {


        try {
            if (result.hasErrors()) {
                return "registration";
            }
            final User registered = userService.registerNewUserAccount(userDto);
            return "successRegister";
        } catch (final UserAlreadyExistException ueaEx) {
            String err = ueaEx.getMessage();
            result.addError(new ObjectError("globalError", err));
            return "registration";
        }
    }
}
            // TODO: email validation
