package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.NoticeRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface NoticeReadRepository extends JpaRepository<NoticeRead, Integer> {

    Optional<NoticeRead> findByNoticeIdAndPersonId(Integer noticeId, Integer personId);

    List<NoticeRead> findByNoticeId(Integer noticeId);

    @Query("SELECT nr.personId FROM NoticeRead nr WHERE nr.noticeId = :noticeId")
    List<Integer> findPersonIdListByNoticeId(@Param("noticeId") Integer noticeId);

    @Modifying
    @Transactional
    void deleteByNoticeId(Integer noticeId);

    long countByNoticeId(Integer noticeId);
}
