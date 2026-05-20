package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "practice_member")
public class PracticeMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer memberId;

    private Integer projectId;
    private Integer personId;

    @Size(max = 50)
    private String personName;

    @Size(max = 50)
    private String className;

    @Size(max = 10)
    private String role;  // LEADER / MEMBER
}
