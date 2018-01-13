package tk.germanbot.service

import kotlin.reflect.KClass

class EntityNotFoundException(val type: KClass<out Any>, val id: String): RuntimeException("${type.simpleName} not found: ${id}")

class EntityValidationException(val type: KClass<out Any>, val msg: String): RuntimeException("${type.simpleName} not valid: ${msg}")
