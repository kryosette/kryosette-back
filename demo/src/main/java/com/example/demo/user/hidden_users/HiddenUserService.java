package com.example.demo.user.hidden_users;

import com.example.demo.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

//@Service
//@RequiredArgsConstructor
//public class HiddenUserService {
//    private final HiddenUserRepository hiddenUserRepository;
//
//    public void hideUser(User currentUser, Optional<User> userToHide) {
//        if (!hiddenUserRepository.existsByUserAndHiddenUser(currentUser, userToHide)) {
//            HiddenUser hiddenUser = new HiddenUser();
//            hiddenUser.setUser(currentUser);
//            hiddenUser.setHiddenUser(userToHide);
//            hiddenUserRepository.save(hiddenUser);
//        }
//    }
//
//    public void unhideUser(User currentUser, Optional<User> userToUnhide) {
//        hiddenUserRepository.deleteByUserAndHiddenUser(currentUser, userToUnhide);
//    }
//
//    public List<Long> getHiddenUserIds(User currentUser) {
//        return hiddenUserRepository.findByUser(currentUser).stream()
//                .map(hiddenUser -> hiddenUser.getHiddenUser().getId())
//                .collect(Collectors.toList());
//    }
//}