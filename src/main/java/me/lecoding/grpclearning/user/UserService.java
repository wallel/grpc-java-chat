package me.lecoding.grpclearning.user;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.protobuf.Empty;
import com.google.protobuf.Option;
import com.google.protobuf.util.Timestamps;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import me.lecoding.grpclearning.api.UserOuterClass;
import me.lecoding.grpclearning.api.UserServiceGrpc;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * UserService
 */
@GRpcService
public class UserService extends UserServiceGrpc.UserServiceImplBase {
    private UserRepository userRepository;
    private Gson gson;
    private PasswordEncoder passwordEncoder;

    public User checkUser(String username,String password){
        User user = userRepository.getOneByUsername(username);
        if(user != null && passwordEncoder.matches(password,user.getPassword())){
            return user;
        }
        return null;
    }

    @Override
    public void getUser(UserOuterClass.GetUserRequest request, StreamObserver<UserOuterClass.User> responseObserver) {
        Optional<User> user = userRepository.findById(request.getId());
        if(user.isPresent()){
            responseObserver.onNext(toClientUser(user.get()));
            responseObserver.onCompleted();
        }else{
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("user not exist!").asRuntimeException());
        }
    }

    @Override
    public void listUsers(UserOuterClass.ListUsersRequest request, StreamObserver<UserOuterClass.ListUsersResponse> responseObserver) {
        UserOuterClass.ListUsersResponse.Builder builder = UserOuterClass.ListUsersResponse.newBuilder();
        Pageable pageable;
        if(Strings.isNullOrEmpty(request.getPageToken())){
            pageable = PageRequest.of(1,request.getPageSize());
        }else{
            pageable = gson.fromJson(request.getPageToken(),PageRequest.class);
        }
        Page<User> u = userRepository.findAll(pageable);
        u.forEach(user->builder.addUsers(toClientUser(user)));
        responseObserver.onNext(builder.setNextPageToken(gson.toJson(u.nextPageable())).build());
        responseObserver.onCompleted();
    }

    @Override
    public void createUser(UserOuterClass.CreateUserRequest request, StreamObserver<UserOuterClass.User> responseObserver) {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setCreateTime(new Date(Instant.now().toEpochMilli()));
        user.setUpdateTime(new Date(Instant.now().toEpochMilli()));
        user.setUsername(request.getUsername());
        user.setNickname(request.getNickname());
        try {
            user = userRepository.save(user);
            responseObserver.onNext(toClientUser(user));
            responseObserver.onCompleted();
        }catch (Exception e){
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("create user error").withCause(e).asRuntimeException());
        }
    }

    @Override
    public void searchUsers(UserOuterClass.SearchUsersRequest request, StreamObserver<UserOuterClass.SearchUsersResponse> responseObserver) {
        UserOuterClass.SearchUsersResponse.Builder builder = UserOuterClass.SearchUsersResponse.newBuilder();
        userRepository.findAllById(request.getIdsList()).forEach(user->builder.addUser(toClientUser(user)));
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void updateUser(UserOuterClass.UpdateUserRequest request, StreamObserver<UserOuterClass.User> responseObserver) {
        try {
            Optional<User> user = userRepository.findById(request.getUser().getId());
            if(!user.isPresent()){
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("user not exist").asRuntimeException());
                return;
            }
            final User u = user.get();
            request.getFieldMask().getPathsList().forEach(path->{
                switch (path){
                    case "nickname":
                        u.setNickname(request.getUser().getNickname());
                        break;
                    case "username":
                        u.setUsername(request.getUser().getUsername());
                        break;
                    case "email":
                        u.setEmail(request.getUser().getEmail());
                        break;
                }
            });
            responseObserver.onNext(toClientUser(userRepository.save(u)));
            responseObserver.onCompleted();
        }catch (Exception e){
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("user not exist").withCause(e).asRuntimeException());
        }
    }

    @Override
    public void deleteUser(UserOuterClass.DeleteUserRequest request, StreamObserver<Empty> responseObserver) {
        userRepository.deleteById(request.getId());
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Autowired
    public void setGson(Gson gson) {
        this.gson = gson;
    }
    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    private static UserOuterClass.User toClientUser(User user){
        return UserOuterClass.User.newBuilder()
                .setCreateTime(Timestamps.fromMillis(user.getCreateTime().getTime()))
                .setUpdateTime(Timestamps.fromMillis(user.getUpdateTime().getTime()))
                .setEmail((user.getEmail() == null) ? "" : user.getEmail())
                .setUsername(user.getUsername())
                .setNickname(user.getNickname())
                .setId(user.getId())
                .build();
    }
}
