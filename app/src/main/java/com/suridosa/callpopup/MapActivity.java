package com.suridosa.callpopup;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kakao.kakaolink.KakaoLink;
import com.kakao.kakaolink.KakaoTalkLinkMessageBuilder;
import com.kakao.util.KakaoParameterException;

import net.daum.android.map.coord.MapCoord;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapReverseGeoCoder;
import net.daum.mf.map.api.MapView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018-02-07.
 */

public class MapActivity extends AppCompatActivity implements MapView.MapViewEventListener, MapView.POIItemEventListener,MapReverseGeoCoder.ReverseGeoCodingResultListener {

    final static String TAG = "MapActivity";

    private MapPOIItem marker;
    private MapPoint oldMapPoint;
    private String   findAddress;

    //현재 선택된 위치
    private double lat,lng;

    //지도뷰
    private MapView mapView;

    //카카오링크 위치 보내기 위한 선언
    private KakaoLink kakaoLink;
    private KakaoTalkLinkMessageBuilder kakaoTalkLinkMessageBuilder;
    private Button btnSendLoc;
    private Button btnShare;

    boolean locationReady = false;

    private String mode;

    private String isLock = "";
    private int limitCount = 0;

    String PhoneNum;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);

        //내 폰번호 가져오기
        TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        PhoneNum = telManager.getLine1Number();
        if(PhoneNum.startsWith("+82")){
            PhoneNum = PhoneNum.replace("+82", "0");
        }
        Log.e(TAG,"MY PHONE NUMBER :: "+PhoneNum);

        //////////////////////////////////////////////////////////////////////
        isLock = getIsLock();
        limitCount = getLimitCount();

        //////////////////////////////////////////////////////////////////////
        Intent i = getIntent();
        String tmpLat = i.getStringExtra("Lat");
        String tmpLng = i.getStringExtra("Lng");

                Log.e(TAG, "tmpLat :: "+tmpLat);
        Log.e(TAG, "tmpLng :: "+tmpLng);

        if( tmpLat != null && tmpLng != null ) {
            mode = "VIEW";
            lat = Float.parseFloat(tmpLat);
            lng = Float.parseFloat(tmpLng);
            setTitle("받은 위치");
        } else {
            mode = "MYLOC";
            setTitle("내 위치 보내기");
        }

        Log.e(TAG,"mode :: "+mode);
        //////////////////////////////////////////////////////////////////////



        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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

        mapView = new MapView(this);
        mapView.setMapViewEventListener(this); // this에 MapView.MapViewEventListener 구현.
        mapView.setPOIItemEventListener(this);

        // 중심점 변경
        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(37.53737528, 127.00557633), true);

        // 줌 레벨 변경
        mapView.setZoomLevel(3, true);
        // 중심점 변경 + 줌 레벨 변경
        //mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(33.41, 126.52), 9, true);
        // 줌 인
        //mapView.zoomIn(true);
        // 줌 아웃
        //mapView.zoomOut(true);

        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.map_view);

        mapViewContainer.addView(mapView);


        //카카오링크 보내기

        try {
            kakaoLink = KakaoLink.getKakaoLink(getApplicationContext());
            kakaoTalkLinkMessageBuilder = kakaoLink.createKakaoTalkLinkMessageBuilder();
        } catch (KakaoParameterException e) {
            Log.e("error",e.getMessage());
        }

        //내 위치 보내기
        btnSendLoc = (Button) findViewById(R.id.btnSendLoc);
        btnSendLoc.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialog();


                        /*
                        String subject = "내 위치 공유";
                        String text = "주소 : "+findAddress;

                        List targetedShareIntents = new ArrayList<>();

                        // 카카오톡
                        Intent kakaoIntent = getShareIntent("com.kakao.talk", subject, text);
                        if(kakaoIntent != null)
                            targetedShareIntents.add(kakaoIntent);

                        // SMS
                        Intent smsntent = getShareIntent("sms", subject, text);
                        if(smsntent != null)
                            targetedShareIntents.add(smsntent);


                        Intent chooser = Intent.createChooser((Intent)targetedShareIntents.remove(0), "친구에게 공유하기");
                        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));
                        startActivity(chooser);
                        */
                    }
                }
        );

        btnShare = (Button)findViewById(R.id.btnShare);
        btnShare.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                         Toast.makeText(getApplicationContext(), "구현 예정입니다", Toast.LENGTH_SHORT).show();
                         releaseLimit();
                         btnSendLoc.setVisibility(View.VISIBLE);
                         btnShare.setVisibility(View.GONE);
                    }
                }
        );


    }

    int selItemIdx = 0;
    private void showDialog() {

        if( "Y".equals(getIsLock()) &&  getLimitCount() == 0 ) {

            showLimitAlert();

        } else {

            final CharSequence[] items = {"카카오링크보내기", "SMS보내기"};
            AlertDialog.Builder ab = new AlertDialog.Builder(MapActivity.this);
            ab.setTitle("위치보내기");
            ab.setSingleChoiceItems(items, 0,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // 각 리스트를 선택했을때
                            Log.e(TAG, "whichButton :: " + whichButton);
                            selItemIdx = whichButton;
                        }
                    }).setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                            if( "Y".equals(getIsLock()) ) minusCount();

                            // OK 버튼 클릭시 , 여기서 선택한 값을 메인 Activity 로 넘기면 된다.
                            switch (selItemIdx) {
                                case 0:
                                    sendKakao();
                                    break;
                                case 1:
                                    showDialog2();
                                    break;
                            }
                        }
                    }).setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Cancel 버튼 클릭시
                            dialog.dismiss();
                        }
                    });
            ab.show();

        }
    }


    private void showDialog2() {

        ArrayList names   = MessageUtils.getContactList(getApplicationContext(),"NAME");
        ArrayList names2   = MessageUtils.getContactList(getApplicationContext(),"NAME2");
        ArrayList numbers = MessageUtils.getContactList(getApplicationContext(),"NUMBER");

        final String[] items = (String[]) names.toArray(new String[names.size()]);
        final String[] items2 = (String[]) names2.toArray(new String[names2.size()]);
        final String[] nums = (String[]) numbers.toArray(new String[numbers.size()]);

        AlertDialog.Builder ab = new AlertDialog.Builder(MapActivity.this);
        ab.setTitle("전화번호 선택");
        ab.setSingleChoiceItems(items, 0,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // 각 리스트를 선택했을때
                        Log.e(TAG,"whichButton :: "+whichButton);
                        selItemIdx = whichButton;
                    }
                }).setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // OK 버튼 클릭시 , 여기서 선택한 값을 메인 Activity 로 넘기면 된다.
                        sendSMS(nums[selItemIdx].toString(), items2[selItemIdx].toString());
                    }
                }).setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Cancel 버튼 클릭시
                        dialog.dismiss();
                    }
                });
        ab.show();

    }


    //SMS보내기
    private void sendSMS(String receiveNumber, String receiveName) {
        Log.e(TAG,"sendSMS");

        String stringText = "내 위치 공유\n";
        stringText += "* 주소 : "+findAddress;
        MessageUtils.sendSMS(MapActivity.this, receiveNumber, stringText);

        // URL 설정.
        StringBuffer url = new StringBuffer();
        url.append(Constants.SERVER_URL + "Callpopup.do");
        url.append("?cmd=regSendInfo");
        url.append("&myNumber=" + PhoneNum);
        url.append("&lat=" + String.valueOf(lat));
        url.append("&lng=" + String.valueOf(lng));
        url.append("&receiveNumber=" + receiveNumber);
        url.append("&receiveName=" + receiveName);
        url.append("&address=" + findAddress);

        Log.e(TAG, "URL :: " + url.toString());

        // AsyncTask를 통해 HttpURLConnection 수행.
        MapActivity.NetworkTask networkTask = new MapActivity.NetworkTask(url.toString(), null);
        networkTask.execute();

    }

    //카카오링크 보내기
    private void sendKakao() {
        Log.e(TAG,"sendKakao");
        try {
            String stringImage = "http://www.suridosa.com/technote6/data/board/myhomeboard/file/1/8278d8a3_b3bbc4b3b8af.jpg";
            String stringText = "내 위치 공유\n";
            stringText += "* 주소 : "+findAddress;
            MessageUtils.sendKakaoLink(MapActivity.this, kakaoLink, kakaoTalkLinkMessageBuilder, stringText, stringImage);

        } catch (KakaoParameterException e) {
            Log.e("error",e.getMessage());
        }
    }

    @Override
    public void onMapViewInitialized(MapView mapView) {
        Log.d(TAG,"onMapViewInitialized : "+lat+","+lng);

//        MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(lat, lng);
//        marker = new MapPOIItem();
//        marker.setItemName(findAddress);
//        marker.setTag(0);
//        marker.setMapPoint(mapPoint);
//        oldMapPoint = mapPoint;
//        marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
//        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
//
//        mapView.addPOIItem(marker);
    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {
        Log.d(TAG,"onMapViewCenterPointMoved");

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {
        Log.d(TAG,"onMapViewZoomLevelChanged :: "+ i);
    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
        Log.d(TAG,"onMapViewSingleTapped :: "+ mapPoint.getMapPointGeoCoord().latitude+" : "+mapPoint.getMapPointGeoCoord().longitude);

        lat = mapPoint.getMapPointGeoCoord().latitude;
        lng = mapPoint.getMapPointGeoCoord().longitude;
        MapReverseGeoCoder reverseGeoCoder = new MapReverseGeoCoder("092836e22dd28d90772e7ee47fb92f58", mapPoint, this, MapActivity.this);
        reverseGeoCoder.startFindingAddress();

        if( oldMapPoint != null ) {
            mapView.removeAllPOIItems();
        }

        marker = new MapPOIItem();
        marker.setItemName(findAddress);
        marker.setTag(0);
        marker.setMapPoint(mapPoint);
        oldMapPoint = mapPoint;
        marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

        mapView.addPOIItem(marker);

        if(!"VIEW".equals(mode)) {
            btnSendLoc.setEnabled(true);
        }

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {
        Log.d(TAG,"onMapViewDoubleTapped :: "+ mapPoint.toString());
    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {
        Log.d(TAG,"onMapViewLongPressed :: "+ mapPoint.toString());
    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {
        Log.d(TAG,"onMapViewDragStarted :: "+ mapPoint.toString());
    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {
        Log.d(TAG,"onMapViewDragEnded :: "+ mapPoint.toString());
    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {
        Log.d(TAG,"onMapViewMoveFinished :: "+ mapPoint.getMapPointGeoCoord());

        if( oldMapPoint != null ) {
            mapView.removeAllPOIItems();
        }

        if( "VIEW".equals(mode) ) {
            mapPoint = MapPoint.mapPointWithGeoCoord(lat, lng);
        }

            MapReverseGeoCoder reverseGeoCoder = new MapReverseGeoCoder("092836e22dd28d90772e7ee47fb92f58", mapPoint, MapActivity.this, MapActivity.this);
            reverseGeoCoder.startFindingAddress();

            Log.d(TAG,"@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@2222"+StringUtils.nvl(findAddress));
            mapPoint = MapPoint.mapPointWithGeoCoord(lat, lng);

            MapPOIItem marker = new MapPOIItem();
            marker.setItemName(StringUtils.nvl(findAddress));
            marker.setTag(0);
            marker.setMapPoint(mapPoint);
            oldMapPoint = mapPoint;
            marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
            marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

            mapView.addPOIItem(marker);

            btnSendLoc.setEnabled(true);

    }



    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
        Log.d(TAG,"onPOIItemSelected :: "+ mapPOIItem.toString());
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {
        Log.d(TAG,"onCalloutBalloonOfPOIItemTouched :: "+ mapPOIItem.toString());
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {
        Log.d(TAG,"onCalloutBalloonOfPOIItemTouched :: "+ mapPOIItem.toString());
    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {
        Log.d(TAG,"onDraggablePOIItemMoved :: "+ mapPOIItem.toString());
    }


    // reverseGeoCodingResultListener
    @Override
    public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String addressString) {
        // 주소를 찾은 경우.
        Log.d(TAG,"주소 : "+addressString);
        findAddress = addressString;
    }

    @Override
    public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {
        // 호출에 실패한 경우.
        Log.d(TAG,"호출에 실패");
    }

    /////////////////////////////////////////////////////////////////////////////////////
    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {

            if( !"VIEW".equals(mode)) {
                //여기서 위치값이 갱신되면 이벤트가 발생한다.
                //값은 Location 형태로 리턴되며 좌표 출력 방법은 다음과 같다.
                Log.d(TAG, "onLocationChanged, location:" + location + " > " + locationReady);

                //if( !locationReady ) {
                double longitude = location.getLongitude(); //경도
                double latitude = location.getLatitude();   //위도

                lat = latitude;
                lng = longitude;

                double altitude = location.getAltitude();   //고도
                float accuracy = location.getAccuracy();    //정확도
                String provider = location.getProvider();   //위치제공자
                //Gps 위치제공자에 의한 위치변화. 오차범위가 좁다.
                //Network 위치제공자에 의한 위치변화
                //Network 위치는 Gps에 비해 정확도가 많이 떨어진다.

                MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude);


                MapReverseGeoCoder reverseGeoCoder = new MapReverseGeoCoder("092836e22dd28d90772e7ee47fb92f58", mapPoint, MapActivity.this, MapActivity.this);
                reverseGeoCoder.startFindingAddress();


                Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                if (oldMapPoint != null) {
                    mapView.removeAllPOIItems();
                }

                MapPOIItem marker = new MapPOIItem();
                marker.setItemName(findAddress);
                marker.setTag(0);
                marker.setMapPoint(mapPoint);
                oldMapPoint = mapPoint;
                marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
                marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

                mapView.addPOIItem(marker);

                mapView.setMapCenterPoint(mapPoint, true);
                Log.d(TAG, "#####################################################");

                //   locationReady = true;
                //}

            }

        }
        public void onProviderDisabled(String provider) {
            // Disabled시
            Log.d("LocationListener", "onProviderDisabled, provider:" + provider);
        }

        public void onProviderEnabled(String provider) {
            // Enabled시
            Log.d("LocationListener", "onProviderEnabled, provider:" + provider);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            // 변경시
            Log.d("LocationListener", "onStatusChanged, provider:" + provider + ", status:" + status + " ,Bundle:" + extras);
        }
    };


    /////////////////////////////////////////////////////////////////////////////
    // 공유하기
    /////////////////////////////////////////////////////////////////////////////
    private Intent getShareIntent(String name, String subject, String text) {
        boolean found = false;

        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");

        // gets the list of intents that can be loaded.
        List<ResolveInfo> resInfos = getPackageManager().queryIntentActivities(intent, 0);

        if(resInfos == null || resInfos.size() == 0)
            return null;

        for (ResolveInfo info : resInfos) {
            if (info.activityInfo.packageName.toLowerCase().contains(name) ||
                    info.activityInfo.name.toLowerCase().contains(name) ) {
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                intent.putExtra(Intent.EXTRA_TEXT, text);
                intent.setPackage(info.activityInfo.packageName);
                found = true;
                break;
            }
        }

        if (found)
            return intent;

        return null;
    }


    //사용제한수 조회
    private int getLimitCount() {
        SharedPreferences pref = getSharedPreferences("CallPop", Activity.MODE_PRIVATE);
        return pref.getInt("COUNT", 0);        //사용가능횟수 기본 3회
    }

    //사용제한수 조회
    private String getIsLock() {
        SharedPreferences pref = getSharedPreferences("CallPop", Activity.MODE_PRIVATE);
        return pref.getString("ISLOCK", "N");
    }

    //사용제한수 1차감
    private void minusCount() {
        SharedPreferences pref = getSharedPreferences("CallPop", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("COUNT", pref.getInt("COUNT",0) -1 );        //사용가능횟수 기본 3회
        editor.commit();
    }

    //사용제한 해제
    private void releaseLimit() {
        SharedPreferences pref = getSharedPreferences("CallPop", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("ISLOCK", "N");
        editor.commit();
    }

    //사용제한 알림창
    private void showLimitAlert()
    {

        btnSendLoc.setVisibility(View.GONE);
        btnShare.setVisibility(View.VISIBLE);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("사용제한");
        builder.setMessage("본 기능은 3회 무료 사용 가능합니다.\n친구 추천을 3회를 하시면 계속 무료 사용가능 합니다.");
        builder.setNeutralButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();
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
            Log.e("ReceiveListActivity", "result :: " + s);

            Gson gson = new Gson();

            if (s != null) {
                ArrayList<CommDomain> list = gson.fromJson(s, new TypeToken<ArrayList<CommDomain>>() {}.getType());
                Log.e("onPostExecute", "list size :" + list.size());
                for (CommDomain domain : list) {
                    Log.e("onPostExecute", domain.getId() + " :: " + domain.getName());
                }
            }
        }
    }

}
