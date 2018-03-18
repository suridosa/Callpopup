package com.suridosa.callpopup;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapReverseGeoCoder;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;

/**
 * Created by Administrator on 2018-02-02.
 */

public class ServiceReceiver extends BroadcastReceiver /*implements MapReverseGeoCoder.ReverseGeoCodingResultListener */ {

    public static final String TAG = "PHONE STATE";
    private static String mLastState;
    private Context context;
    private String PhoneNum;
    private String CallNum;
    private String diaplayName;
    private String contactId;
    private String callAddress;
    private String state;

    private Intent mIntent;
    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(final Context context, Intent intent) {

        Log.d(TAG,"onReceive()");
        this.context = context;
        mIntent = intent;
        //부팅시 서비스 시작하기 위함.
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.e(TAG,"ACTION_BOOT_COMPLETED!!!");
        }

        //내 폰번호 가져오기
        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        PhoneNum = telManager.getLine1Number();
        if(PhoneNum.startsWith("+82")){
            PhoneNum = PhoneNum.replace("+82", "0");
        }
        Log.e(TAG,"MY PHONE NUMBER :: "+PhoneNum);


        final LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try{
            // GPS 제공자의 정보가 바뀌면 콜백하도록 리스너 등록하기~!!!
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, // 등록할 위치제공자
                    100, // 통지사이의 최소 시간간격 (miliSecond)
                    1, // 통지사이의 최소 변경거리 (m)
                    mLocationListener);

            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자
                    100, // 통지사이의 최소 시간간격 (miliSecond)
                    1, // 통지사이의 최소 변경거리 (m)
                    mLocationListener);

        }catch(SecurityException ex){
        }

        /** * http://mmarvick.github.io/blog/blog/lollipop-multiple-broadcastreceiver-call-state/
         * * 2번 호출되는 문제 해결 */
        state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        if (state.equals(mLastState)) {
            return;
        } else {
            mLastState = state;
        }

        if ( TelephonyManager.EXTRA_STATE_RINGING.equals(state) || TelephonyManager.EXTRA_STATE_IDLE.equals(state) || TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) {
            CallNum = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            final String phone_number = PhoneNumberUtils.formatNumber(CallNum);

            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone_number));
            String[] projection = new String[]{ContactsContract.PhoneLookup.CONTACT_ID,ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.Contacts._ID} ;

            Cursor cursor = context.getContentResolver().query(uri, projection , null, null, null);
            if(cursor != null){
                if(cursor.moveToFirst()){
                    diaplayName = cursor.getString(1);
                    contactId = cursor.getString(2);
                } cursor.close();
            }

            diaplayName = StringUtils.nvl(diaplayName, context.getString(R.string.unknown));

//            Intent it = new Intent(context, CallPopup.class);
//            it.putExtra("IN_NUMBER", incomingNumber);
//            it.putExtra("IN_NAME", diaplayName);
//            it.putExtra("STATE", state);
//            it.putExtra("CONTACTID", contactId);
//            context.startActivity(it);

