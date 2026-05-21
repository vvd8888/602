package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * SchoolBusReservation 校车预约表实体类
 * 保存学生和教师对校车的预约信息
 */
@Getter
@Setter
@Entity
@Table(name = "school_bus_reservation")
public class SchoolBusReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 关联的班次ID
    private Integer scheduleId;

    // 预约人ID（学生或教师的personId）
    private Integer personId;

    // 预约人类型：1-学生 2-教师
    private Integer userType;

    // 预约状态：0-待确认 1-已确认 2-已取消 3-已完成
    private Integer status;

    // 预约时间
    @Size(max = 20)
    private String reservationTime;

    // 取消时间
    @Size(max = 20)
    private String cancelTime;

    // 取消原因
    @Size(max = 200)
    private String cancelReason;

    // 备注信息
    @Size(max = 200)
    private String remark;

    // 创建时间
    @Size(max = 20)
    private String createTime;

    // 更新时间
    @Size(max = 20)
    private String updateTime;
}
