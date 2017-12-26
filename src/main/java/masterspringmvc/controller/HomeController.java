package masterspringmvc.controller;

import masterspringmvc.UserProfileSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private UserProfileSession userProfileSession;

    @Autowired
    public HomeController(UserProfileSession userProfileSession){
        this.userProfileSession = userProfileSession;
    }

    @GetMapping("/")
    public String home(){
        if (userProfileSession.getTastes().isEmpty()){
            return "redirect:/profile";
        }
        return "redirect:/search/mixed;keywords="+String.join(",",userProfileSession.getTastes());
    }
}
