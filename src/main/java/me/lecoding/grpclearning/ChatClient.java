package me.lecoding.grpclearning;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import me.lecoding.grpclearning.common.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class ChatClient {
    private static Logger logger = LoggerFactory.getLogger(ChatClient.class);
    private final ManagedChannel channel;
    private ChatRoomGrpc.ChatRoomBlockingStub blockingStub;
    private StreamObserver<Chat.ChatRequest> chat;
    private String token = "";
    private boolean Loggined = false;
    public ChatClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext().build());
    }

    private ChatClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = ChatRoomGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public boolean login(String name) {
        Chat.LoginRequest request = Chat.LoginRequest.newBuilder().setName(name).build();
        Chat.LoginResponse response;
        try {
            response = blockingStub.login(request);
        } catch (StatusRuntimeException e) {
            logger.error("rpc failed with status:" + e.getStatus() + " message:" + e.getMessage());
            return false;
        }
        logger.info("login with name {} OK!",name);
        this.token = response.getToken();
        this.Loggined = true;
        startReceive();
        return true;
    }

    private void startReceive(){
        Metadata meta = new Metadata();
        meta.put(Constant.HEADER_ROLE,this.token);

        chat =  MetadataUtils.attachHeaders(ChatRoomGrpc.newStub(this.channel),meta).chat(new StreamObserver<Chat.ChatResponse>() {
            @Override
            public void onNext(Chat.ChatResponse value) {
                switch (value.getEventCase()){
                    case ROLE_LOGIN:
                    {
                        logger.info("user {}:login!!",value.getRoleLogin().getName());
                    }
                    break;
                    case ROLE_LOGOUT:
                    {
                        logger.info("user {}:logout!!",value.getRoleLogout().getName());
                    }
                    break;
                    case ROLE_MESSAGE:
                    {
                        logger.info("user {}:{}",value.getRoleMessage().getName(),value.getRoleMessage().getMsg());
                    }
                    break;
                    case EVENT_NOT_SET:
                    {
                        logger.error("receive event error:{}",value);
                    }
                    break;
                    case SERVER_SHUTDOWN:
                    {
                        logger.info("server closed!");
                        logout();
                    }
                    break;
                }
            }
            @Override
            public void onError(Throwable t) {
                logger.error("got error from server:{}",t.getMessage(),t);
            }

            @Override
            public void onCompleted() {
                logger.info("closed by server");
            }
        });
        Metadata header = new Metadata();
        header.put(Constant.HEADER_ROLE,this.token);
    }

    public void sendMessage(String msg) throws InterruptedException {
        if("LOGOUT".equals(msg)){
            this.chat.onCompleted();
            this.logout();
            this.Loggined = false;
            shutdown();
        }else{
            if(this.chat != null) this.chat.onNext(Chat.ChatRequest.newBuilder().setMessage(msg).build());
        }
    }

    public void logout(){
        Chat.LogoutResponse resp = blockingStub.logout(Chat.LogoutRequest.newBuilder().build());
        logger.info("logout result:{}",resp);
    }

    public static void main(String[] args) throws InterruptedException {
        ChatClient client = new ChatClient("localhost", 8000);
        try {
            String name = "";
            Scanner sc = new Scanner(System.in);
            do{
                System.out.println("please input your nickname");
                name = sc.nextLine();
            }while (!client.login(name));

            while(client.Loggined){
                name = sc.nextLine();
                if(client.Loggined)client.sendMessage(name);
            }
        } finally {
            client.shutdown();
        }
    }
}
