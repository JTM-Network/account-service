package com.jtm.account

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@EnableDiscoveryClient
@SpringBootApplication
open class AccountService

fun main(args: Array<String>) {
    SpringApplication.run(AccountService::class.java, *args)
}