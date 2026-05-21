package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * SchoolBusCampus 校区表实体类
 * 保存校区的名称和位置信息
 */
@Getter
@Setter
@Entity
@Table(name = "school_bus_campus")
public class SchoolBusCampus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 校区编码（唯一标识）
    @NotBlank
    @Size(max = 50)
    private String campusCode;

    // 校区名称
    @NotBlank
    @Size(max = 100)
    private String campusName;

    // 校区地址
    @Size(max = 200)
    private String address;

    // 排序号
    private Integer sortOrder;

    // 是否启用：0-禁用 1-启用
    private Integer enabled;

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
