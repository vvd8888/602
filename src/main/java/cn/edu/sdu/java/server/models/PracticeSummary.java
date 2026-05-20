package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "practice_summary")
public class PracticeSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer summaryId;

    private Integer projectId;
    private Integer personId;  // nullable, null for team summary

    @Size(max = 50)
    private String personName;

    @Size(max = 10)
    private String summaryType;  // PERSONAL / TEAM

    @Size(max = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Size(max = 20)
    private String submitTime;

    @Size(max = 10)
    private String status;  // DRAFT / SUBMITTED / APPROVED / REJECTED
}
