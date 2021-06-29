package com.jtm.account.core.util

import com.jtm.account.core.domain.entity.Role
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

class UtilJwt {
    companion object {
        fun accessToken(key: String, id: UUID, email: String, roles: List<Role>): String {
            val currentTime = System.currentTimeMillis()
            val exp = currentTime + TimeUnit.MINUTES.toMillis(10)

            return Jwts.builder().signWith(SignatureAlgorithm.HS256, key)
                .setSubject(email)
                .setIssuedAt(Date(currentTime))
                .setExpiration(Date(exp))
                .claim("roles", roles.stream().map { "ROLE_" + it.name }.collect(Collectors.toList()))
                .claim("id", id.toString())
                .compact()
        }

        fun cookieAccessToken(key: String, email: String, roles: List<Role>): String {
            val currentTime = System.currentTimeMillis()
            val exp = currentTime + TimeUnit.MINUTES.toMillis(10)

            return Jwts.builder().signWith(SignatureAlgorithm.HS256, key)
                .setSubject(email)
                .setIssuedAt(Date(currentTime))
                .setExpiration(Date(exp))
                .claim("roles", roles.stream().map { "ROLE_" + it.name }.collect(Collectors.toList()))
                .claim("session", true)
                .compact()
        }

        fun refreshToken(key: String, email: String, roles: List<Role>): String {
            val currentTime = System.currentTimeMillis()
            val exp = currentTime + TimeUnit.DAYS.toMillis(30)

            return Jwts.builder().signWith(SignatureAlgorithm.HS256, key)
                .setSubject(email)
                .setIssuedAt(Date(currentTime))
                .setExpiration(Date(exp))
                .claim("roles", roles.stream().map { "ROLE_" + it.name }.collect(Collectors.toList()))
                .compact()
        }

        fun passwordResetToken(key: String, email: String, id: UUID): String {
            val currentTime = System.currentTimeMillis()
            val exp = currentTime + TimeUnit.DAYS.toMillis(1)

            return Jwts.builder().signWith(SignatureAlgorithm.HS256, key)
                .setSubject(email)
                .setIssuedAt(Date(currentTime))
                .setExpiration(Date(exp))
                .claim("id", id)
                .compact()
        }

        fun getEmail(key: String, token: String): String {
            return Jwts.parser().setSigningKey(key).parseClaimsJws(token).body.subject
        }

        fun getClaims(key: String, token: String): Jws<Claims> {
            return Jwts.parser().setSigningKey(key).parseClaimsJws(token)
        }
    }
}