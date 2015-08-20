package org.jetbrains.ktor.http

import org.jetbrains.ktor.application.*

val ApplicationRequest.uri: String get() = requestLine.uri
val ApplicationRequest.httpMethod: HttpMethod get() = requestLine.method
val ApplicationRequest.httpVersion: String get() = requestLine.version
fun ApplicationRequest.header(name: String): String? = headers[name]
fun ApplicationRequest.parameter(name: String): String? = parameters[name]?.singleOrNull()

fun ApplicationResponse.status(code: HttpStatusCode) = status(code.value)
fun ApplicationResponse.contentType(value: ContentType) = contentType(value.toString())
fun ApplicationResponse.contentType(value: String) = header("Content-Type", value)
fun ApplicationResponse.header(name: String, value: Int): ApplicationResponse = header(name, value.toString())

fun ApplicationResponse.sendRedirect(url: String, permanent: Boolean = false): ApplicationRequestStatus {
    status(if (permanent) HttpStatusCode.MovedPermanently else HttpStatusCode.Found)
    header("Location", url)
    return stream { }
}

fun ApplicationRequestContext.respondText(text: String) = with(response) {
    status(HttpStatusCode.OK)
    contentType(ContentType.Text.Plain)
    sendText(text)
}

fun ApplicationRequestContext.respondRedirect(url: String, permanent: Boolean = false) = with(response) { sendRedirect(url, permanent) }
fun ApplicationRequestContext.respondError(code: Int, message: String) = with(response) {
    status(code)
    sendText(message)
}

fun ApplicationRequestContext.respondAuthenticationRequest(realm: String) = with(response) {
    status(HttpStatusCode.Unauthorized)
    header("WWW-Authenticate", "Basic realm=\"$realm\"")
    sendText("Not authorized")
}
