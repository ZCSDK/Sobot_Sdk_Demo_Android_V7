#sobot SDK
-keep class com.sobot.chat.** {*;}
-dontwarn com.sobot.chat.**

# OkHttp3
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

## ----------------------------------
##      Glide相关
## ----------------------------------
-keep class com.bumptech.glide.Glide { *; }
-keep class com.bumptech.glide.request.RequestOptions {*;}
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-dontwarn com.bumptech.glide.**

## ----------------------------------
##      Picasso相关
## ----------------------------------
-keep class com.squareup.picasso.Picasso { *; }
-dontwarn com.squareup.okhttp.**
-dontwarn com.squareup.picasso.**

## ----------------------------------
##      Fresco相关
## ----------------------------------
-keep class com.facebook.fresco.** { *; }
-keep class com.facebook.imagepipeline.** { *; }

# 保留所有 TypeToken 子类不被混淆，防止泛型信息丢失
-keep class com.sobot.chat.gson.reflect.TypeToken {
    *;
}
# 保留泛型签名信息
-keepattributes Signature
# 保留 Gson 序列化类的字段名和类型
-keepclassmembers class * {
    @com.sobot.chat.gson.annotations.SerializedName <fields>;
}


