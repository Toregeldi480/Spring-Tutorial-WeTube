package com.wetube.video_service.repository;

import com.wetube.video_service.entity.VideoRating;
import com.wetube.video_service.entity.VideoRatingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VideoRatingRepository extends JpaRepository<VideoRating, VideoRatingId> {
    @Modifying
    @Query(value = "DELETE FROM video_service.video_ratings vr WHERE vr.video_id = :videoId AND vr.user_id = :userId", nativeQuery = true)
    void removeByVideoIdAndUserId(@Param("videoId") UUID videoId, @Param("userId") UUID userId);

    @Query(value = "SELECT vr.video_id, vr.user_id, vr.is_liked " +
            "FROM video_service.video_ratings vr " +
            "WHERE vr.user_id = :userId " +
            "AND vr.is_liked = true",
            nativeQuery = true)
    List<VideoRating> findLikesByUserId(@Param("userId") UUID userId);

    @Query(value = "SELECT vr.video_id, vr.user_id, vr_is_liked " +
            "FROM video_service.video_ratings vr " +
            "WHERE vr.user_id = :userId " +
            "AND vr.is_liked = false",
            nativeQuery = true)
    List<VideoRating> findDislikesByUserId(@Param("userId") UUID userId);

    @Query(value = "SELECT vr.video_id, vr.user_id, vr.is_liked " +
            "FROM video_service.video_ratings vr " +
            "WHERE vr.video_id = :videoId " +
            "AND vr.user_id = :userId",
            nativeQuery = true)
    Optional<VideoRating> findByVideoIdAndUserId(@Param("videoId") UUID videoId, @Param("userId") UUID userId);
}
