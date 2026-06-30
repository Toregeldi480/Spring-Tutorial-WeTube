package com.wetube.user_service.service;

import com.wetube.user_service.dto.UserDto;
import com.wetube.user_service.entity.User;
import com.wetube.user_service.entity.UserSubscription;
import com.wetube.user_service.repository.UserRepository;
import com.wetube.user_service.repository.UserSubscriptionRepository;
import com.wetube.user_service.util.UserMapper;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;

    public UserService(UserMapper userMapper, UserRepository userRepository,
            UserSubscriptionRepository userSubscriptionRepository) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
    }

    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return userMapper.toDto(user);
    }

    public Iterable<UserDto> all() {
        Iterable<User> allUsers = userRepository.findAll();
        ArrayList<UserDto> allUserDtos = new ArrayList<>();
        allUsers.forEach(user -> allUserDtos.addLast(userMapper.toDto(user)));
        return allUserDtos;
    }

    public String subscribe(String channelId, String subscriberId) {
        UUID channelUUID = UUID.fromString(channelId);
        UUID subscriberUUID = UUID.fromString(subscriberId);
        Optional<UserSubscription> userSubscription = userSubscriptionRepository
                .findByChannelIdAndSubscriberId(channelUUID, subscriberUUID);

        if (userSubscription.isEmpty()) {
            User channel = userRepository.findById(channelUUID).orElseThrow();
            channel.setSubscribers(channel.getSubscribers() + 1);
            userSubscriptionRepository.save(new UserSubscription(channelUUID, subscriberUUID));
            return "Subscribed To Channel With ID: " + channelId;
        }

        return "Already Subscribed To Channel With ID: " + channelId;
    }

    public String unsubscribe(String channelId, String subscriberId) {
        UUID channelUUID = UUID.fromString(channelId);
        UUID subscriberUUID = UUID.fromString(subscriberId);
        Optional<UserSubscription> userSubscription = userSubscriptionRepository
                .findByChannelIdAndSubscriberId(channelUUID, subscriberUUID);

        if (!userSubscription.isEmpty()) {
            User channel = userRepository.findById(channelUUID).orElseThrow();
            channel.setSubscribers(channel.getSubscribers() - 1);
            userSubscriptionRepository.deleteByChannelIdAndSubscriberId(channelUUID, subscriberUUID);
            return "Unsubscribed From Channel With ID: " + channelId;
        }

        return "You Are Not Subscribed To Channel With ID: " + channelId;
    }

    public List<UserDto> getChannelSubscribers(String channelId) {
        List<UserSubscription> userSubscriptions = userSubscriptionRepository
                .findByChannelId(UUID.fromString(channelId));

        return userSubscriptions.stream().map(subscriber -> userMapper.toDto(subscriber.getSubscriber()))
                .collect(Collectors.toList());
    }

    public List<UserDto> getUserSubscriptions(String userId) {
        List<UserSubscription> userSubscriptions = userSubscriptionRepository.findByChannelId(UUID.fromString(userId));

        return userSubscriptions.stream().map(channel -> userMapper.toDto(channel.getChannel()))
                .collect(Collectors.toList());
    }
}
