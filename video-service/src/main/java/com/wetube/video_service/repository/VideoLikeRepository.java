package com.wetube.video_service.repository;

import com.wetube.video_service.entity.VideoLike;
import com.wetube.video_service.entity.VideoLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VideoLikeRepository extends JpaRepository<VideoLike, VideoLikeId> {
    @Query(value = "SELECT COUNT(vl) > 0 FROM video_service.video_likes vl WHERE vl.video_id = :video_id AND vl.user_id = :userId", nativeQuery = true)
    boolean existsByVideoIdAndUserId(@Param("videoId") UUID videoId, @Param("userId") UUID userId);

    @Query(value = "DELETE FROM video_service.video_likes vl WHERE vl.video_id = :videoId AND vl.user_id = :userId", nativeQuery = true)
    void removeByVideoIdAndUserId(@Param("videoId") UUID videoId, @Param("userId") UUID userId);

    @Query(value = "SELECT vl FROM video_service.video_likes vl WHERE vl.user_id = :user_id", nativeQuery = true)
    List<VideoLike> findByUserId(@Param("userId") UUID userId);
}
