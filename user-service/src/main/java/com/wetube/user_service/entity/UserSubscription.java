package com.wetube.user_service.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Table(name = "user_subscriptions")
@Entity
@IdClass(UserSubscriptionId.class)
public class UserSubscription {
    public UserSubscription() {
    }

    public UserSubscription(UUID channelId, UUID subscriberId) {
        this.channelId = channelId;
        this.subscriberId = subscriberId;
    }

    @Id
    @Column(name = "channel_id")
    private UUID channelId;

    @Id
    @Column(name = "subscriber_id")
    private UUID subscriberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", insertable = false, updatable = false)
    private User channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_id", insertable = false, updatable = false)
    private User subscriber;

    public UUID getChannelId() {
        return channelId;
    }

    public void setChannelId(UUID channelId) {
        this.channelId = channelId;
    }

    public UUID getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(UUID subscriberId) {
        this.subscriberId = subscriberId;
    }

    public User getChannel() {
        return channel;
    }

    public void setChannel(User channel) {
        this.channel = channel;
    }

    public User getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(User subscriber) {
        this.subscriber = subscriber;
    }
}
