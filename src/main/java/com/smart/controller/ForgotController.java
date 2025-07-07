package com.smart.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	//email id form open handler
	@RequestMapping("/forgot")
	public String openEmailForm() {
		return "forgot_email";
	}
	
	
	//email submission and OTP generation handler
	@PostMapping("/send-otp")
	public String openOTP(@RequestParam("email") String email, HttpSession session) {
		
		System.out.println("Email : " + email);
		
		//check if user exists in DB beforesending OTP
		
		User user = this.userRepository.getUserByUserName(email);

		if(user == null) {
			//send error message
			session.setAttribute("message", "User does not exist with this email!");
			return "forgot_email";
		}
		else {
			//send change password form
			
		}
		
		
		//generating 6-digit OTP
		Random random = new Random();
		
		int otp = random.nextInt(999999);
		
		System.out.println("OTP : " + otp);
		
		//write code for send otp to email
		String subject = "OTP From Smart Contact Manager"; 
		String message = ""
				+ "<div style='border:1px solid #e2e2e2; padding:20px;'>"
				+ "<h1>"
				+ "Forgot Password OTP : "
				+ "<b>"
				+ otp
				+ "</b>"
				+ "</h1>"
				+ "</div>";
		String to = email;
		
		boolean flag = this.emailService.sendEmail(subject, message, email);
		
		if(flag) {
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			return "verify_otp";
		}
		else {
			session.setAttribute("message", "Check your email id!");
			return "forgot_email";
		}
	}
	
	
	//verify otp handler
	@PostMapping("/verify-otp")
	public String verifyOTP(@RequestParam("otp") int otp, HttpSession session) {
		
		 System.out.println("OTP : " + otp);  
		
		int myotp = (int) session.getAttribute("myotp");
		
		String email = (String)session.getAttribute("email");
		
		if(myotp == otp) {
			
			//return password change form
			return "password_change";
		}
		else {
			
			session.setAttribute("message", "You have entered wrong OTP!");
			return "verify_otp";
		}
	}
	
	//change password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newPassword,@RequestParam("newpassword2")String confirmPassword, HttpSession session) {
		
		if(!newPassword.equals(confirmPassword)) {
			session.setAttribute("message", new Message("Password does not match!", "alert-danger"));
			return "password_change";
		}
		
		String email = (String)session.getAttribute("email");
		User user = this.userRepository.getUserByUserName(email);
		user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
		this.userRepository.save(user);

		session.setAttribute("message", new Message("Password Changed Successfully!", "alert-success"));
		return "redirect:/signin?change=password changed successfully.";
	}
}
