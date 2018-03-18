package com.suridosa.callpopup;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.auth.helper.Base64;
import com.kakao.kakaolink.AppActionBuilder;
import com.kakao.kakaolink.KakaoLink;
import com.kakao.kakaolink.KakaoTalkLinkMessageBuilder;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.KakaoParameterException;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.Utility;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * Created by Administrator on 2018-02-06.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    //카카오 로그인
    private SessionCallback sessionCallback;
    private boolean isLogin = false;

    Button btnMap;
    Button btnRecvList;
    Button btnSendList;

    private String PhoneNum;
    private BackPressCloseHandler backPressCloseHandler;

    private LinearLayout mainLayout;
    private LinearLayout loginLayout;

    private Menu logoutMenu;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //////////////////////////////////////////
        initLimitCount();

        mainLayout = (LinearLayout)findViewById(R.id.mainLayout);
        loginLayout = (LinearLayout)findViewById(R.id.loginLayout);

        //내 폰번호 가져오기
        TelephonyManager telManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        PhoneNum = telManager.getLine1Number();
        if(PhoneNum.startsWith("+82")){
            PhoneNum = PhoneNum.replace("+82", "0");
        }

        Log.e(TAG, "PhoneNum :: "+PhoneNum);
        //키 해시 값
        Log.e(TAG, "KeyHash :: "+getKeyHash(getApplicationContext()));

        //DAUM 지도보기
        btnMap = (Button) findViewById(R.id.btnMap);
        btnMap.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent mapIntent = new Intent(MainActivity.this, MapActivity.class);
                        mapIntent.putExtra("MYNUMBER",PhoneNum);
                        startActivity(mapIntent);
                    }
                }
        );

        //받은위치
        btnRecvList = (Button)findViewById(R.id.btnRecvList);
        btnRecvList.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent listIntent = new Intent(MainActivity.this, ReceiveListActivity.class);
                        startActivity(listIntent);
                    }
                }
        );

        //보낸위치
        btnSendList = (Button)findViewById(R.id.btnSendList);
        btnSendList.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent listIntent = new Intent(MainActivity.this, SendListActivity.class);
                        startActivity(listIntent);
                    }
                }
        );

        /////////////////////////////////////////////////
        try{
            PackageInfo info = getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_SIGNATURES);
            for(Signature signature : info.signatures){
                MessageDigest messageDigest = MessageDigest.getInstance("SHA");
                messageDigest.update(signature.toByteArray());
                Log.d("aaaa", Base64.encodeBase64URLSafeString(messageDigest.digest()));
            }
        } catch (Exception e){
            Log.d("error", "PackageInfo error is " + e.toString());
        }

        sessionCallback = new SessionCallback();
        Session.getCurrentSession().addCallback(sessionCallback);
        Session.getCurrentSession().checkAndImplicitOpen();

        //뒤로가기 2번 누르면 종료
        backPressCloseHandler = new BackPressCloseHandler(this);
    }



    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)){
            return ;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void request(){
        UserManagement.requestMe(new MeResponseCallback() {
            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                isLogin = false;
                redirectLoginActivity();
                Log.e(TAG, "Session Closed Error is " + errorResult.toString());
            }

            @Override
            public void onNotSignedUp() {
                Log.e(TAG, "onNotSignedUp  ");
                isLogin = false;
                redirectLoginActivity();

            }

            @Override
            public void onSuccess(UserProfile result) {
                isLogin = true;
                redirectLoginActivity();

                Log.e(TAG,""+result.getNickname());
                Log.e(TAG,""+result.getProfileImagePath());
                Log.e(TAG,""+result.getId());

                Toast.makeText(MainActivity.this, "사용자 이름은 " + result.getNickname(), Toast.LENGTH_SHORT).show();

                //사용자 정보 등록
                // URL 설정.
                StringBuffer url = new StringBuffer();
                url.append(Constants.SERVER_URL+"Callpopup.do");
                url.append("?cmd=regUserInfo");
                url.append("&myNumber="+PhoneNum);
                url.append("&myNickname="+result.getNickname());
                url.append("&myImg="+result.getProfileImagePath());
                url.append("&myId="+result.getId());

                Log.e(TAG, "URL :: "+ url.toString());

                // AsyncTask를 통해 HttpURLConnection 수행.
                MainActivity.NetworkTask networkTask = new MainActivity.NetworkTask(url.toString(), null);
                networkTask.execute();

            }
        });
    }

    //카카오  로그아웃
    private void onClickLogout() {
        UserManagement.requestLogout(new LogoutResponseCallback() {
            @Override
            public void onCompleteLogout() {
                isLogin = false;
                redirectLoginActivity();
            }
        });
    }

    //화면 메뉴버튼 / 로그인버튼 세팅
    private void redirectLoginActivity() {

        //상단 로그아웃 메뉴
        MenuItem item = logoutMenu.getItem(0);

        if( isLogin ) {
            item.setTitle("Logout");
            mainLayout.setVisibility(View.VISIBLE);
            loginLayout.setVisibility(View.GONE);
        } else {
            item.setTitle("");
            mainLayout.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);
        }

    }

    private class SessionCallback implements ISessionCallback{

        @Override
        public void onSessionOpened() {
            request();
        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            isLogin = false;
            if(exception != null) {
                Log.d("error", "Session Fail Error is " + exception.getMessage().toString());
            }
                redirectLoginActivity();

        }
    }

    //Key Hash 출력
    public static String getKeyHash(final Context context) {
        PackageInfo packageInfo = Utility.getPackageInfo(context, PackageManager.GET_SIGNATURES);
        if (packageInfo == null)
            return null;

        for (Signature signature : packageInfo.signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                return android.util.Base64.encodeToString(md.digest(), android.util.Base64.NO_WRAP);
            } catch (NoSuchAlgorithmException e) {
                Log.w("MainActivity", "Unable to get MessageDigest. signature=" + signature, e);
            }
        }
        return null;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        logoutMenu = menu;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Log.e(TAG,"action_setting click!!!");
            onClickLogout();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //사용제한 세팅
    private void initLimitCount() {
        SharedPreferences pref = getSharedPreferences("CallPop", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("COUNT", 3);        //사용가능횟수 기본 3회
        editor.putString("ISLOCK", "N"); //사용제한 유무 - 친구추천(3인?)시 해제
        editor.commit();
    }


    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public class NetworkTask extends AsyncTask<Void, Void, String> {

        private String url;
        private ContentValues values;

        public NetworkTask(String url, ContentValues values) {

            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... params) {

            String result; // 요청 결과를 저장할 변수.
            RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
            result = requestHttpURLConnection.request(url, values); // 해당 URL로 부터 결과물을 얻어온다.

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            //doInBackground()로 부터 리턴된 값이 onPostExecute()의 매개변수로 넘어오므로 s를 출력한다.
            Log.e(TAG, "result :: " + s);

            Gson gson = new Gson();
            if( s != null ) {
                ArrayList<CommDomain> list = gson.fromJson(s, new TypeToken<ArrayList<CommDomain>>() {
                }.getType());
                for (CommDomain domain : list) {
                    Log.e(TAG, domain.toString());
                }
            }

//            refreshList();

        }
    }

}