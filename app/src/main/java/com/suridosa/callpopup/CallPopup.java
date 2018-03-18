package com.suridosa.callpopup;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.internal.telephony.ITelephony;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.lang.reflect.Method;

/**
 * Created by Administrator on 2018-02-02.
 */

public class CallPopup extends Activity implements View.OnTouchListener {

    private static final String TAG = "CallPopup";

    private TextView tv_call_name;
    private TextView tv_call_number;
    private TextView tv_state;
    private ImageButton btn_close;
    private ImageButton hookon, hookoff, imgMapExp, imgMapImp;
    private ImageView imgPhoto;

    private LinearLayout mainLayout;
    private LinearLayout topLayout;

    String state;
    String call_name;
    String call_number;
    String contactId;

//    WindowManager.LayoutParams params;
//    protected View rootView;
//    private WindowManager windowManager;

    float oldXvalue;
    float oldYvalue;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.call_popup_top);

        Intent intent = getIntent();
        call_name = intent.getStringExtra("IN_NAME");
        call_number = intent.getStringExtra("IN_NUMBER");
        state = intent.getStringExtra("STATE");
        contactId = intent.getStringExtra("CONTACTID");

        Log.e("CallPopup", "state :: "+state);

        mainLayout = (LinearLayout)findViewById(R.id.mainLayout);
        mainLayout.setOnTouchListener(this);

        tv_call_number = (TextView)findViewById(R.id.tv_call_number);
        tv_call_name = (TextView)findViewById(R.id.tv_call_name);
        imgPhoto = (ImageView)findViewById(R.id.imgPhoto);

        //tv_state = (TextView)findViewById(R.id.tv_state);
        btn_close = (ImageButton) findViewById(R.id.btn_close);
        btn_close.setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //전화받기
        hookon = (ImageButton) findViewById(R.id.hookon);
        hookon.setOnClickListener(
                new View.OnClickListener() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onClick(View view) {

                        //전화가 걸려온 경우
                        if( "RINGING".equals(state) ) {

//                            try
//                            {
//                                TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
//                                Class c = Class.forName(tm.getClass().getName());
//                                Method m = c.getDeclaredMethod("getITelephony");
//                                m.setAccessible(true);
//                                ITelephony telephonyService;
//                                telephonyService = (ITelephony)m.invoke(tm);
//
//                                // Silence the ringer and answer the call!
//                                telephonyService.silenceRinger();
//                                telephonyService.answerRingingCall();
//
//                            } catch (Exception e)
//                            {
//                                e.printStackTrace();
//                            }

//                            new_intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
//                            try {
//                                sendOrderedBroadcast(new_intent, "android.permission.CALL_PRIVILEGED");
//                            } catch (Exception e) {
//                                Log.e("CallPopup", e.getMessage());
//                            }

//                            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//                            try {
//                                Class<?> c = Class.forName(tm.getClass().getName());
//                                Method m = c.getDeclaredMethod("getITelephony");
//                                m.setAccessible(true);
//                                ITelephony telephonyService = (ITelephony) m.invoke(tm);
//                                telephonyService.silenceRinger();
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }


                        }

                        //전화가 끊긴 상태
                        if( "IDLE".equals(state) ) {
                            Intent intent = new Intent("android.intent.action.CALL");
                            intent.setData(Uri.parse("tel:"+call_number));
                            try {
                                startActivity(intent);
                            } catch (Exception e) {
                                Log.e("CallPopup",e.getMessage());
                            }

                        }
                    }
                });

        //전화끊기
        hookoff = (ImageButton) findViewById(R.id.hookoff);
        hookoff.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if( "OFFHOOK".equals(state) || "RINGING".equals(state) ) {
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
                });

        //MapActivity 호출
        imgMapExp = (ImageButton) findViewById(R.id.imgMapExp);
        imgMapExp.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent mapIntent = new Intent(CallPopup.this, MapActivity.class);
                        startActivity(mapIntent);
                    }
                });

        //ReceiveListActivity 호출
        imgMapImp = (ImageButton) findViewById(R.id.imgMapImp);
        imgMapImp.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent mapIntent = new Intent(CallPopup.this, ReceiveListActivity.class);
                        startActivity(mapIntent);
                    }
                });



        Log.e(TAG, "contactId :: "+contactId);

        if( contactId != null && !"".equals(contactId) ) {
            imgPhoto.setImageBitmap(getPhoto(Long.parseLong(contactId)));
        } else {
            imgPhoto.setImageDrawable(getResources().getDrawable(R.drawable.no_photo));
        }

        tv_call_name.setText(call_name);
        tv_call_number.setText(call_number);
        //tv_state.setText(state);

        ///////////////////////////////////////////////////////////////////////////////////////////////////
//        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
//        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
//        rootView = layoutInflater.inflate(R.layout.call_popup_top, null);
//        setDraggable();
        ///////////////////////////////////////////////////////////////////////////////////////////////////
    }



    public Bitmap getPhoto(Long contactId) {
        Uri contactPhotoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        InputStream photoDataStream = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(),contactPhotoUri);
        Bitmap photo = BitmapFactory.decodeStream(photoDataStream);
        return photo;
    }


//    private void setDraggable() {
//        rootView.setOnTouchListener(new View.OnTouchListener() {
//            private int initialX;
//            private int initialY;
//            private float initialTouchX;
//            private float initialTouchY;
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        initialX = params.x;
//                        initialY = params.y;
//                        initialTouchX = event.getRawX();
//                        initialTouchY = event.getRawY();
//                        return true;
//                    case MotionEvent.ACTION_UP:
//                        return true;
//                    case MotionEvent.ACTION_MOVE:
//                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
//                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
//                        if (rootView != null) windowManager.updateViewLayout(rootView, params);
//                        return true;
//                }
//                return false;
//            }
//        });
//    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int width = ((ViewGroup) v.getParent()).getWidth() - v.getWidth();
        int height = ((ViewGroup) v.getParent()).getHeight() - v.getHeight();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            oldXvalue = event.getX();
            oldYvalue = event.getY();
            //  Log.i("Tag1", "Action Down X" + event.getX() + "," + event.getY());
            Log.e("Tag1", "Action Down rX " + event.getRawX() + "," + event.getRawY());
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            v.setX(event.getRawX() - oldXvalue);
            v.setY(event.getRawY() - (oldYvalue + v.getHeight()));
            //  Log.i("Tag2", "Action Down " + me.getRawX() + "," + me.getRawY());
        } else if (event.getAction() == MotionEvent.ACTION_UP) {

            if (v.getX() > width && v.getY() > height) {
                v.setX(width);
                v.setY(height);
            } else if (v.getX() < 0 && v.getY() > height) {
                v.setX(0);
                v.setY(height);
            } else if (v.getX() > width && v.getY() < 0) {
                v.setX(width);
                v.setY(0);
            } else if (v.getX() < 0 && v.getY() < 0) {
                v.setX(0);
                v.setY(0);
            } else if (v.getX() < 0 || v.getX() > width) {
                if (v.getX() < 0) {
                    v.setX(0);
                    v.setY(event.getRawY() - oldYvalue - v.getHeight());
                } else {
                    v.setX(width);
                    v.setY(event.getRawY() - oldYvalue - v.getHeight());
                }
            } else if (v.getY() < 0 || v.getY() > height) {
                if (v.getY() < 0) {
                    v.setX(event.getRawX() - oldXvalue);
                    v.setY(0);
                } else {
                    v.setX(event.getRawX() - oldXvalue);
                    v.setY(height);
                }
            }


        }
        return true;
    }

}
