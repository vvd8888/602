package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.SchoolBusCampus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolBusCampusRepository extends JpaRepository<SchoolBusCampus, Integer> {
    
    // 根据校区编码查询
    Optional<SchoolBusCampus> findByCampusCode(String campusCode);
    
    // 查询所有启用的校区，按排序号排序
    List<SchoolBusCampus> findByEnabledOrderBySortOrder(Integer enabled);
    
    // 查询所有校区，按排序号排序
    List<SchoolBusCampus> findAllByOrderBySortOrder();
}
