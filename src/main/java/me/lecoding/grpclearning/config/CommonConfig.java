package me.lecoding.grpclearning.config;

import com.google.gson.Gson;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class CommonConfig {
    @Value("${jwt.secret}")
    private String secret;

    @Bean
    public Gson gson(){
        return new Gson();
    }
    @Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }


    @Bean
    public JWSSigner jwtSinger(){
        try {
            return new MACSigner(secret);
        }catch (Exception e){
            throw new RuntimeException("create signer error",e);
        }
    }

    @Bean
    public JWSVerifier jwtVerifier(){
        try {
            return new MACVerifier(secret);
        }catch (Exception e){
            throw new RuntimeException("create signer error",e);
        }
    }
}
