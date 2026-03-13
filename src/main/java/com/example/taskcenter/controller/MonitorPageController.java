package com.example.taskcenter.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MonitorPageController {

    @GetMapping("/monitor")
    public String monitorPage() {
        return "monitor";
    }
}
