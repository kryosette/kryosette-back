package com.example.demo.user;

import com.example.demo.common.BaseEntity;
import com.example.demo.communication.friend.FriendRequest;
import com.example.demo.user.role.Role;
import com.example.demo.security.id_generator.SUUID2;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static jakarta.persistence.FetchType.EAGER;

@Slf4j
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_user")
@EntityListeners(AuditingEntityListener.class)
public class  User extends BaseEntity implements UserDetails, Principal {

    @Id
    @Column(name = "id", columnDefinition = "VARCHAR(32)", length = 32, unique = true, nullable = false)
    private String id;
    private String firstname;
    private String lastname;

    @Column(unique = true)
    private String email;

    @JsonIgnore
    private String password;

    @JsonIgnore
    private boolean accountLocked;

    @JsonIgnore
    private boolean enabled;

    @Column(name = "followers_count")
    private Integer followersCount = 0;

    @Column(name = "is_subscribed")
    private Boolean isSubscribed;

    @Getter
    @Setter
    @Column(name = "secret_key")
    private String secretKey;

    @Getter
    @Setter
    @Column(name = "enabled_2fa")
    private Boolean enabled2Fa;

    @JsonIgnore
    @ManyToMany(fetch = EAGER)
    private List<Role> roles;

    @PrePersist
    public void generateId() {
        if (this.id == null) {
            try {
                SUUID2.setUuidByteLength(4);
                SUUID2.setUuidEntropyLength(100);
                this.id = SUUID2.generateId();
            } catch (RuntimeException e) {
                throw new IllegalStateException("Ошибка при генерации ID.", e);
            }
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles
                .stream()
                .map(r -> new SimpleGrantedAuthority(r.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public String fullName() {
        return getFirstname() + " " + getLastname();
    }

    @Override
    public String getName() {
        return email;
    }

    public String getFullName() {
        return firstname + " " + lastname;
    }

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FriendRequest> sentFriendRequests = new HashSet<>();

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FriendRequest> receivedFriendRequests = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "user_friends",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
    private Set<User> friends = new HashSet<>();
}
