package com.jtm.account.core.usecase.token

import com.jtm.account.core.domain.entity.AccountProfile
import com.jtm.account.core.util.UtilJwt
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.util.*

@Component
open class TokenProvider {

    @Value("\${security.jwt.access-key:accessKey}")
    lateinit var accessKey: String

    @Value("\${security.jwt.refresh-key:refreshKey}")
    lateinit var refreshKey: String

    @Value("\${security.jwt.api-key:apiKey}")
    lateinit var apiKey: String

    @Value("\${security.jwt.request-key:requestKey}")
    lateinit var requestKey: String

    @Value("\${security.jwt.verify-key:verifyKey}")
    lateinit var verifyKey: String

    fun createAccessToken(profile: AccountProfile): String {
        return UtilJwt.accessToken(accessKey, profile.id, profile.email, profile.roles)
    }

    fun createAccessCookieToken(profile: AccountProfile): String {
        return UtilJwt.cookieAccessToken(accessKey, profile.email, profile.roles)
    }

    fun createRefreshToken(profile: AccountProfile): String {
        return UtilJwt.refreshToken(refreshKey, profile.email, profile.roles)
    }

    fun createVerificationToken(email: String): String {
        return UtilJwt.verifyToken(verifyKey, email)
    }

    fun createRequestToken(email: String): String {
        return UtilJwt.passwordResetToken(requestKey, email)
    }

    fun getEmail(token: String): String {
        return UtilJwt.getEmail(accessKey, token)
    }

    fun getEmailRefresh(token: String): String? {
        return try {
            UtilJwt.getEmail(refreshKey, token)
        } catch (ex: Exception) {
            null
        }
    }

    fun getEmailPasswordReset(token: String): String {
        return UtilJwt.getEmail(requestKey, token)
    }

    fun getClaimsPasswordReset(token: String): Jws<Claims> {
        return UtilJwt.getClaims(requestKey, token)
    }

    fun getClaimsAccessToken(token: String): Jws<Claims> {
        return UtilJwt.getClaims(accessKey, token)
    }

    fun getClaimsRefreshToken(token: String): Jws<Claims> {
        return UtilJwt.getClaims(refreshKey, token)
    }

    @Bean
    open fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(10)
    }
}