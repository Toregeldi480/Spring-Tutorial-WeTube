package com.wetube.video_service.repository;

import com.wetube.video_service.entity.Video;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VideoRepository extends CrudRepository<Video, UUID> {
    Optional<Video> findById(UUID id);

    @Query(value = "SELECT * FROM video_service.videos v WHERE " +
            "to_tsvector('english', v.title || ' ' || v.description) " +
            "@@ to_tsquery('english', :keyword)",
            nativeQuery = true)
    List<Video> searchByKeyword(@Param("keyword") String keyword);
}
