package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "score")
@EntityListeners(AuditingEntityListener.class)
public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "score_id")
    private Integer scoreId;

    // 修改为简单的 @JoinColumn
    @ManyToOne
    @JoinColumn(name = "person_id")
    private Student student;

    // 修改为简单的 @JoinColumn
    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    private Integer mark;
    private Integer ranking;

    @Column(name = "selection_status")
    private String selectionStatus = "PENDING";

    @Column(name = "apply_time")
    @CreatedDate
    private Date applyTime;

    @Column(name = "approve_time")
    private Date approveTime;

    @Column(name = "approve_by")
    private Integer approveBy;

    @Column(name = "reject_reason", length = 255)
    private String rejectReason;

    public Score() {
        // 构造器为空
    }
}