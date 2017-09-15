package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.service.WorldClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api")
public class HelloWorldResource {

    private final WorldClient worldClient;

    public HelloWorldResource(WorldClient worldClient) {
        this.worldClient = worldClient;
    }

    @GetMapping("/helloworld")
    public String getHelloWorld() {
        return "Hello " + worldClient.getWorld();
    }
}
