//package com.example.demo.user.hidden_users;
//
//import com.example.demo.user.User;
//import com.example.demo.user.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/api/hidden-users")
//@RequiredArgsConstructor
//public class HiddenUserController {
//    private final HiddenUserService hiddenUserService;
//    private final CurrentUserService userService;
//    private final UserRepository userRepository;
//
//    @PostMapping("/{userId}")
//    public ResponseEntity<Void> hideUser(
//            @PathVariable String userId
//    ) {
//        com.example.demo.user.User currentUser = userService.getCurrentUser();
//        Optional<com.example.demo.user.User> userToHide = userRepository.findById(userId);
//        hiddenUserService.hideUser(currentUser, userToHide);
//        return ResponseEntity.ok().build();
//    }
//
//    @DeleteMapping("/{userId}")
//    public ResponseEntity<Void> unhideUser(@PathVariable String userId) {
//        com.example.demo.user.User currentUser = userService.getCurrentUser();
//        Optional<User> userToUnhide = userRepository.findById(userId);
//        hiddenUserService.unhideUser(currentUser, userToUnhide);
//        return ResponseEntity.ok().build();
//    }
//}