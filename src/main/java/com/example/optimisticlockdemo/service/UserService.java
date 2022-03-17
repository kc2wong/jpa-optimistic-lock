package com.example.optimisticlockdemo.service;

import com.example.optimisticlockdemo.model.User;
import com.example.optimisticlockdemo.repository.UserEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    @Autowired
    private UserEntityRepository userEntityRepository;

    public User findById(long id) {
        return userEntityRepository.findById(id)
                .map(it -> {
                    User user = new User();
                    user.setId(it.getId());
                    user.setName(it.getName());
                    user.setVersion(it.getVersion());
                    user.setEmail(it.getEmail());
                    return user;
                })
                .orElse(null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean updateEmail(User user, long secondsToDelay) {
        return userEntityRepository.findById(user.getId())
                .map(it -> {
                    it.setEmail(user.getEmail());
                    // Setting the version has no effect.
                    // JPA does not check if the version match with current value in database when doing update
                    // JPA always increment the version by 1
                    it.setVersion(user.getVersion());
                    try {
                        TimeUnit.SECONDS.sleep(secondsToDelay);
                    } catch (InterruptedException e) {
                    }
                    userEntityRepository.save(it);
                    return true;
                })
                .orElse(false);
    }
}
