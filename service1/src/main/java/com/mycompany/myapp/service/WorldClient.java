package com.mycompany.myapp.service;

import com.mycompany.myapp.client.AuthorizedFeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@AuthorizedFeignClient(name = "service2")
public interface WorldClient {

    @RequestMapping(value = "/api/world", method = RequestMethod.GET)
    String getWorld();
}
