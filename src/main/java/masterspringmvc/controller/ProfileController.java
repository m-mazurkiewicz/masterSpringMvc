package masterspringmvc.controller;

import masterspringmvc.Forms.ProfileForm;
import masterspringmvc.UserProfileSession;
import masterspringmvc.config.PictureUploadProperties;
import masterspringmvc.date.USLocalDateFormatter;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

@Controller
public class ProfileController {

    private UserProfileSession userProfileSession;

//    public static final String ATTR_PROFILE_FORM = "profileForm";
//    @GetMapping("/profile")
//    public String displayProfile(Model model){
//        model.addAttribute(ATTR_PROFILE_FORM, new ProfileForm()); //tutaj potrzeba przekazac mu do modelu danych tego GETa, ze ma cos takiego jak profileForm, bo nie ma gdzie zapisywac tam tych wartosci
//        return  "profile/profilePage";
//    }

    @Autowired
    public ProfileController(UserProfileSession userProfileSession){
        this.userProfileSession = userProfileSession;
    }

    @GetMapping("/profile")
    public String displayProfile(ProfileForm profileForm){
        return  "profile/profilePage";
    }

    @ModelAttribute
    public ProfileForm getProfileForm(){
        return userProfileSession.toForm();
    }

    @PostMapping(value = "/profile", params = {"save"})
    public String saveProfile(@Valid ProfileForm profileForm, BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            return "profile/profilePage";
        }
        userProfileSession.saveForm(profileForm);
        return "redirect:/search/mixed;keywords="+String.join(",",profileForm.getTastes());
    }

    @RequestMapping(value = "/profile", params = {"addTaste"})
    public String addRow(ProfileForm profileForm){
        profileForm.getTastes().add(null);
        return "profile/profilePage";
    }

    @RequestMapping(value = "/profile", params = {"removeTaste"})
    public String removeRow(ProfileForm profileForm, HttpServletRequest req){
        Integer rowId = Integer.valueOf(req.getParameter("removeTaste"));
        profileForm.getTastes().remove(rowId.intValue());
        return "profile/profilePage";
    }

    @ModelAttribute("dateFormat")
    public String localeFormat(Locale locale){
        return USLocalDateFormatter.getPattern(locale);
    }

}
