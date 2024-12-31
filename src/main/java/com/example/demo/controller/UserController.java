package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    // Save a User
    @PostMapping("/users")
    public ResponseEntity<User> saveUser(@RequestBody User user) {
        return ResponseEntity.ok(userService.saveUser(user));
    }

    // Get Users with Pagination
    @GetMapping("/users")
    public ResponseEntity<Page<User>> getUsers(@RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(userService.getUsers(PageRequest.of(page, 10)));
    }
    
    // Update a User
    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        User existingUser = userService.getUserById(id);
        if (existingUser == null) {
            return ResponseEntity.notFound().build();
        }
        existingUser.setName(updatedUser.getName());
        existingUser.setEmail(updatedUser.getEmail());
        // Update other fields as necessary
        return ResponseEntity.ok(userService.saveUser(existingUser));
    }

    // Delete a User
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        User existingUser = userService.getUserById(id);
        if (existingUser == null) {
            return ResponseEntity.notFound().build();
        }
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

    // Nested API Call
    @GetMapping("/nested-call")
    public ResponseEntity<String> nestedApiCall() {
        RestTemplate restTemplate = new RestTemplate();
        String thirdPartyUrl = "https://api.thirdparty.com/data";
        String response = restTemplate.getForObject(thirdPartyUrl, String.class);
        return ResponseEntity.ok(response);
    }
}
