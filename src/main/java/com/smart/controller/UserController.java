package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	//method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model,Principal principal, HttpSession session) {
		
		if(principal != null) {
			String userName = principal.getName();
			System.out.println("USERNAME : " + userName);
			
			//get the user using username(Email)
			
			User user = userRepository.getUserByUserName(userName);
			
			System.out.println("USER : " + user);
			
			model.addAttribute("user", user);
		}
		
		Message message = (Message) session.getAttribute("message");
		if(message != null) {
			model.addAttribute("message",message);
			session.removeAttribute("message");
		}
	}
	
	//dashboard home handler
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title", "SmartContactManaget - Dashboard");
		return "normal/user_dashboard";
	}
	
	//open add contact form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model, Principal principal) {
		
		model.addAttribute("title", "SmartContactManager - Add Contact");
		model.addAttribute("contact", new Contact());
		model.addAttribute("scrollablePage", true);
		
		return "normal/add_contact";
	}
	
	//processing add-contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, 
								@RequestParam("profileImage") MultipartFile file, 
								Principal principal, HttpSession session) {
			
		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
			
			//processing and uploading file
			if(file.isEmpty()) {
				//if the file is empty then try our message
				System.out.println("File is empty");
				contact.setImage("default.png");
			}
			else {
				//upload the file to folder and update the name to contact
				contact.setImage(file.getOriginalFilename());
				
				File savedfile = new ClassPathResource("static/img").getFile();
				
			 	Path path = Paths.get(savedfile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				System.out.println("Image is uploaded");
			}
			
			contact.setUser(user);
			
			user.getContacts().add(contact);
			
			
			
			this.userRepository.save(user);
			
			System.out.println("DATA " + contact);
			System.out.println("Contact added to database");
			
			//message of success...
			session.setAttribute("message", new Message("Your Contact is Added Successfully! Add More Contacts.", "success"));
			
		} catch (Exception e) {
			System.out.println("ERROR : " + e.getMessage());
			e.printStackTrace();
			//message of error...
			session.setAttribute("message", new Message("Something Went Wrong! Try Again.", "danger"));
		}
		
		
		return "redirect:/user/add-contact";
	}
	
	//Show-Contacts Handler
	//per page = 6(n)
	//current page = 0(page)
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page")Integer page, Model model, Principal principal) {
		
		//contact ki list bhejni hai database se view pe
		
		String userName = principal.getName();
		
		User user = this.userRepository.getUserByUserName(userName);
		
		model.addAttribute("title", "SmartContactManager - View Contacts");
		
		//currentPage - page
		//contactPerPage - 5
		PageRequest pageable = PageRequest.of(page, 5);
				
		Page<Contact> contacts = contactRepository.findByUserOrderByNameAsc(user, pageable);
		
		model.addAttribute("contacts",contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	
	//showing particular contact details
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId")Integer cId, Model model, Principal principal) {
		System.out.println("CID : " + cId);
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		
		if(contactOptional.isPresent()) {
			Contact contact = contactOptional.get();
			
			//check which user is logged in
			String userName = principal.getName();
			User user = this.userRepository.getUserByUserName(userName);
			
			if(user.getId() == contact.getUser().getId()) {
				model.addAttribute("contact",contact);
				model.addAttribute("title","SmartContactManager - " + contact.getName());
				model.addAttribute("scrollablePage", true);
			}else {
				model.addAttribute("contact",null);
			}
		}else {
			model.addAttribute("contact",null);
		}
		
		return "normal/contact_detail";
	}
	
	//delete contact handler
	@GetMapping("/{cId}/delete")
	public String deleteContact(@PathVariable("cId")Integer cId, Principal principal, HttpSession session) {
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		
		if(contactOptional.isPresent()) {
			
			Contact contact = contactOptional.get();
			
			//logged-in user
			String userName = principal.getName();
			User user = this.userRepository.getUserByUserName(userName);
			
			//check if contact belongs to the logged-in user
			if(user.getId() == contact.getUser().getId()) {
				
				contact.setUser(null);
				
				User user2 = this.userRepository.getUserByUserName(principal.getName());
				
				user2.getContacts().remove(contact);
				
				this.userRepository.save(user2);
				
				session.setAttribute("message", new Message("Contact deleted successfully!", "success")); 
			
			}else {
				
				session.setAttribute("message", new Message("You are not authorized to delete this contact!", "danger")); 
			
			}
		
		}else {
			
			session.setAttribute("message", new Message("Contact not found!", "danger"));
		
		}
		
		return "redirect:/user/show-contacts/0";
	}
	
	
	//open update form handler
	@PostMapping("/{cid}/update-contact")
	public String updateForm(@PathVariable("cid")Integer cid, Model model) {
		
		model.addAttribute("title","Update Contact");
		
		Contact contact = this.contactRepository.findById(cid).get();
		
		model.addAttribute("contact",contact);
		
		return "normal/update_form";
	}
	
	//update contact handler
	@RequestMapping(value = "/process-update", method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, Model model, HttpSession session, Principal principal) {
		
		try {
			//fetch old contact details
			Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();
			
			if(!file.isEmpty()) {
				//re-write file :- 
				//delete old photo 
				
				File deleteFile = new ClassPathResource("static/img").getFile();
				
				File file1 = new File(deleteFile, oldContactDetail.getImage());
				
				file1.delete();
				
				//update new photo
				
				File savedfile = new ClassPathResource("static/img").getFile();
				
			 	Path path = Paths.get(savedfile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
				
			}
			else {
				contact.setImage(oldContactDetail.getImage());
			}
			
			User user = this.userRepository.getUserByUserName(principal.getName());
			
			contact.setUser(user);
			
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Your contact is updated successfully!", "success"));
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		System.out.println("CONTACT NAME : " + contact.getName());
		System.out.println("CONTACT ID : " + contact.getcId());
		
		return "redirect:/user/" + contact.getcId() + "/contact";
	}
	
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model, Principal principal) {
		
		String email = principal.getName();
		User user = userRepository.getUserByUserName(email);
		
		model.addAttribute("title","SmartContactManager Profile - " + user.getName());
		return "normal/profile";
	}
	
	//open settings handler
	@GetMapping("/settings")
	public String openSettings(Model model) {
		model.addAttribute("title", "SmartContactManager - Settings");
		return "normal/settings";
	}
	
	//change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword, 
								@RequestParam("newPassword") String newPassword, 
								Principal principal,
								RedirectAttributes redirectAttributes,
								HttpServletRequest request,
								HttpServletResponse response) {
		
		System.out.println("OLD PASSWORD : " + oldPassword);
		System.out.println("NEW PASSWORD : " + newPassword);
		
		String userName = principal.getName();
		User currentUser = this.userRepository.getUserByUserName(userName);
		System.out.println(currentUser.getPassword());
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			
			//change the password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			 
			// set flash attribute (will survive one redirect)
			redirectAttributes.addFlashAttribute("message", new Message("Your Password is Successfully Changed.", "success"));			
			
			//Auto-Logout after password change
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if(auth != null) {
				new SecurityContextLogoutHandler().logout(request, response, auth);
			}
			return "redirect:/signin?passwordChanged=true";
			
		}else {
			
			//error
			redirectAttributes.addFlashAttribute("message", new Message("Wrong Old Password!", "danger"));
			return "redirect:/user/settings";

		}
		
	}
	
	//Show Edit Profile Page Handler
	@GetMapping("/edit-profile")
	public String showEditProfileForm(Model model, Principal principal) {
		String username = principal.getName();
		User user = userRepository.getUserByUserName(username);
		model.addAttribute("title", "SmartContactManager - Edit Profile");
		model.addAttribute("user", user);
		return "normal/edit_profile";
	}
	
	//Update User Profile and submit 
	@PostMapping("/process-edit-profile")
	public String updateUserProfile(@ModelAttribute User user, @RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session) {
		try {
			
			User currentUser = userRepository.getUserByUserName(principal.getName());
			
			//Handle image
			if(!file.isEmpty()) {
				
				//Delete old image
				File oldImageFolder = new ClassPathResource("static/img").getFile();
				File oldImageFile = new File(oldImageFolder, currentUser.getImageUrl());
				oldImageFile.delete();
				
				//Save new image
				File newImageFolderFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(newImageFolderFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				currentUser.setImageUrl(file.getOriginalFilename());
			}
			
			currentUser.setName(user.getName());
			currentUser.setEmail(user.getEmail());
			currentUser.setAbout(user.getAbout());
			
			//Save to DB
			userRepository.save(currentUser);
			
			session.setAttribute("message", new Message("Profile updated successfully", "success"));
		
		}catch(Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("Something went wrong!", "danger"));
		}
		
		return "redirect:/user/index";
	}
	
}


 
