package com.beyt.generator.controller;

import com.beyt.generator.entity.User;
import com.beyt.generator.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;


    @GetMapping
    public ResponseEntity<List<User>> userList() {
        return ResponseEntity.ok(userService.getAllUsers());
    }


    @GetMapping("{id}")
    public ResponseEntity<User> user(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getOne(id));
    }

    @PostMapping
    public ResponseEntity<String> addUser(@RequestBody @Valid User user) {
        userService.save(user);
        return ResponseEntity.ok("OK");
    }


    @PutMapping("{id}")
    public ResponseEntity<String> updateUser(@RequestBody @Valid User user, @PathVariable Long id) {
        userService.updateUser(id, user);
        return ResponseEntity.ok("OK");
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("OK");
    }
}
