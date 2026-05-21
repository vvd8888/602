package cn.edu.sdu.java.server.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 校车预约请求
 */
@Data
public class SchoolBusReservationRequest {
    @NotNull(message = "班次ID不能为空")
    private Integer scheduleId;
    
    private String remark;  // 备注
}
