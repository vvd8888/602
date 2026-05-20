package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * NoticeRead 已读记录
 */
@Getter
@Setter
@Entity
@Table(name = "notice_read")
public class NoticeRead {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer readId;

    private Integer noticeId;
    private Integer personId;

    @Size(max = 20)
    private String readTime;
}