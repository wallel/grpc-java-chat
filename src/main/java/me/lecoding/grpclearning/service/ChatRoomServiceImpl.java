package me.lecoding.grpclearning.service;

import com.google.common.collect.Sets;
import com.google.protobuf.util.Timestamps;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import me.lecoding.grpclearning.Chat;
import me.lecoding.grpclearning.ChatRoomGrpc;
import me.lecoding.grpclearning.common.Constant;
import me.lecoding.grpclearning.common.JWTUtils;
import me.lecoding.grpclearning.interceptor.RoleServerInterceptor;
import me.lecoding.grpclearning.manager.OnlineUserManager;
import me.lecoding.grpclearning.user.User;
import me.lecoding.grpclearning.user.UserService;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;
import java.util.Set;

@GRpcService(interceptors = {RoleServerInterceptor.class})
public class ChatRoomServiceImpl extends ChatRoomGrpc.ChatRoomImplBase {
    private UserService userService;
    private OnlineUserManager onlineUserManager;
    private JWTUtils jwtUtils;
    private static Logger logger = LoggerFactory.getLogger(ChatRoomServiceImpl.class);
    private Set<StreamObserver<Chat.ChatResponse>> clients = Sets.newConcurrentHashSet();

    @Override
    public void login(Chat.LoginRequest request, StreamObserver<Chat.LoginResponse> responseObserver) {
        User user= userService.checkUser(request.getName(),request.getPassword());
        if(Objects.isNull(user)){
            responseObserver.onError(Status.fromCode(Status.UNAUTHENTICATED.getCode()).withDescription("uasername or password error").asRuntimeException());
            return;
        }
        onlineUserManager.addUser(user);
        responseObserver.onNext(Chat.LoginResponse.newBuilder().setToken(jwtUtils.generateToken(user.getId())).build());
        responseObserver.onCompleted();
        logger.info("user {} login OK!",request.getName());
        boardCast(Chat.ChatResponse
            .newBuilder()
            .setTimestamp(Timestamps.fromMillis(System.currentTimeMillis()))
            .setRoleLogin(
                Chat.ChatResponse.Login
                    .newBuilder()
                    .setName(request.getName())
                    .build()
            ).build());
    }

    @Override
    public void logout(Chat.LogoutRequest request, StreamObserver<Chat.LogoutResponse> responseObserver) {
        User user = Constant.CONTEXT_ROLE.get();
        if(!Objects.isNull(user)) {
            logger.info("user logout:{}", user.getUsername());
            onlineUserManager.removeUserById(user.getId());
        }
        responseObserver.onNext(Chat.LogoutResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<Chat.ChatRequest> chat(StreamObserver<Chat.ChatResponse> responseObserver) {
        clients.add(responseObserver);
        User user = Constant.CONTEXT_ROLE.get();
        if(Objects.isNull(user)) {
            responseObserver.onError(Status.UNAUTHENTICATED.withDescription("need login first").asRuntimeException());
            return null;
        }


        return new StreamObserver<Chat.ChatRequest>() {
            @Override
            public void onNext(Chat.ChatRequest value) {
                logger.info("got message from {} :{}",user.getNickname(),value.getMessage());
                boardCast(Chat.ChatResponse
                        .newBuilder()
                        .setTimestamp(Timestamps.fromMillis(System.currentTimeMillis()))
                        .setRoleMessage(
                                Chat.ChatResponse.Message
                                        .newBuilder()
                                        .setMsg(value.getMessage())
                                        .setName(user.getNickname())
                                        .build()
                        ).build());
            }

            @Override
            public void onError(Throwable t) {
                logger.error("got error from {}",user.getNickname(),t);
                userLogout(responseObserver,user);
            }
            @Override
            public void onCompleted() {
                userLogout(responseObserver,user);
            }
        };
    }

    private void userLogout(StreamObserver<Chat.ChatResponse> responseObserver,User user){
        clients.remove(responseObserver);
        boardCast(Chat.ChatResponse
                .newBuilder()
                .setTimestamp(Timestamps.fromMillis(System.currentTimeMillis()))
                .setRoleLogout(
                        Chat.ChatResponse.Logout
                                .newBuilder()
                                .setName(user.getNickname())
                                .build()
                ).build());
    }
    private void boardCast(Chat.ChatResponse msg){
        for(StreamObserver<Chat.ChatResponse> resp : clients){
            resp.onNext(msg);
        }
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    @Autowired
    public void setJwtUtils(JWTUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }
    @Autowired
    public void setOnlineUserManager(OnlineUserManager onlineUserManager) {
        this.onlineUserManager = onlineUserManager;
    }
}
