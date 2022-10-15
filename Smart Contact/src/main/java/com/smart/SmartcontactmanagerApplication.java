package com.smart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class SmartcontactmanagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartcontactmanagerApplication.class, args);
	}

	@RequestMapping("/")
	public String home() {
		//creating endpoints
		return "welcome to springboot project home";
	}

	@RequestMapping("/home")
	public String welcome(){
		return "hello";

	}


	@RequestMapping("/welcome")
	public String first(){
		return "hello how are you! isn't it fun";
	}
	@RequestMapping("/welcome")
	public String f(){
		return "hello how are";
	}
	@RequestMapping("/welcome")
	public String fi(){
		return "hello how are you! isn't it fun";
	}
	@RequestMapping("/welcome")
	public String fir(){
		return "hello how are you! isn't it fun";
	}
	@RequestMapping("/welcome")
	public String firs(){
		return "hello how are you! isn't it fun";
	}


}
