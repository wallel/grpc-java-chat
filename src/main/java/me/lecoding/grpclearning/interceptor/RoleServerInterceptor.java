package me.lecoding.grpclearning.interceptor;

import io.grpc.*;
import me.lecoding.grpclearning.common.Constant;
import me.lecoding.grpclearning.user.User;
import me.lecoding.grpclearning.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RoleServerInterceptor implements ServerInterceptor {
    private UserService userService;
    private static Logger logger = LoggerFactory.getLogger(RoleServerInterceptor.class);
    private static final ServerCall.Listener NOOP_LISTENER = new ServerCall.Listener() {};
    @SuppressWarnings("unchecked")
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        Context ctx = Context.current();
        if(!"me.lecoding.grpclearning.ChatRoom/Login".equals(call.getMethodDescriptor().getFullMethodName())){
            String token = headers.get(Constant.HEADER_ROLE);
            if(token == null){
                call.close(Status.UNAUTHENTICATED.withDescription("need login first!"),headers);
                return NOOP_LISTENER;
            }
            User user = userService.findUserByToken(token);
            if(user == null){
                call.close(Status.UNAUTHENTICATED.withDescription("token error!"),headers);
                return NOOP_LISTENER;
            }
            ctx = ctx.withValue(Constant.CONTEXT_ROLE,user);
        }
        return Contexts.interceptCall(ctx,call,headers,next);
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
