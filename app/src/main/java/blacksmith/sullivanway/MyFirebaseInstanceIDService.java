package blacksmith.sullivanway;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseInsIDService";

    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + token);

        // 각자 핸드폰 토큰값을 핸드폰으로 전송한다
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {

        OkHttpClient client = new OkHttpClient();

        Log.d(TAG, "Send Ready");
        RequestBody body = new FormBody.Builder()
                .add("Token", token)
                .build();

        Request request = new Request.Builder()
                // TODO
                // 당일 IP주소 받아서 저장하기
                .url("http://113.198.79.101/FCM/RegisterKey")    // 페이지 경로를 입력
                .post(body)
                .build();

        try {
            client.newCall(request).execute();
            Log.d(TAG, "Send Finished");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
