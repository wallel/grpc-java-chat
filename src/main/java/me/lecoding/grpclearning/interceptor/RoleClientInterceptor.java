package me.lecoding.grpclearning.interceptor;

import io.grpc.*;

public class RoleClientInterceptor implements ClientInterceptor {
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method,callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {

                super.start(responseListener, headers);
            }
        };
    }
}
