//package com.mobileandroid.appsnews.models
//
//data class WitResponse(
//    val text: String? = null
//)

package com.mobileandroid.appsnews.models

data class WitResponse(
    val text: String? = null,
    val intents: List<Intent>? = null,
    val entities: Map<String, List<Entity>>? = null,
    val traits: Map<String, List<Trait>>? = null
)

data class Intent(
    val id: String? = null,
    val name: String? = null,
    val confidence: Double? = null
)

data class Entity(
    val id: String? = null,
    val name: String? = null,
    val role: String? = null,
    val start: Int? = null,
    val end: Int? = null,
    val body: String? = null,
    val confidence: Double? = null,
    val value: String? = null,
    val type: String? = null
)

data class Trait(
    val id: String? = null,
    val value: String? = null,
    val confidence: Double? = null
)