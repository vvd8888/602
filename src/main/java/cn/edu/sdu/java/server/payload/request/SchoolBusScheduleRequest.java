package cn.edu.sdu.java.server.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 校车班次保存请求
 */
@Data
public class SchoolBusScheduleRequest {
    private Integer id;  // 编辑时传入
    
    @NotBlank(message = "起点校区不能为空")
    private String departureCampus;
    
    @NotBlank(message = "终点校区不能为空")
    private String arrivalCampus;
    
    @NotBlank(message = "发车时间不能为空")
    private String departureTime;
    
    private String arrivalTime;  // 预计到达时间
    
    @NotNull(message = "座位总数不能为空")
    private Integer totalSeats;
    
    private Integer status;  // 班次状态
    
    private String remark;  // 备注
}
