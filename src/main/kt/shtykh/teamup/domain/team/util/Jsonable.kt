package shtykh.teamup.domain.team.util

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature


import java.io.IOException
import java.io.InputStream


/**
 * Created by shtykh on 02/10/15.
 */
interface Jsonable {

    fun toJson(): String {
        try {
            return mapper.writeValueAsString(this)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }

    companion object {
        val mapper = initMapper()

        fun initMapper(): ObjectMapper {
            val mapper = ObjectMapper()
            mapper.enable(SerializationFeature.INDENT_OUTPUT)
            mapper.serializationConfig.defaultVisibilityChecker
                    .withFieldVisibility(JsonAutoDetect.Visibility.NONE)
                    .withGetterVisibility(JsonAutoDetect.Visibility.PUBLIC_ONLY)
                    .withSetterVisibility(JsonAutoDetect.Visibility.PUBLIC_ONLY)
                    .withCreatorVisibility(JsonAutoDetect.Visibility.PUBLIC_ONLY)
            return mapper
        }

        inline fun <reified T: Jsonable> jacksonTypeRef(): TypeReference<T> = object: TypeReference<T>() {}

        inline fun <reified T : Jsonable> fromJson(json: String): T {
            try {
                val stream : InputStream = json.byteInputStream()
                return mapper.readValue<T>(stream, jacksonTypeRef<T>())
            } catch (e: IOException) {
                throw RuntimeException(e.javaClass.toString() + ": " + e.message + " in:\n" + json)
            }
        }
    }
}
