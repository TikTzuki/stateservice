package org.minerva.stateservice.hrm;


import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface FileRepos {
    @Multipart
    @POST("/api/v1/files/")
    Call<ResponseBody> upload(@Part MultipartBody.Part file, @Header("server-auth") String serverAuth, @Header("Authorization") String auth);
}
