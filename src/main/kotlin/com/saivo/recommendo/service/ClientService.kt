package com.saivo.recommendo.service

import com.saivo.recommendo.model.infrastructure.Client
import com.saivo.recommendo.repository.ClientRepository
import com.saivo.recommendo.util.createUUID
import com.saivo.recommendo.util.exception.ClientNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class ClientService {

    @Autowired
    val clientRepository: ClientRepository? = null

    fun getClientById(clientId: String): Client {
        return clientRepository!!.findById(clientId).orElseThrow {
            ClientNotFoundException()
        }
    }

    fun isClient(clientId: String): Boolean {
        try {
            return getClientById(clientId).clientId!!.isNotEmpty()
        } catch (e: ClientNotFoundException) {
            println(e)
        }
        return false
    }

    fun getClients(): MutableIterable<Client> {
        return clientRepository!!.findAll()
    }

    fun saveClient(client: Client? = null, secret: String = "", action: String = ""): String {
        when (action) {
            "register" -> {
                return registerClient(secret)
            }
            "update" -> {
                client?.let {
                    return updateClient(it)
                }
            }
        }
        return ""
    }

    fun registerClient(secret: String): String {
        return clientRepository!!.save(Client().apply {
            scope = "register"
            clientId = UUID.randomUUID().toString().takeIf { !isClient(it) }
            clientSecret = secret
            resourceIds = System.getenv("RESOURCE_ID")
            accessTokenValidity = 3600
            refreshTokenValidity = 3600
            authorizedGrantTypes = "client_credentials"
        }).clientId!!
    }

    fun updateClient(client: Client): String {
        return clientRepository?.save(getClientById(client.clientId!!).apply {
            scope += ",${client.scope}"
            resourceIds += ",${client.resourceIds}"
            accessTokenValidity = client.accessTokenValidity
            refreshTokenValidity = client.refreshTokenValidity
            authorizedGrantTypes += ",${client.authorizedGrantTypes}"
        })?.clientId!!
    }
}