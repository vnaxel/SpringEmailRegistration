package com.example.demo.web.controller;

import com.example.demo.persistence.model.User;
import com.example.demo.service.IUserService;
import com.example.demo.web.dto.UserDto;
import com.example.demo.web.error.UserAlreadyExistException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
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
    public ModelAndView registerUserAccount(@ModelAttribute("user") @Valid UserDto userDto,
                                              HttpServletRequest request, Errors errors) {

        try {
            final User registered = userService.registerNewUserAccount(userDto);
            // TODO: email validation
        } catch (final UserAlreadyExistException ueaEx) {
            ModelAndView mav = new ModelAndView("registration", "user", userDto);
            mav.addObject("message", "An account for that email already exists.");
            return mav;
            // TODO: display error messages on templates
        }
        return new ModelAndView("successRegister", "user", userDto);
    }
}
