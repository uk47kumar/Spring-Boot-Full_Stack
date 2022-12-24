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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

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
            if (file.isEmpty()) {
                //if the file is empty then try out message
                System.out.println("File is empty");
                contact.setImage("contact.png");
            } else {
                // update the file to folder and update the name to contact
                contact.setImage(file.getOriginalFilename());

                File saveFile = new ClassPathResource("static/img").getFile();

                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                System.out.println("Image is uploaded");
            }

            user.getContacts().add(contact);

            this.userRepository.save(user);

            System.out.println("DATA " + contact);

            System.out.println("Added to the database");

            // Message success...
            session.setAttribute("message", new Message("Your Contact Added!! Add more..", "success"));

        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();

            // Message error...
            session.setAttribute("message", new Message("Something went wrong!! Try again..", "danger"));
        }
        return "normal/add_contact_form";
    }

    // show 5 contact per page
    // current page No. = 0
    @GetMapping("/show-contact/{page}")
    public String showAllContacts(@PathVariable("page") Integer page,
                                  Model model,
                                  Principal principal) {

        model.addAttribute("title", "User Contacts");

        // To find the list of user's contacts
        String userName = principal.getName();
        User user = this.userRepository.getUserByUserName(userName);

        Pageable pageable = PageRequest.of(page, 5);

        Page<Contact> contacts = this.contactRepository.findContactByUser(user.getId(), pageable);

        model.addAttribute("contacts", contacts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", contacts.getTotalPages());

        return "normal/show_contact";
    }

    //showing particular user's contact
    @RequestMapping("/{cId}/contact")
    public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal) {
        System.out.println("CID: " + cId);

        Optional<Contact> optionalContact = this.contactRepository.findById(cId);
        Contact contact = optionalContact.get();

        // solving some issue security (user can view only own personal contact not other user's contact)
        String userName = principal.getName();
        User user = this.userRepository.getUserByUserName(userName);

        if (user.getId() == contact.getUser().getId()) {
            model.addAttribute("contact", contact);
            model.addAttribute("title", contact.getName());
        }
        return "normal/contact_detail";
    }

    @GetMapping("/delete/{cId}")
    public String deleteContact(@PathVariable("cId") Integer cId, Model model,
                                HttpSession session, Principal principal) {

        Contact contact = this.contactRepository.findById(cId).get();

//        contact.setUser(null);
//        this.contactRepository.delete(contact);
        User user = this.userRepository.getUserByUserName(principal.getName());
        user.getContacts().remove(contact);
        this.userRepository.save(user);

        session.setAttribute("message", new Message("Your contact is deleted successfully", "success"));

        return "redirect:/user/show-contact/0";
    }

    //    contact update form handler
    @PostMapping("/update-contact/{cid}")
    public String updateForm(@PathVariable("cid") Integer cid, Model model) {
        model.addAttribute("title", "update contact");

        Contact contact = this.contactRepository.findById(cid).get();
        model.addAttribute("contact", contact);
        return "normal/update_form";
    }

    @PostMapping("/process-update")
    public String updateHandler(@ModelAttribute Contact contact,
                                @RequestParam("profileImage") MultipartFile file,
                                Model model,
                                HttpSession session, Principal principal) {
        try {
            //old contact details
            Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();
            //image
            if (!file.isEmpty()) {
                //file work...
                //rewrite

                //delete old photo
                File deleteFile = new ClassPathResource("static/img").getFile();
                File file1 = new File(deleteFile, oldContactDetail.getImage());
                file1.delete();

                //update new photo
                File saveFile = new ClassPathResource("static/img").getFile();

                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                contact.setImage(file.getOriginalFilename());
            } else {
                contact.setImage(oldContactDetail.getImage());
            }
            User user = this.userRepository.getUserByUserName(principal.getName());
            contact.setUser(user);

            this.contactRepository.save(contact);

            session.setAttribute("message", new Message("Your contact is updated...", "success"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Update Name: " + contact.getName());
        System.out.println("Cid: " + contact.getcId());
        return "redirect:/user/" + contact.getcId() + "/contact";
    }

    // your profile handler
    @GetMapping("/profile")
    public String yourProfile(Model model) {
        model.addAttribute("title", "Profile Page");
        return "normal/profile";
    }

    // open setting handler
    @GetMapping("/settings")
    public String openSettings(Model model) {
        model.addAttribute("title", "Settings");
        return "normal/settings";
    }

    // change password handler
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 Principal principal,
                                 HttpSession session) {
        System.out.println("OLD PASSWORD: " + oldPassword);
        System.out.println("NEW PASSWORD: " + newPassword);
        String userName = principal.getName();
        User currentUser = this.userRepository.getUserByUserName(userName);
        System.out.println("Encrypt password: "+currentUser.getPassword());
        if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())){
            // change the password
            currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
            this.userRepository.save(currentUser);
            session.setAttribute("message", new Message("Your password is successfully changed...", "success"));

        }else {
            //error
            session.setAttribute("message", new Message("Please Enter Correct Old Password!", "danger"));
            return "redirect:/user/settings";
        }
        return "redirect:/user/index";
    }
}
