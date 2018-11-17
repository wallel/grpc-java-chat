package me.lecoding.grpclearning.user;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Dummy UserService
 */
@Service
public class UserService {
    private Map<String,User> token2User = Maps.newHashMap();
    private Set<String> LogedUsers = Sets.newHashSet();

    public User findUserByToken(String token){
        return token2User.get(token);
    }

    public String addLogedUser(User user){
        String token = UUID.randomUUID().toString();
        user.setToken(token);
        LogedUsers.add(user.getUserName());
        token2User.put(token,user);
        return token;
    }

    public boolean checkLoged(String name){
        return LogedUsers.contains(name);
    }

    public void logout(User user){
        LogedUsers.remove(user.getUserName());
        token2User.remove(user.getToken());
    }
}
