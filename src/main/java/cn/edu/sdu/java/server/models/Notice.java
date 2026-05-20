package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Notice 通知主表
 */
@Getter
@Setter
@Entity
@Table(name = "notice")
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer noticeId;

    @Size(max = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Size(max = 20)
    private String noticeType;   // SYSTEM / COURSE / ACTIVITY

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

