package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "response")
public class Response {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer responseId;

    private Integer questionnaireId;

    private Integer personId;      // 匿名时为 null

    @Size(max = 20)
    private String submitTime;

    private Boolean isLate;
}
