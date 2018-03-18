package com.suridosa.callpopup;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Created by Administrator on 2018-02-13.
 */

public class SendListActivity extends AppCompatActivity {

    public static final String TAG = "SendListActivity";

    CustomAdapter adapter;
    ArrayList<CommDomain> list;
    ListView listview;
    String PhoneNum;

    @SuppressLint("MissingPermission")
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_send_list);

        //내 폰번호 가져오기
        TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        PhoneNum = telManager.getLine1Number();
        if(PhoneNum.startsWith("+82")){
            PhoneNum = PhoneNum.replace("+82", "0");
        }
        Log.e(TAG,"MY PHONE NUMBER :: "+PhoneNum);

        setTitle("보낸 위치");

        listview = (ListView)findViewById(R.id.listview);

        //데이터를 저장하게 되는 리스트
        list = new ArrayList<CommDomain>();


        // URL 설정.
        StringBuffer url = new StringBuffer();
        url.append(Constants.SERVER_URL+"Callpopup.do");
        url.append("?cmd=getSendList");
        url.append("&myNumber="+PhoneNum);

        // AsyncTask를 통해 HttpURLConnection 수행.
        NetworkTask networkTask = new NetworkTask(url.toString(), null);
        networkTask.execute();

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
            Log.e("ReceiveListActivity","result :: "+s);

            Gson gson = new Gson();
            //data = gson.fromJson(result, CommDomain.class);
            list = gson.fromJson(s, new TypeToken<ArrayList<CommDomain>>(){}.getType());

            Log.e("onPostExecute","list size :" +list.size());
            for(CommDomain domain : list) {
                Log.e("onPostExecute", domain.getId()+" :: "+domain.getName());
            }

            refreshList();

        }

        //조회 결과 새로고침
        private void refreshList() {

            //리스트뷰와 리스트를 연결하기 위해 사용되는 어댑터
            adapter = new CustomAdapter(getApplicationContext(), R.layout.list, list);

            //리스트뷰의 어댑터를 지정해준다.
            listview.setAdapter(adapter);

            adapter.notifyDataSetChanged();
        }
    }
}
