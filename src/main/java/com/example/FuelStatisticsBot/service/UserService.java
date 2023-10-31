package com.example.FuelStatisticsBot.service;

import com.example.FuelStatisticsBot.model.User;
import com.example.FuelStatisticsBot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findOne(long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User save (User user) {
        return userRepository.save(user);
    }

    @Transactional
    public void update(long id, User updatedUser) {
        updatedUser.setChatId(id);
        userRepository.save(updatedUser);
    }

    @Transactional
    public void delete(long id) {
        userRepository.deleteById(id);
    }
}
