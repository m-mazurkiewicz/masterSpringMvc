package masterspringmvc.controller;

import masterspringmvc.UserProfileSession;
import masterspringmvc.config.PictureUploadProperties;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

@Controller
public class PictureUploadController {
    private final Resource picturesDir;
    private final Resource anonymousPicture;
    private final MessageSource messageSource;
    private final UserProfileSession userProfileSession;

    @Autowired
    public PictureUploadController(PictureUploadProperties uploadProperties, MessageSource messageSource, UserProfileSession userProfileSession){
        picturesDir = uploadProperties.getUploadPath();
        anonymousPicture = uploadProperties.getAnonymousPicture();
        this.messageSource = messageSource;
        this.userProfileSession = userProfileSession;
    }
//
//    @RequestMapping("upload")
//    public String uploadPage(){
//        return "profile/uploadPage";
//    }



    @PostMapping(value = "/profile", params = {"upload"})
    public String onUpload(@RequestParam MultipartFile file, RedirectAttributes redirectAttributes) throws IOException{
        if (file.isEmpty() || !isImage(file)){
            redirectAttributes.addFlashAttribute("error", "Niewłaściwy plik. Załaduj odpowiedni plik z obrazem");
            return "redirect:/profile";
        }
        Resource picturePath = copyFileToPictures(file);
        userProfileSession.setPicturePath(picturePath);
        //return "profile/profilePage";
        return "redirect:/profile";
    }

//    @PostMapping("/upload")
//    public String onUpload(MultipartFile file, RedirectAttributes redirectAttributes, Model model) throws IOException{
//        //throw new IOException("Komunikat testowy");
//        if (file.isEmpty() || !isImage(file)){
//            redirectAttributes.addFlashAttribute("error", "Niewłaściwy plik. Załaduj odpowiedni plik z obrazem");
//            return "redirect:/profile";
//        }
//        Resource picturePath = copyFileToPictures(file);
//        model.addAttribute("picturePath", picturePath);
//        return "profile/profilePage";
//    }

    @RequestMapping(value = "/uploadedPicture")
    public void getUploadedPicture(HttpServletResponse response) throws IOException{
        Resource picturePath = userProfileSession.getPicturePath();
        if (picturePath == null){
            picturePath = anonymousPicture;
        }
        response.setHeader("Content-Type", URLConnection.guessContentTypeFromName(picturePath.getFilename()));
        //Path path = Paths.get(picturePath.getURI());
        IOUtils.copy(picturePath.getInputStream(), response.getOutputStream());
    }

//    @ModelAttribute("picturePath")
//    public Resource picturePath(){
//        return anonymousPicture;
//    }

    private Resource copyFileToPictures(MultipartFile file) throws IOException{
        String fileExtension = getFileExtension(file.getOriginalFilename());
        File tempFile = File.createTempFile("pic", fileExtension, picturesDir.getFile());
        try (InputStream in = file.getInputStream();
        OutputStream out = new FileOutputStream(tempFile)){
            IOUtils.copy(in, out);
        }
        return new FileSystemResource(tempFile);
    }

    private boolean isImage(MultipartFile file){
        return file.getContentType().startsWith("image");
    }

    private static String getFileExtension (String name){
        return name.substring(name.lastIndexOf("."));
    }

    @ExceptionHandler(IOException.class)
    public ModelAndView handleIOException(Locale locale){
        ModelAndView modelAndView = new ModelAndView("profile/profilePage");
        modelAndView.addObject("error", messageSource.getMessage("upload.io.exception", null, locale));
        modelAndView.addObject("profileForm", userProfileSession.toForm());
        return modelAndView;
    }

    @RequestMapping("uploadError")
    public ModelAndView onUploadError(Locale locale){
        ModelAndView modelAndView = new ModelAndView("profile/profilePage");
        modelAndView.addObject("error", messageSource.getMessage("upload.file.too.big", null, locale));
        modelAndView.addObject("profileForm", userProfileSession.toForm());
        return modelAndView;
    }
}
