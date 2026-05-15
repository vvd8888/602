package cn.edu.sdu.java.server.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/auth")
    public String testAuth(Authentication authentication) {
        System.out.println("===== 认证信息测试 =====");
        System.out.println("Principal 类型: " + (authentication != null ? authentication.getPrincipal().getClass().getName() : "null"));
        System.out.println("Principal: " + (authentication != null ? authentication.getPrincipal() : "null"));
        System.out.println("Authorities: " + (authentication != null ? authentication.getAuthorities() : "null"));
        System.out.println("Name: " + (authentication != null ? authentication.getName() : "null"));
        System.out.println("Credentials: " + (authentication != null ? authentication.getCredentials() : "null"));
        System.out.println("Details: " + (authentication != null ? authentication.getDetails() : "null"));
        System.out.println("=====================================");

        if (authentication != null && authentication.getPrincipal() != null) {
            return "Principal: " + authentication.getPrincipal() + "\n" +
                    "Class: " + authentication.getPrincipal().getClass().getName() + "\n" +
                    "Authorities: " + authentication.getAuthorities();
        }
        return "No authentication information";
    }
}