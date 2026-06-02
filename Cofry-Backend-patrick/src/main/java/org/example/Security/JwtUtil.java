package org.example.Security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import java.util.Date;

public class JwtUtil {
    private static final String SECRET = "cofry_super_secret_key_123456789";
    private static final String ISSUER = "cofry-api";
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET);

    // Gera um token válido por 24 horas contendo o userId e o email
    public static String gerarToken(Long userId, String email) {
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(email)
                .withClaim("userId", userId)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 86400000)) // 24 horas
                .sign(ALGORITHM);
    }

    // Valida o token e retorna o userId dele. Retorna null se for inválido/expirado
    public static Long verificarToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        try {
            // Remove o prefixo "Bearer " se presente
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            JWTVerifier verifier = JWT.require(ALGORITHM)
                    .withIssuer(ISSUER)
                    .build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaim("userId").asLong();
        } catch (Exception e) {
            System.out.println("Erro na validação do token JWT: " + e.getMessage());
            return null;
        }
    }
}
