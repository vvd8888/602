package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * NoticeTarget 通知目标接收人
 */
@Getter
@Setter
@Entity
@Table(name = "notice_target")
public class NoticeTarget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer targetId;

    private Integer noticeId;

    @Size(max = 15)
    private String targetType;   // ALL / CLASS / PERSON

    @Size(max = 50)
    private String targetValue;  // className 或 personId
}
