package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.KeyTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KeyTopicRepository extends JpaRepository<KeyTopic, Integer> {

    List<KeyTopic> findByStatusOrderByCreateTimeDesc(String status);

    @Query("SELECT k FROM KeyTopic k WHERE k.creatorId = :creatorId AND (:status = '' OR k.status = :status) ORDER BY k.createTime DESC")
    List<KeyTopic> findByCreatorIdAndStatus(@Param("creatorId") Integer creatorId, @Param("status") String status);
}
