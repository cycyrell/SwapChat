package xyz.teamcatalyst.breedr;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.support.multidex.MultiDex;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.pixplicity.easyprefs.library.Prefs;
import com.sendbird.android.SendBird;

import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class SwapChat extends Application {

    private static FirebaseService apiService;

    public static FirebaseService getApiService() {
        return apiService;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://puppy-love-74c8d.firebaseio.com")
                .addConverterFactory(MoshiConverterFactory.create())
                .build();

        apiService = retrofit.create(FirebaseService.class);

        initChatSDK();
        sFaceServiceClient = new FaceServiceRestClient("https://westcentralus.api.cognitive.microsoft.com/face/v1.0", "d7987c551e3747c1bcaf8476f622b2ff");
    }

    public static FaceServiceClient getFaceServiceClient() {
        return sFaceServiceClient;
    }

    private static FaceServiceClient sFaceServiceClient;

    private void initChatSDK() {
        SendBird.init("5BC3A192-AED6-4C86-BCE2-A4C33737D50F", this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
            MultiDex.install(this);
        } catch (RuntimeException exception) {
            // Multidex support doesn't play well with Robolectric yet
        }
    }
}