//            Intent i = new Intent(context, CallingService.class);
//            i.putExtra("IN_NUMBER", incomingNumber);
//            i.putExtra("IN_NAME", diaplayName);
//            i.putExtra("STATE", state);
//            i.putExtra("CONTACTID", contactId);
//            if( callAddress != null ) {
//                i.putExtra("IN_ADDRESS", callAddress);
//            }
//           // if( contactId != null && !"".equals(contactId)) {
//            Log.e(TAG,"contactId ====> "+contactId);
//                if( contactId == null || "".equals(contactId) ) {
//                    contactId = "-1";
//                }
//                Long _id = Long.parseLong(contactId);
//                Bitmap sendBitmap = getPhoto(_id);
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                sendBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//                byte[] byteArray = stream.toByteArray();
//            Log.e(TAG,"byteArray ====> "+byteArray.toString());
//                i.putExtra("photo", byteArray);
//            //}
//
//            context.startService(i);

        }

      }


      private void callSerivce() {

          Intent i = new Intent(context, CallingService.class);
          i.putExtra("IN_NUMBER", CallNum);
          i.putExtra("IN_NAME", diaplayName);
          i.putExtra("STATE", state);
          i.putExtra("CONTACTID", contactId);
          if( callAddress != null ) {
              i.putExtra("IN_ADDRESS", callAddress);
          }
          // if( contactId != null && !"".equals(contactId)) {
          Log.e(TAG,"contactId ====> "+contactId);
          if( contactId == null || "".equals(contactId) ) {
              contactId = "-1";
          }
          Long _id = Long.parseLong(contactId);
          Bitmap sendBitmap = getPhoto(_id);
          ByteArrayOutputStream stream = new ByteArrayOutputStream();
          sendBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
          byte[] byteArray = stream.toByteArray();
          Log.e(TAG,"byteArray ====> "+byteArray.toString());
          i.putExtra("photo", byteArray);
          //}

          context.startService(i);
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
            Log.e("ServiceReceiveer", "result :: " + s);

            Gson gson = new Gson();

            if( s != null ) {
                ArrayList<CommDomain> list = gson.fromJson(s, new TypeToken<ArrayList<CommDomain>>() {
                }.getType());
                for (CommDomain domain : list) {
                    Log.e("onPostExecute", domain.toString());
                    callAddress = domain.getCallAddress();
                }

                callSerivce();
            }

//            refreshList();

        }
    }

    public Bitmap getPhoto(Long contactId) {
        Uri contactPhotoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Log.e(TAG,"contactPhotoUri ====> "+contactPhotoUri.getPath());
        if( contactPhotoUri != null ) {
            InputStream photoDataStream = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), contactPhotoUri);
            if( photoDataStream != null ) {
                Log.e(TAG,"contactPhotoUri ==== 11111111111 ");
                return BitmapFactory.decodeStream(photoDataStream);
            }
        } else {
            Log.e(TAG,"contactPhotoUri ==== 22222222222222 ");
            Bitmap defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.no_photo);
            return defaultPhoto;
        }

        Log.e(TAG,"contactPhotoUri ==== 33333333333333 ");
        Bitmap defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.no_photo);
        return defaultPhoto;
    }


    /////////////////////////////////////////////////////////////////////////////////////
    private final LocationListener mLocationListener = new LocationListener() {

        public void onLocationChanged(Location location) {

                //if( !locationReady ) {
                double longitude = location.getLongitude(); //경도
                double latitude = location.getLatitude();   //위도
                double altitude = location.getAltitude();   //고도
                float accuracy = location.getAccuracy();    //정확도
                String provider = location.getProvider();   //위치제공자
                //Gps 위치제공자에 의한 위치변화. 오차범위가 좁다.
                //Network 위치제공자에 의한 위치변화
                //Network 위치는 Gps에 비해 정확도가 많이 떨어진다.

                if( PhoneNum != null ) {
                    // URL 설정.
                    StringBuffer url = new StringBuffer();
                    url.append(Constants.SERVER_URL + "Callpopup.do");
                    url.append("?cmd=getLocationInfo");
                    url.append("&myNumber=" + PhoneNum);
                    url.append("&myLat=" + String.valueOf(latitude));
                    url.append("&myLng=" + String.valueOf(longitude));
                    url.append("&callNumber=" + CallNum);

                    Log.e(TAG, "URL :: " + url.toString());

                    // AsyncTask를 통해 HttpURLConnection 수행.
                    ServiceReceiver.NetworkTask networkTask = new ServiceReceiver.NetworkTask(url.toString(), null);
                    networkTask.execute();
                }

        }
        public void onProviderDisabled(String provider) {
            // Disabled시
            Log.e("LocationListener", "onProviderDisabled, provider:" + provider);
        }

        public void onProviderEnabled(String provider) {
            // Enabled시
            Log.e("LocationListener", "onProviderEnabled, provider:" + provider);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            // 변경시
            Log.e("LocationListener", "onStatusChanged, provider:" + provider + ", status:" + status + " ,Bundle:" + extras);
        }
    };


//    // reverseGeoCodingResultListener
//    @Override
//    public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String addressString) {
//        // 주소를 찾은 경우.
//        Log.e(TAG,"주소 : "+addressString);
//        findAddress = addressString;
//    }
//
//    @Override
//    public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {
//        // 호출에 실패한 경우.
//        Log.d(TAG,"호출에 실패");
//    }

}