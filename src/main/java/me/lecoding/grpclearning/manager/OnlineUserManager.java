package me.lecoding.grpclearning.manager;

import com.google.common.collect.Maps;
import me.lecoding.grpclearning.user.User;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OnlineUserManager {
    private Map<String, User> onlineUsers = Maps.newHashMap();

    public void addUser(User user){
        onlineUsers.put(user.getId(),user);
    }
    public void removeUserById(String userId){
        onlineUsers.remove(userId);
    }
    public User findUserById(String userId){
        return onlineUsers.get(userId);
    }
}
