package com.beyt.generator.service;


import com.beyt.generator.entity.User;
import com.beyt.generator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements ApplicationRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        userRepository.save(new User("Ali"));
        userRepository.save(new User("Veli"));
        userRepository.save(new User("John"));
        userRepository.save(new User("Mihriban"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getOne(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public void save(User user) {
        user.setId(null);

        userRepository.save(user);
    }

    public void updateUser(Long id, User user) {
        user.setId(id);

        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
