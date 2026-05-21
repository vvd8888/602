package cn.edu.sdu.java.server.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 校车班次响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchoolBusScheduleResponse {
    private Integer id;
    private String departureCampus;
    private String arrivalCampus;
    private String departureTime;
    private String arrivalTime;
    private Integer totalSeats;
    private Integer reservedSeats;
    private Integer status;
    private String statusText;  // 状态文本：正常运营/停运/取消
    private String remark;
    private String createTime;
    
    // 计算剩余座位数
    public Integer getAvailableSeats() {
        if (totalSeats != null && reservedSeats != null) {
            return totalSeats - reservedSeats;
        }
        return totalSeats;
    }
}
