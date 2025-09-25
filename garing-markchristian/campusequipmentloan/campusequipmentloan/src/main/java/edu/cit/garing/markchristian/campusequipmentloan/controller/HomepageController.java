package edu.cit.garing.markchristian.campusequipmentloan.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomepageController {

    @GetMapping("/home")
    public String home() {
        return " Welcome! You are logged in.";
    }
}
