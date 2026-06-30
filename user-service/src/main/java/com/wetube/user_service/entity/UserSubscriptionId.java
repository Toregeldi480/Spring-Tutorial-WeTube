package com.wetube.user_service.entity;

import java.io.Serializable;
import java.util.UUID;

public class UserSubscriptionId implements Serializable {
    private UUID channelId;
    private UUID subscriberId;

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
}
