package com.example.demo.domain.services.auth.two_fa.totp;

import com.example.demo.domain.model.user.subscription.User;
import com.example.demo.domain.repositories.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TOTPService {

    private final UserRepository userRepository;

    @Transactional
    public Boolean generateTOTP(String userId) {
        User user = userRepository.getById(userId);

//        if (user.getEnabled2Fa()) return false;

        user.setEnabled2Fa(true);
        userRepository.save(user);
        return true;
    }
}
