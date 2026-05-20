package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "key_topic")
public class KeyTopic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer keyTopicId;

    @Size(max = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    private Integer creatorId;
    @Size(max = 50)
    private String creatorName;

    @Size(max = 20)
    private String createTime;

    @Size(max = 10)
    private String status;  // DRAFT / PUBLISHED / WITHDRAWN
}
