package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
@Getter
@Setter
public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private Integer id;

    private String username;
    private String perName;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Integer id, String username, String password,String perName,
                           Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.perName = perName;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // 检查 userType 是否为 null
        if (user.getUserType() == null) {
            throw new RuntimeException("用户 " + user.getUserName() + " 的 userType 为 null，请检查数据库 user 表的 user_type_id 字段");
        }
        
        // 检查 userType.getName() 是否为 null
        if (user.getUserType().getName() == null) {
            throw new RuntimeException("用户 " + user.getUserName() + " 的 userType.name 为 null");
        }
        
        authorities.add(new SimpleGrantedAuthority(user.getUserType().getName()));
        
        // 检查 person 是否为 null
        String perName = "";
        if (user.getPerson() != null) {
            perName = user.getPerson().getName() != null ? user.getPerson().getName() : "";
        } else {
            System.err.println("警告：用户 " + user.getUserName() + " 的 person 为 null");
        }

        return new UserDetailsImpl(
                user.getPersonId(),
                user.getUserName(),
                user.getPassword(),
                perName,
                authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}