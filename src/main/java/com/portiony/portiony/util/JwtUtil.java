//package com.portiony.portiony.util;
//
//import io.jsonwebtoken.*;
//import io.jsonwebtoken.security.Keys;
//import org.springframework.stereotype.Component;
//
//import java.security.Key;
//import java.util.Date;
//
//@Component
//public class JwtUtil {
//
//    // 실제 서비스에선 yml에서 불러오도록 리팩토링 가능
//    private static final String SECRET_KEY = "your-secret-key-for-jwt-signing-must-be-long-enough";
//    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 1일
//
//    private final Key key;
//
//    public JwtUtil() {
//        this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
//    }
//
//    // 토큰 생성
//    public String generateToken(String email) {
//        return Jwts.builder()
//                .setSubject(email)
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
//                .signWith(key, SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//    // 토큰 유효성 검사
//    public boolean validateToken(String token) {
//        try {
//            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
//            return true;
//        } catch (JwtException | IllegalArgumentException e) {
//            return false;
//        }
//    }
//
//    // 토큰에서 이메일 추출
//    public String extractEmail(String token) {
//        return Jwts.parserBuilder().setSigningKey(key).build()
//                .parseClaimsJws(token).getBody().getSubject();
//    }
//}
