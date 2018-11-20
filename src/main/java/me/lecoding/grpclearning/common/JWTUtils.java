package me.lecoding.grpclearning.common;

import com.nimbusds.jose.*;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JWTUtils {
    private static final long DEFAULT_EXPIRE_TIME = 30 * 60 * 1000;

    private JWSSigner jwtSinger;
    private JWSVerifier jwtVerifier;

    public String generateToken(String username){
        JWTClaimsSet claimSet = new JWTClaimsSet.Builder()
                .subject(username)
                .expirationTime(new Date(new Date().getTime() + DEFAULT_EXPIRE_TIME))
                .build();
        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimSet);
        try {
            signedJWT.sign(jwtSinger);
        } catch (JOSEException e) {
            e.printStackTrace();
        }
        return signedJWT.serialize();
    }

    public String checkToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            if(!signedJWT.verify(jwtVerifier))return null;
            return signedJWT.getJWTClaimsSet().getSubject();
        }catch (Exception e) {
            return null;
        }
    }

    @Autowired
    public void setJwtSinger(JWSSigner jwtSinger) {
        this.jwtSinger = jwtSinger;
    }

    @Autowired
    public void setJwtVerifier(JWSVerifier jwtVerifier) {
        this.jwtVerifier = jwtVerifier;
    }
}