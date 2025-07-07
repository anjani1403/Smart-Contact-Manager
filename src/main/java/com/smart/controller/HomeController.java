package com.smart.controller;

import java.io.File;

import javax.mail.internet.ContentType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	//Home Page Handler
	@RequestMapping("/")
	public String home(Model model) {
		
		model.addAttribute("title", "Home - Smart Contact Manager");
		return "home";
	}
	
	//About Page Handler
	@RequestMapping("/about")
	public String about(Model model) {
		
		model.addAttribute("title", "About - Smart Contact Manager");
		return "about";
	}
	
	//SignUp Page Handler
	@RequestMapping("/signup")
	public String signup(Model model) {
		
		model.addAttribute("title", "SignUp - Smart Contact Manager");
		model.addAttribute("scrollablePage", true);
		model.addAttribute("user", new User());
		return "signup";
	}
	
	//Registering User Handler
	@RequestMapping(value = "/do_signup", method = RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user,
							   BindingResult result,
							   @RequestParam(value = "agreement", defaultValue = "false") boolean agreement,
							   @RequestParam("profileImage") MultipartFile file,
							   Model model, HttpSession session) {
		
		try {
			
			//check if user agreed to terms
			if(!agreement) {
				System.out.println("Please tick the agree and proceed checkbox");
				throw new Exception("Please tick the agree and proceed checkbox");
			}
			
			//If validation errors exists in user input, return form with erorrs
			if(result.hasErrors()) {
				System.out.println("ERROR" + result.toString());
				model.addAttribute("user", user);
				return "signup";
			}
			
			//check if image file is empty
			if(file.isEmpty()) {
				throw new Exception("Profile image is required");
			}
			
			//check for valid image file types
			String contetType = file.getContentType();
			if(!(contetType.equals("image/png") || contetType.equals("image/jpeg") || contetType.equals("image/jpg"))) {
				throw new Exception("Only PNG, JPG and JPEG formats are allowed");
			}
			
			//Generate unique filename and save it in user object
			String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
			user.setImageUrl(fileName);
			
			//save the image file to static/img directory
			File saveFile = new File("src/main/resources/static/img/" + fileName);
			file.transferTo(saveFile);
			
			//set default user attributes
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			System.out.println("Agreement" + agreement);
			System.out.println("USER" + user);
			
			//save the user to database
			this.userRepository.save(user);
			
			//set success message and reset form
			model.addAttribute("user", new User());
			session.setAttribute("message",  new Message("Successfully Registered", "alert-success"));
			return "signup";
			
		}catch(Exception e) {
			
			//If any error occurs, show error message
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message",  new Message("Something Went Wrong " + e.getMessage(), "alert-danger"));
			return "signup";
		}
		
	}
	
	//Custom Login Handler
	@GetMapping("/signin")
	public String customLogin(Model model) {
		model.addAttribute("title", "Login - Smart Contact Manager");
		return "login";
	}
	
	@GetMapping("/login-fail")
	public String loginFailed(Model model) {
		model.addAttribute("title", "Login-Failed");
		return "login-fail";
	}
	
}
