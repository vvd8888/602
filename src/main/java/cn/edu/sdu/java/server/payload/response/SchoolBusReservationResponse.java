package cn.edu.sdu.java.server.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 校车预约响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchoolBusReservationResponse {
    private Integer id;
    private Integer scheduleId;
    private Integer personId;
    private String personName;  // 预约人姓名
    private String personNum;   // 预约人学号/工号
    private Integer userType;
    private String userTypeText;  // 用户类型文本：学生/教师
    private Integer status;
    private String statusText;  // 状态文本
    private String reservationTime;
    private String cancelTime;
    private String cancelReason;
    private String remark;
    private String createTime;
    
    // 班次信息（嵌套）
    private String departureCampus;
    private String arrivalCampus;
    private String departureTime;
}
