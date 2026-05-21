package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.SchoolBusSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchoolBusScheduleRepository extends JpaRepository<SchoolBusSchedule, Integer> {
    
    // 根据起点和终点校区查询班次
    List<SchoolBusSchedule> findByDepartureCampusAndArrivalCampus(String departureCampus, String arrivalCampus);
    
    // 根据状态查询班次
    List<SchoolBusSchedule> findByStatus(Integer status);
    
    // 根据日期范围和状态查询班次（假设后续扩展支持日期）
    @Query("SELECT s FROM SchoolBusSchedule s WHERE s.status = ?1 ORDER BY s.departureTime")
    List<SchoolBusSchedule> findByStatusOrderByDepartureTime(Integer status);
    
    // 根据创建人查询班次
    List<SchoolBusSchedule> findByCreatorId(Integer creatorId);
}
