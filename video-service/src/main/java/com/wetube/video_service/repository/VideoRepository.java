package com.wetube.video_service.repository;

import com.wetube.video_service.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VideoRepository extends JpaRepository<Video, UUID> {
    Optional<Video> findById(UUID id);

    List<Video> findByUserId(UUID userId);

    @Query(value = "SELECT v.id, v.user_id, v.title, v.description, v.original_filename, v.duration, v.file_size, v.likes, v.tsvector_column, v.status, v.visibility " +
            "FROM video_service.videos v " +
            "WHERE v.tsvector_column @@ to_tsquery(:query) " +
            "AND v.status = 'READY' " +
            "AND v.visibility = 'PUBLIC' " +
            "ORDER BY v.likes",
            nativeQuery = true)
    List<Video> findByQuery(@Param("query") String query);
}
