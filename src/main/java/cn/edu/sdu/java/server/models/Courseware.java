package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "courseware")
public class Courseware {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer coursewareId;

    private String title;
    private String description;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private Integer courseId;
    private String courseName;
    private Integer teacherId;
    private String teacherName;
    private Integer downloadCount;
    private String createTime;
}
