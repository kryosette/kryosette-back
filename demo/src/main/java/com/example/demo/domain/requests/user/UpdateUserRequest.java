package com.example.demo.domain.requests.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
}