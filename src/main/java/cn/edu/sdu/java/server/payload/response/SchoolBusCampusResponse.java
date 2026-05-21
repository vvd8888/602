package cn.edu.sdu.java.server.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 校区响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchoolBusCampusResponse {
    private Integer id;
    private String campusCode;
    private String campusName;
    private String address;
    private Integer sortOrder;
    private Integer enabled;
    private String remark;
}
