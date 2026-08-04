package dev.excsi.urlshortener.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String serveHtml() {
        return "forward:/index.html";
    }
}
