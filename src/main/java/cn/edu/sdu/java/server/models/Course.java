package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "course", uniqueConstraints = {})
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer courseId;

    @NotBlank
    @Size(max = 20)
    private String num;

    @Size(max = 50)
    private String name;

    private Integer credit;

    @ManyToOne
    @JoinColumn(name = "pre_course_id")
    private Course preCourse;

    @Size(max = 12)
    private String coursePath;

    // 新增字段：教师模式需要
    @Size(max = 50)
    private String teacher;    // 授课教师

    @Size(max = 50)
    private String time;       // 上课时间

    @Size(max = 50)
    private String classroom;  // 上课地点

    @Column(name = "max_capacity")
    private Integer maxCapacity;  // 最大选课人数

    @Column(name = "current_enrolled")
    private Integer currentEnrolled = 0;  // 当前已选人数

    @Column(name = "status")
    private String status = "OPEN";  // 默认值为 OPEN
}