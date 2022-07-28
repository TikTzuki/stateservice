package org.minerva.stateservice.hrm.configs

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.google.gson.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.minerva.stateservice.hrm.FileService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Configuration
open class Beans(
    @Autowired
    val serviceConfigs: ServiceConfigs,
) {
//    @Bean
//    open fun objectMapper(): ObjectMapper {
//        return ObjectMapper()
//            .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
//            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
//            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
//            .findAndRegisterModules()
//    }

    open fun dateDeserializer(): JsonDeserializer<LocalDateTime> {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
        return JsonDeserializer<LocalDateTime> { json: JsonElement?, type: Type?, jsonDeserializationContext: JsonDeserializationContext? ->
            if (json != null) {
                val jsonString = json.getAsString()
                return@JsonDeserializer LocalDateTime.parse(jsonString, formatter)
            }
            null
        }
    }

    @Bean
    open fun fileService(): FileService {
        dateDeserializer()
        val gson = GsonBuilder()
            .setDateFormat("dd/MM/yyyy HH:mm:ss")
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(LocalDateTime::class.java, dateDeserializer())
            .create()

        val clientBuilder = OkHttpClient.Builder();
        clientBuilder.addInterceptor(Interceptor { chain: Interceptor.Chain ->
            val request = chain.request().newBuilder()
                .addHeader("server-auth", serviceConfigs.serverAuth)
                .addHeader("Authorization", serviceConfigs.auth)
                .build()
            chain.proceed(request)
        })
        val retrofit = Retrofit.Builder()
            .baseUrl(serviceConfigs.host)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(clientBuilder.build())
            .build()
        return retrofit.create();
    }

}