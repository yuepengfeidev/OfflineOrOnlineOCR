package com.example.offlineoronlineocr.retrofit;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * NetRequestInterface
 *
 * @author yuepengfei
 * @date 2019/5/26
 * @description
 */
public interface NetRequestInterface {
    /**
     * 通过图片url 获取图片中的手写内容
     *
     * @param accessToken 通过API Key和Secret Key获取的access_token
     * @param image       图片base64编码后的字符串
     * @return 手写识别的内容
     */
    @POST("rest/2.0/ocr/v1/handwriting")
    @FormUrlEncoded
    public Observable<RecognitionBean> getRecognitionResult
    (@Field("access_token") String accessToken, @Field("image") String image);
}
