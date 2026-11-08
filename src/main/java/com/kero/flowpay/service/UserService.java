
package com.kero.flowpay.service;

import com.kero.flowpay.model.User;
import com.kero.flowpay.model.Wallet;
import com.kero.flowpay.repository.UserRepository;
import com.kero.flowpay.repository.WalletRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.kero.flowpay.exception.ResourceNotFoundException;
import com.kero.flowpay.exception.BadRequestException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletRepository walletRepository;

    public UserService(
            UserRepository userRepository,
            WalletRepository walletRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.walletRepository = walletRepository;
    }

    @Transactional
    public User register(String name, String email, String password) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BadRequestException("Email already registered");
        }

        String hashedPassword = passwordEncoder.encode(password);

        User user = new User(name, email, hashedPassword);
        User savedUser = userRepository.save(user);

        Wallet wallet = new Wallet(savedUser);
        walletRepository.save(wallet);

        return savedUser;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }
}
