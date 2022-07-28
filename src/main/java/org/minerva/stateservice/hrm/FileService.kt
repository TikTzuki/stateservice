package org.minerva.stateservice.hrm

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import okhttp3.MultipartBody
import org.minerva.stateservice.hrm.models.FileUpload
import retrofit2.Call
import retrofit2.http.*
import java.util.*

interface FileService {
    @Multipart
    @POST("/api/v1/files/")
    fun upload(@Part file: MultipartBody.Part): Call<FileUpload?>

    @GET("/api/v1/files/download")
    fun download(@Query("uuid") uuid: List<UUID>): Call<JsonArray?>
}