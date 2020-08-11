package com.ilunos.agent.docker.security

import io.micronaut.context.annotation.Replaces
import io.micronaut.security.token.Claims
import io.micronaut.security.token.DefaultRolesFinder
import io.micronaut.security.token.RolesFinder
import io.micronaut.security.token.config.TokenConfiguration
import net.minidev.json.JSONObject
import javax.inject.Singleton

@Singleton
@Replaces(DefaultRolesFinder::class)
class KeycloakRoleFinder(tokenConfiguration: TokenConfiguration) : RolesFinder {

    private val defaultRolesFinder = DefaultRolesFinder(tokenConfiguration)

    override fun findInClaims(claims: Claims): List<String> {
        val roles = defaultRolesFinder.findInClaims(claims)

        roles.addAll(findInRealm(claims))
        roles.addAll(findInResources(claims))

        return roles
    }

    private fun findInRealm(claims: Claims): List<String> {
        val realmAccess = claims["realm_access"] as JSONObject? ?: return emptyList()
        return (realmAccess["roles"] as Iterable<*>?)?.map { "REALM_${it.toString().toUpperCase()}" }?.toList() ?: emptyList()
    }

    private fun findInResources(claims: Claims): List<String> {
        val list = mutableListOf<String>()

        val resources = claims["resource_access"] as JSONObject? ?: return emptyList()
        resources.forEach { resource ->
            val resourceRoles = (resource.value as JSONObject?) ?: return@forEach
            list.addAll((resourceRoles["roles"] as Iterable<*>?)?.map { "${formatResource(resource.key)}_${formatResource(it.toString())}" }?.toList() ?: emptyList())
        }

        return list
    }

    private fun formatResource(name: String): String = name.replace('-', '_').toUpperCase()
}