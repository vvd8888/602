package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "questionnaire")
public class Questionnaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer questionnaireId;

    @Size(max = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Boolean anonymous;
    @Size(max = 20)
    private String deadline;
    private Boolean allowLate;
    private Boolean allowModify;

    @Size(max = 10)
    private String status;       // DRAFT / PUBLISHED / WITHDRAWN

    private Integer creatorId;
    @Size(max = 50)
    private String creatorName;

    @Size(max = 20)
    private String createTime;
    @Size(max = 20)
    private String publishTime;
}

