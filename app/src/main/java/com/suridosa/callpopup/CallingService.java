package com.suridosa.callpopup;

import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.internal.telephony.ITelephony;

import java.io.InputStream;
import java.lang.reflect.Method;

/**
 * Created by Administrator on 2018-02-02.
 */

public class CallingService extends Service implements View.OnClickListener {

    public static final String TAG = "CallingService";

    protected View rootView;
    protected View adView;

    private String in_number;
    private String in_name;
    private String state;
    private String contactId;
    private String in_address;

    private WindowManager.LayoutParams params;
    private WindowManager windowManager;

    @Override
    public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //getting the widget layout from xml using layout inflater
        rootView = LayoutInflater.from(this).inflate(R.layout.call_popup_widget, null);

        //setting the layout parameters
        params = new WindowManager.LayoutParams(   WindowManager.LayoutParams.WRAP_CONTENT,
                                                    WindowManager.LayoutParams.WRAP_CONTENT,
                                                    WindowManager.LayoutParams.TYPE_PHONE,
                                                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                                                    PixelFormat.TRANSLUCENT);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.addView(rootView, params);


        //adding click listener to close button and expanded view
        rootView.findViewById(R.id.hookon).setOnClickListener(this);
        rootView.findViewById(R.id.hookoff).setOnClickListener(this);
        rootView.findViewById(R.id.imgMapExp).setOnClickListener(this);
        rootView.findViewById(R.id.imgMapImp).setOnClickListener(this);
        rootView.findViewById(R.id.btn_close).setOnClickListener(this);

        adView =  (LinearLayout)rootView.findViewById(R.id.banner);

        //rootView.setOnClickListener(this);

        //화면이동
        rootView.findViewById(R.id.mainLayout).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_UP:
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        //this code is helping the widget to move around the screen with fingers
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(rootView, params);
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            in_number = intent.getStringExtra("IN_NUMBER");
            in_name = intent.getStringExtra("IN_NAME");
            state = intent.getStringExtra("STATE");
            contactId = intent.getStringExtra("CONTACTID");
            in_address = intent.getStringExtra("IN_ADDRESS");
        } catch (Exception e) {}

        if( "IDLE".equals(state) ) {
            adView.setVisibility(View.VISIBLE);
        } else {
            adView.setVisibility(View.GONE);
        }

        Log.e(TAG, "in_number :: "+in_number);
        Log.e(TAG, "in_name :: "+in_name);
        Log.e(TAG, "state :: "+state);
        Log.e(TAG, "contactId :: "+contactId);

        ((TextView)rootView.findViewById(R.id.tv_call_number)).setText(in_number);
        ((TextView)rootView.findViewById(R.id.tv_call_name)).setText(in_name);
        if( in_address != null ) {
            ((TextView) rootView.findViewById(R.id.tv_call_address)).setText(in_address);
        }

        byte[] arr = intent.getByteArrayExtra("photo");
        //if( contactId != null && !"".equals(contactId) ) {
//        if( arr != null && arr.length > 0 ) {
            Bitmap image = BitmapFactory.decodeByteArray(arr, 0, arr.length);
            ((ImageView)rootView.findViewById(R.id.imgPhoto)).setImageBitmap(image);
            //((ImageView)rootView.findViewById(R.id.imgPhoto)).setImageBitmap(getPhoto(Long.parseLong(contactId)));
//        } else {
//            ((ImageView)rootView.findViewById(R.id.imgPhoto)).setImageDrawable(getResources().getDrawable(R.drawable.no_photo));
//        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removePopup();
    }

    public void removePopup() {
        if (rootView != null && windowManager != null) windowManager.removeView(rootView);
    }

    @Override
    public void onClick(View v) {
        int vId = v.getId();
        Log.e(TAG, "onClick View Id ::: "+vId);
        switch(vId) {
            case R.id.hookon: //전화걸기
                hookOn();
                break;
            case R.id.imgMapImp: //받은위치 목록
                openMapImp();
                break;
            case R.id.imgMapExp : //내위치보내기
                openMapExp();
                break;
            case R.id.hookoff:  //전화끊기
                hookOff();
                break;
            case R.id.btn_close :  //창닫기
                stopSelf();
                break;
        }
    }

    //전화걸기
    private void hookOn() {

        //전화가 끊긴 상태
        if( "IDLE".equals(state) ) {
            Intent intent = new Intent("android.intent.action.CALL");
            intent.setData(Uri.parse("tel:"+in_number));
            try {
                startActivity(intent);
            } catch (Exception e) {
                Log.e("CallPopup",e.getMessage());
            }
        }

    }

    //전화끊기
    private void hookOff() {

        if( "OFFHOOK".equals(state) || "RINGING".equals(state) ) { //전화 통화나 벨이 울릴경우
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            try {
                Class<?> c = Class.forName(tm.getClass().getName());
                Method m = c.getDeclaredMethod("getITelephony");
                m.setAccessible(true);
                ITelephony telephonyService = (ITelephony) m.invoke(tm);
                telephonyService.endCall();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    //받은위치 목록
    public void openMapImp() {
        Intent mapIntent = new Intent(this, ReceiveListActivity.class);
        startActivity(mapIntent);
    }

    //내위치 보내기
    public void openMapExp() {
        Intent mapIntent = new Intent(this, MapActivity.class);
        startActivity(mapIntent);
    }


//    public Bitmap getPhoto(Long contactId) {
//        Uri contactPhotoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
//        InputStream photoDataStream = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(),contactPhotoUri);
//        Bitmap photo = BitmapFactory.decodeStream(photoDataStream);
//        return photo;
//    }

}
