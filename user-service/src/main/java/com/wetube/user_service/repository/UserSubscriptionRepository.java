package com.wetube.user_service.repository;

import com.wetube.user_service.entity.User;
import com.wetube.user_service.entity.UserSubscription;
import com.wetube.user_service.entity.UserSubscriptionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, UserSubscriptionId> {
    @Modifying
    @Query(value = "DELETE FROM user_service.user_subscriptions us WHERE us.channel_id = :channelId AND us.subscriber_id = :subscriberId", nativeQuery = true)
    void deleteByChannelIdAndSubscriberId(@Param("channelId") UUID channelId, @Param("subscriberId") UUID subscriberId);

    @Query(value = "SELECT us.channel_id, us.subscriber_id " +
            "FROM user_service.user_subscriptions us " +
            "WHERE us.channel_id = :channelId " +
            "AND us.subscriber_id = :subscriberId", nativeQuery = true)
    Optional<UserSubscription> findByChannelIdAndSubscriberId(@Param("channelId") UUID channelId,
            @Param("subscriberId") UUID subscriberId);

    @Query(value = "SELECT us.channel_id, us.subscriber_id " +
            "FROM user_service.user_subscriptions us " +
            "WHERE us.channel_id = :channelId", nativeQuery = true)
    List<UserSubscription> findByChannelId(@Param("channelId") UUID channelId);

    @Query(value = "SELECT us.channel_id, us.subscriber_id " +
            "FROM user_service.user_subscriptions us " +
            "WHERE us.subscriber_id = :subscriberId", nativeQuery = true)
    List<UserSubscription> findBySubscriberId(@Param("subscriberId") UUID subscriberId);
}
