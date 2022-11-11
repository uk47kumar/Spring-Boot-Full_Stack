package com.smart.controller;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entity.Contact;
import com.smart.entity.User;
import com.smart.helper.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ContactRepository contactRepository;

    // method to add common data to response
    @ModelAttribute
    public void addCommonData(Model model, Principal principal) {
        String userName = principal.getName();
        System.out.println("USERNAME: " + userName);

        // get the user using username (email)
        User user = userRepository.getUserByUserName(userName);
        System.out.println("USER: " + user);

        model.addAttribute("user", user);
    }

    @RequestMapping("/index")
    public String dashboard(Model model, Principal principal) {
        model.addAttribute("title", "User Dashboard");
        return "normal/user_dashboard";
    }

    //add contact form handler
    @GetMapping("/add-contact")
    public String openAddContactForm(Model model) {
        model.addAttribute("title", "Add Contact");
        model.addAttribute("contact", new Contact());
        return "normal/add_contact_form";
    }

    // processing add contact form
    @PostMapping("/process-contact")
    public String processContact(@ModelAttribute Contact contact,
                                 @RequestParam("profileImage") MultipartFile file,
                                 Principal principal,
                                 HttpSession session) {

        try {
            String name = principal.getName();
            User user = this.userRepository.getUserByUserName(name);

            contact.setUser(user);

            // processing the uploading file (image)
            if(file.isEmpty()){
                //if the file is empty then try out message
                System.out.println("File is empty");
            }
            else {
                // update the file to folder and update the name to contact
                contact.setImage(file.getOriginalFilename());

                File saveFile = new ClassPathResource("static/img").getFile();

                Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());

                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                System.out.println("Image is uploaded");
            }

            user.getContacts().add(contact);

            this.userRepository.save(user);

            System.out.println("DATA " + contact);

            System.out.println("Added to the database");

            // Message success...
            session.setAttribute("message", new Message("Your Contact Added!! Add more..","success"));

        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();

            // Message error...
            session.setAttribute("message", new Message("Something went wrong!! Try again..","danger"));
        }
        return "normal/add_contact_form";
    }

    // show 5 contact per page
    // current page No. = 0
    @GetMapping("/show-contact/{page}")
    public String showAllContacts(@PathVariable("page") Integer page, Model model, Principal principal){

        model.addAttribute("title","User Contacts");

        // To find the list of user's contacts
        String userName = principal.getName();
        User user = this.userRepository.getUserByUserName(userName);

        Pageable pageable = PageRequest.of(page,3);

        Page<Contact> contacts = this.contactRepository.findContactByUser(user.getId(),pageable);

        model.addAttribute("contacts",contacts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages",contacts.getTotalPages());

        return "normal/show_contact";
    }
}
