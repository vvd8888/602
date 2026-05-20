package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "question")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer questionId;

    private Integer questionnaireId;

    @Size(max = 15)
    private String questionType;   // SINGLE / MULTI / TEXT / SCALE

    @Size(max = 200)
    private String title;

    private Boolean required;
    private Integer sortOrder;

    private Integer scaleMin;      // 仅 SCALE
    private Integer scaleMax;      // 仅 SCALE
}

