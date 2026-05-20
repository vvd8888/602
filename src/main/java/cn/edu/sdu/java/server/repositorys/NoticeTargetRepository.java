package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.NoticeTarget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeTargetRepository extends JpaRepository<NoticeTarget, Integer> {

    List<NoticeTarget> findByNoticeId(Integer noticeId);

    void deleteByNoticeId(Integer noticeId);
}
