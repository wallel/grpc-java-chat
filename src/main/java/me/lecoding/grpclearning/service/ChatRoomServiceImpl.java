package me.lecoding.grpclearning.service;

import com.google.common.collect.Sets;
import com.google.protobuf.util.Timestamps;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import me.lecoding.grpclearning.Chat;
import me.lecoding.grpclearning.ChatRoomGrpc;
import me.lecoding.grpclearning.common.Constant;
import me.lecoding.grpclearning.user.User;
import me.lecoding.grpclearning.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;

public class ChatRoomServiceImpl extends ChatRoomGrpc.ChatRoomImplBase {
    private static Logger logger = LoggerFactory.getLogger(ChatRoomServiceImpl.class);
    private Set<StreamObserver<Chat.ChatResponse>> clients = Sets.newConcurrentHashSet();

    @Override
    public void login(Chat.LoginRequest request, StreamObserver<Chat.LoginResponse> responseObserver) {
        if(UserService.getInstance().checkLoged(request.getName())){
            responseObserver.onError(Status.fromCode(Status.ALREADY_EXISTS.getCode()).asRuntimeException());
            return;
        }
        String token = UserService.getInstance().addLogedUser(new User(request.getName()));
        responseObserver.onNext(Chat.LoginResponse.newBuilder().setToken(token).build());
        responseObserver.onCompleted();

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
            logger.info("user logout:{}", user.getUserName());
            UserService.getInstance().logout(user);
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
                logger.info("got message from {} :{}",user.getUserName(),value.getMessage());
                boardCast(Chat.ChatResponse
                        .newBuilder()
                        .setTimestamp(Timestamps.fromMillis(System.currentTimeMillis()))
                        .setRoleMessage(
                                Chat.ChatResponse.Message
                                        .newBuilder()
                                        .setMsg(value.getMessage())
                                        .setName(user.getUserName())
                                        .build()
                        ).build());
            }

            @Override
            public void onError(Throwable t) {
                logger.error("got error from {}",user.getUserName(),t);
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
                                .setName(user.getUserName())
                                .build()
                ).build());
    }
    private void boardCast(Chat.ChatResponse msg){
        for(StreamObserver<Chat.ChatResponse> resp : clients){
            resp.onNext(msg);
        }
    }
}
