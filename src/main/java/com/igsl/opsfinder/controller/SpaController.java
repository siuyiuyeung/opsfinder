package com.igsl.opsfinder.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Catch-all controller for SPA (Single Page Application) routing.
 * Forwards all non-API, non-static GET requests to index.html so Vue Router
 * can handle client-side navigation on page refresh or direct URL access.
 */
@Controller
public class SpaController {

    /**
     * Forward any path that:
     *  - does not start with /api, /actuator, /ws, /error
     *  - does not contain a dot (i.e. is not a static file like .js, .css)
     * to index.html, letting the Vue SPA bootstrap and handle routing.
     */
    @GetMapping(value = {
        "/",
        "/{path:^(?!api$|actuator$|ws$|error$)[^\\.]*}",
        "/{path:^(?!api$|actuator$|ws$|error$)[^\\.]*}/**"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
