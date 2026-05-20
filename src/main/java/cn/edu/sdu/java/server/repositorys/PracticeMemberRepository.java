package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.PracticeMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PracticeMemberRepository extends JpaRepository<PracticeMember, Integer> {

    List<PracticeMember> findByProjectId(Integer projectId);

    List<PracticeMember> findByPersonId(Integer personId);

    PracticeMember findByProjectIdAndPersonId(Integer projectId, Integer personId);

    void deleteByProjectId(Integer projectId);
}
