package org.minerva.stateservice;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class RedocController {
    @RequestMapping(value = "/redoc", method = RequestMethod.GET)
    public String redoc() {
//        ModelAndView modelAndView = new ModelAndView();
//        modelAndView.setViewName("redoc.html");
//        return modelAndView;
        return "redoc.html";
    }
}
