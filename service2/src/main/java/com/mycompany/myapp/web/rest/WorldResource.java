package com.mycompany.myapp.web.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api")
public class WorldResource {

    @GetMapping("/world")
    public String getWorld() {
        return "World";
    }
}
