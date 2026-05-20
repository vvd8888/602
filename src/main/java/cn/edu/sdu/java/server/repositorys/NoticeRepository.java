package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Integer> {

    List<Notice> findByStatusOrderByPublishTimeDesc(String status);

    List<Notice> findByStatusNotOrderByPublishTimeDesc(String status);

    @Query("SELECT n FROM Notice n WHERE n.creatorId = :creatorId AND (:status = '' OR n.status = :status) ORDER BY n.createTime DESC")
    List<Notice> findByCreatorIdAndStatus(@Param("creatorId") Integer creatorId, @Param("status") String status);
}