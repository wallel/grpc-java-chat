package me.lecoding.grpclearning.interceptor;

import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 异常处理
 */
@Component
public class ExceptionInterceptor implements ServerInterceptor {
    private static Logger logger = LoggerFactory.getLogger(ExceptionInterceptor.class);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        ServerCall.Listener<ReqT> delegate = next.startCall(call,headers);
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(delegate){
            @Override
            public void onHalfClose() {
                try {
                    super.onHalfClose();
                }catch (Exception e){
                    logger.error("get error when call {},",call.getMethodDescriptor().getFullMethodName(),e);
                    call.close(Status.INTERNAL.withCause(e).withDescription(stacktraceToString(e)),new Metadata());
                }
            }
        };
    }
    private String stacktraceToString(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
