package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * SchoolBusSchedule 校车班次表实体类
 * 保存校车的班次信息，包括发车时间、起点终点校区等
 */
@Getter
@Setter
@Entity
@Table(name = "school_bus_schedule")
public class SchoolBusSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 起点校区ID
    @NotBlank
    @Size(max = 50)
    private String departureCampus;

    // 终点校区ID
    @NotBlank
    @Size(max = 50)
    private String arrivalCampus;

    // 发车时间（格式：HH:mm）
    @NotBlank
    @Size(max = 10)
    private String departureTime;

    // 预计到达时间（格式：HH:mm）
    @Size(max = 10)
    private String arrivalTime;

    // 座位总数
    private Integer totalSeats;

    // 已预约人数
    private Integer reservedSeats;

    // 班次状态：0-正常运营 1-停运 2-取消
    private Integer status;

    // 备注信息
    @Size(max = 200)
    private String remark;

    // 创建时间
    @Size(max = 20)
    private String createTime;

    // 创建人ID
    private Integer creatorId;

    // 更新时间
    @Size(max = 20)
    private String updateTime;

    // 更新人ID
    private Integer updaterId;
}
