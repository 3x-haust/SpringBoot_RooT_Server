package io.github._3xhaust.root_server.domain.community.repository;

import io.github._3xhaust.root_server.domain.community.entity.CommunityGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommunityGradeRepository extends JpaRepository<CommunityGrade, Long> {
    Optional<CommunityGrade> findByGradeLevel(Short gradeLevel);
}
