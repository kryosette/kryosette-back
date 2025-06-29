package com.posts.post.main;

import com.posts.post.common.BaseDate;
import lombok.*;
import jakarta.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Manuo extends BaseDate {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Integer id;

    private String email;
    private String name;
    private String billing_address;
    private String shipping_address;

}
