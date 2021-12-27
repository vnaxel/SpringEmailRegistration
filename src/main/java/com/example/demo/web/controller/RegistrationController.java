package com.example.demo.web.controller;

import com.example.demo.persistence.model.User;
import com.example.demo.persistence.model.VerificationToken;
import com.example.demo.registration.OnRegistrationCompleteEvent;
import com.example.demo.service.IUserService;
import com.example.demo.web.dto.UserDto;
import com.example.demo.web.error.UserAlreadyExistException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Calendar;
import java.util.Locale;

@Controller
public class RegistrationController {

    @Autowired
    private IUserService userService;

    @Autowired
    ApplicationEventPublisher eventPublisher;

    @Autowired
    private JavaMailSender mailSender;

    @GetMapping("/registration")
    public String showRegistrationForm(WebRequest request, Model model) {
        UserDto userDto = new UserDto();
        model.addAttribute("user", userDto);
        return "registration";
    }

    @PostMapping("/registration")
    public String registerUserAccount(@ModelAttribute("user") @Valid UserDto userDto,
                                      BindingResult result, HttpServletRequest request) {

        try {
            if (result.hasErrors()) {
                return "registration";
            }
            User registered = userService.registerNewUserAccount(userDto);
            String appUrl = request.getContextPath();
            eventPublisher.publishEvent(new OnRegistrationCompleteEvent(registered, appUrl));
        } catch (final UserAlreadyExistException ueaEx) {
            String err = ueaEx.getMessage();
            result.addError(new ObjectError("globalError", err));
            return "registration";

        } catch (RuntimeException ex) {
            String err = ex.getMessage();
            result.addError(new ObjectError("globalError", err));
            return "registration";
        }
        return "successRegister";
    }

    @GetMapping("/registrationConfirm")
    public String confirmRegistration(WebRequest request, Model model, @RequestParam("token") String token) {

        VerificationToken verificationToken = userService.getVerificationToken(token);
        if (verificationToken == null) {
            String err = "Invalid token";
            model.addAttribute("err", err);
            return "badUser";
        }

        Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            String err = "Your registration token has expired. Please get a new one.";
            model.addAttribute("err", err);
            model.addAttribute("expired", true);
            model.addAttribute("token", token);
            return "badUser";
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userService.saveRegisteredUser(user);
        String message = "Your account is now enabled";
        model.addAttribute("message", message);
        return "login";
    }

    @GetMapping("/resendRegistrationToken")
    public String resendRegistrationToken(HttpServletRequest request, Model model, @RequestParam("token") String existingToken) {
        VerificationToken newToken = userService.generateNewVerificationToken(existingToken);

        User user = userService.getUser(newToken.getToken());
        String appUrl = "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();

        SimpleMailMessage email = constructResendVerificationEmail(appUrl, newToken, user);
        mailSender.send(email);

        String message = "We have sent you an email with a new registration token";
        model.addAttribute("message", message);

        return "login";
    }

    private SimpleMailMessage constructResendVerificationEmail(String contextPath, VerificationToken newToken, User user) {
        String confirmationUrl = contextPath + "/registrationConfirm?token=" + newToken.getToken();
        String message = "To confirm your registration, please click on the below link.";
        SimpleMailMessage email = new SimpleMailMessage();
        email.setSubject("Resend Registration Token");
        email.setText(message + "\r\n" + confirmationUrl);
        email.setFrom("noreply@debool.com");
        email.setTo(user.getEmail());
        return email;
    }
}

