package com.example.userservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUser(@PathVariable long id){

        UserEntity user = userRepository.findById(id);


        String use =user.getName();

        UserDTO dto = new UserDTO();

        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());

        return ResponseEntity.ok(dto);

    }


    @PostMapping("/register")
    public ResponseEntity<?> Register(@RequestBody UserEntity users) {

    UserEntity user = userRepository.findByEmail(users.getEmail());
    

    if(user != null){

        return ResponseEntity.badRequest().body("email already exists");
   }
        String password = passwordEncoder.encode(users.getPassword());

        users.setPassword(password);
        userRepository.save(users);


        return ResponseEntity.ok("User registered successfully.");

    }
    @PostMapping("/login")
    public ResponseEntity<?> Login(@RequestBody UserEntity users){

        UserEntity user = userRepository.findByEmail(users.getEmail());

        if(user != null && passwordEncoder.matches(users.getPassword(), user.getPassword())){

            return ResponseEntity.ok("Login successful");
            

        }
            return ResponseEntity.badRequest().body("Invalid credentials");

        
    }
    
    
      
    
}
