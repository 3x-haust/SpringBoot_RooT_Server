package io.github._3xhaust.root_server.domain.community.repository;

import io.github._3xhaust.root_server.domain.community.entity.CommunityTag;
import io.github._3xhaust.root_server.domain.community.entity.CommunityTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityTagRepository extends JpaRepository<CommunityTag, CommunityTagId> {
    @Query("SELECT ct FROM CommunityTag ct JOIN FETCH ct.tag WHERE ct.community.id = :communityId")
    List<CommunityTag> findByCommunityIdWithTag(@Param("communityId") Long communityId);

    void deleteByCommunityId(Long communityId);

    @Query("SELECT ct.tag.name FROM CommunityTag ct WHERE ct.community.id = :communityId")
    List<String> findTagNamesByCommunityId(@Param("communityId") Long communityId);
}
