package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "practice_project")
public class PracticeProject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer projectId;

    @Size(max = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Size(max = 10)
    private String projectType;  // NORMAL / KEY

    private Integer keyTopicId;  // nullable, only for KEY type

    @Size(max = 15)
    private String status;  // DRAFT / SUBMITTED / APPROVED / REJECTED / IN_PROGRESS / COMPLETED / SUMMARY_SUBMITTED / FINISHED

    private Integer leaderId;
    @Size(max = 50)
    private String leaderName;

    @Size(max = 50)
    private String teamName;

    @Size(max = 50)
    private String mentor;

    @Size(max = 20)
    private String startDate;
    @Size(max = 20)
    private String endDate;

    @Size(max = 20)
    private String createTime;
    @Size(max = 20)
    private String submitTime;
    @Size(max = 20)
    private String approveTime;

    @Column(columnDefinition = "TEXT")
    private String rejectReason;
}
