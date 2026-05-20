package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "practice_review")
public class PracticeReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reviewId;

    private Integer projectId;
    private Integer summaryId;  // nullable, null for project-level review

    private Integer reviewerId;
    @Size(max = 50)
    private String reviewerName;

    @Size(max = 10)
    private String reviewType;  // PROJECT / SUMMARY

    @Size(max = 10)
    private String reviewResult;  // APPROVED / REJECTED

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Size(max = 20)
    private String reviewTime;
}
