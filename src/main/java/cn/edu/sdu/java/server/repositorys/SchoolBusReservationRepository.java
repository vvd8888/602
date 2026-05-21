package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.SchoolBusReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchoolBusReservationRepository extends JpaRepository<SchoolBusReservation, Integer> {
    
    // 根据班次ID查询预约记录
    List<SchoolBusReservation> findByScheduleId(Integer scheduleId);
    
    // 根据人员ID查询预约记录
    List<SchoolBusReservation> findByPersonId(Integer personId);
    
    // 根据人员ID和状态查询预约记录
    List<SchoolBusReservation> findByPersonIdAndStatus(Integer personId, Integer status);
    
    // 根据班次ID和状态统计预约人数
    int countByScheduleIdAndStatus(Integer scheduleId, Integer status);
    
    // 检查某个人是否已经预约了某个班次
    @Query("SELECT COUNT(r) FROM SchoolBusReservation r WHERE r.scheduleId = ?1 AND r.personId = ?2 AND r.status IN (0, 1)")
    int countByScheduleIdAndPersonIdAndActiveStatus(Integer scheduleId, Integer personId);
}
