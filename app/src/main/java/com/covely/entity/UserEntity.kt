package com.covely.entity

data class UserEntity(
    val user_id : String,
    val timezone : String,
    val app_package : String,
    val idfa: String? = null,
    val push_token : String? = null,
    val ad_campaign : String? = null,
    val ad_id : String? = null
)


data class InstallEntity(
    val allow : Boolean,
    val entity : User,
    var goto : String? = null
)


data class User(
    val user_id : String,
    val timezone : String,
    val created_at : String,
    var idfa : String? = null,
    var push_token : String? = null,
    var updated_at : String? = null
)
