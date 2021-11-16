package net.oskarstrom.lucuma.error

@JvmInline
value class LucumaParseException(string: String): Exception(string)