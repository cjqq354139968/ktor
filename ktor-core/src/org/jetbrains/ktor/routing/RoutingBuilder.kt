package org.jetbrains.ktor.routing

import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.http.*

public fun createRoutingEntry(routingEntry: RoutingEntry, path: String): RoutingEntry {
    val parts = RoutingPath.parse(path).parts
    var current: RoutingEntry = routingEntry;
    for (part in parts) {
        val selector = when (part.kind) {
            RoutingPathPartKind.TailCard -> UriPartTailcardRoutingSelector(part.value)
            RoutingPathPartKind.Parameter -> when {
                part.optional -> UriPartOptionalParameterRoutingSelector(part.value)
                else -> UriPartParameterRoutingSelector(part.value)
            }
            RoutingPathPartKind.Constant ->
                when {
                    part.optional -> UriPartWildcardRoutingSelector()
                    else -> UriPartConstantRoutingSelector(part.value)
                }
        }
        // there may already be entry with same selector, so join them
        current = current.select(selector)
    }
    return current
}

fun RoutingEntry.route(path: String, build: RoutingEntry.() -> Unit) = createRoutingEntry(this, path).build()

fun RoutingEntry.route(method: HttpMethod, path: String, build: RoutingEntry.() -> Unit) {
    val selector = HttpMethodRoutingSelector(method)
    select(selector).route(path, build)
}

fun RoutingEntry.method(method: HttpMethod, body: RoutingEntry.() -> Unit) {
    val selector = HttpMethodRoutingSelector(method)
    select(selector).body()
}

fun RoutingEntry.param(name: String, value: String, build: RoutingEntry.() -> Unit) {
    val selector = ConstantParameterRoutingSelector(name, value)
    select(selector).build()
}

fun RoutingEntry.param(name: String, build: RoutingEntry.() -> Unit) {
    val selector = ParameterRoutingSelector(name)
    select(selector).build()
}


fun RoutingEntry.header(name: String, value: String, build: RoutingEntry.() -> Unit) {
    val selector = HttpHeaderRoutingSelector(name, value)
    select(selector).build()
}

fun RoutingEntry.handle(handle: RoutingApplicationRequestContext.() -> ApplicationRequestStatus) {
    addInterceptor(true, handle)
}

fun RoutingEntry.addInterceptor(leafOnly: Boolean, handle: RoutingApplicationRequestContext.() -> ApplicationRequestStatus) {
    intercept(leafOnly) { context, next ->
        val result = context.handle()
        if (result == ApplicationRequestStatus.Unhandled)
            next(context)
        else
            result
    }
}
