package it.wa2.paymentservice

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.core.convert.converter.Converter
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import java.util.stream.Collectors

class KeycloakRealmRoleConverter : Converter<Jwt?, MutableCollection<GrantedAuthority?>?> {
    override fun convert(jwt: Jwt): MutableCollection<GrantedAuthority?>? {
        val realmAccess = jwt.getClaimAsMap("realm_access")
        if (realmAccess == null || realmAccess.isEmpty()) {
            return mutableListOf()
        }
        val roles = realmAccess["roles"] as MutableList<String?>
        return roles.stream()
            .map { role: String? -> SimpleGrantedAuthority("ROLE_" + role) }
// Spring derive roles from Authorities with "ROLE_" prefix
            .collect(Collectors.toList())
    }
}
