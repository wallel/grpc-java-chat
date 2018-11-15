package me.lecoding.grpclearning.interceptor;

import io.grpc.*;
import me.lecoding.grpclearning.common.Constant;
import me.lecoding.grpclearning.user.User;
import me.lecoding.grpclearning.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoleServerInterceptor implements ServerInterceptor {
    private static Logger logger = LoggerFactory.getLogger(RoleServerInterceptor.class);
    private static final ServerCall.Listener NOOP_LISTENER = new ServerCall.Listener() {};
    @SuppressWarnings("unchecked")
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        logger.info("call message full name :{}",call.getMethodDescriptor().getFullMethodName());
        Context ctx = Context.current();
        if(!"me.lecoding.grpclearning.ChatRoom/Login".equals(call.getMethodDescriptor().getFullMethodName())){
            String token = headers.get(Constant.HEADER_ROLE);
            if(token == null){
                call.close(Status.UNAUTHENTICATED.withDescription("need login first!"),headers);
                return NOOP_LISTENER;
            }
            User user = UserService.getInstance().findUserByToken(token);
            if(user == null){
                call.close(Status.UNAUTHENTICATED.withDescription("token error!"),headers);
                return NOOP_LISTENER;
            }
            ctx = ctx.withValue(Constant.CONTEXT_ROLE,user);
        }
        return Contexts.interceptCall(ctx,call,headers,next);
    }
}
