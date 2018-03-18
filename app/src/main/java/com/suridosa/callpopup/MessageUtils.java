package com.suridosa.callpopup;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.kakao.kakaolink.KakaoLink;
import com.kakao.kakaolink.KakaoTalkLinkMessageBuilder;
import com.kakao.util.KakaoParameterException;

import java.util.ArrayList;

/**
 * Created by Administrator on 2018-02-12.
 */

public class MessageUtils {

    private static final String TAG = "MessageUtils";

    //SMS전송
    public static void sendSMS(Context context, String phoneNumber, String smsText) {

        PendingIntent sentIntent = PendingIntent.getBroadcast(context, 0, new Intent("SMS_SENT_ACTION"), 0);
        PendingIntent deliveredIntent = PendingIntent.getBroadcast(context, 0, new Intent("SMS_DELIVERED_ACTION"), 0);

        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch(getResultCode()){
                    case Activity.RESULT_OK:
                        // 전송 성공
                        Toast.makeText(context, "전송 완료", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        // 전송 실패
                        Toast.makeText(context, "전송 실패", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        // 서비스 지역 아님
                        Toast.makeText(context, "서비스 지역이 아닙니다", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        // 무선 꺼짐
                        Toast.makeText(context, "무선(Radio)가 꺼져있습니다", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        // PDU 실패
                        Toast.makeText(context, "PDU Null", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter("SMS_SENT_ACTION"));


        SmsManager mSmsManager = SmsManager.getDefault();
        mSmsManager.sendTextMessage(phoneNumber, null, smsText, sentIntent, deliveredIntent);


    }

    //
    public static void sendKakaoLink(Activity activity
                                      , KakaoLink kakaoLink
                                      , KakaoTalkLinkMessageBuilder kakaoTalkLinkMessageBuilder
                                      , String stringText
                                      , String stringImage) throws KakaoParameterException {

        try {
            kakaoTalkLinkMessageBuilder.addText(stringText);
            kakaoTalkLinkMessageBuilder.addImage(stringImage, 320, 320);
            //kakaoTalkLinkMessageBuilder.addWebLink("홈페이지 이동", stringUrl);
            kakaoTalkLinkMessageBuilder.addAppButton("앱열기");
            final String linkContents = kakaoTalkLinkMessageBuilder.build();
            kakaoLink.sendMessage(kakaoTalkLinkMessageBuilder, activity);
            //kakaoLink.sendMessage(kakaoTalkLinkMessageBuilder.build(), activity);
        } catch (KakaoParameterException e) {
            Log.e(TAG ,"[sendKakaoLink] "+e.getMessage());
            throw e;
        }

    }


    //전화번호부에서 이름,번호를 가져오는 메소드
    public static ArrayList getContactList(Context context, String gbn) {

        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.CONTACT_ID, // 연락처 ID -> 사진 정보 // 가져오는데 사용
                                              ContactsContract.CommonDataKinds.Phone.NUMBER, // 연락처
                                              ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME }; // 연락처 // 이름.
        String[] selectionArgs = null;
        String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
        Cursor contactCursor = context.getContentResolver().query(uri, projection, null, selectionArgs, sortOrder);

        ArrayList contactlist = new ArrayList();

        if (contactCursor.moveToFirst()) {
            do {
                String phonenumber = contactCursor.getString(1).replaceAll("-", "");
                if (phonenumber.length() == 10) {
                    phonenumber = phonenumber.substring(0, 3) + "-" + phonenumber.substring(3, 6) + "-" + phonenumber.substring(6);
                } else if (phonenumber.length() > 8) {
                    phonenumber = phonenumber.substring(0, 3) + "-" + phonenumber.substring(3, 7) + "-" + phonenumber.substring(7);
                }

                if( "NUMBER".equals(gbn) ) contactlist.add(phonenumber);
                if( "NAME".equals(gbn) ) contactlist.add(contactCursor.getString(2) + "  [" + phonenumber+"]");
                if( "NAME2".equals(gbn) ) contactlist.add(contactCursor.getString(2));

            }
            while (contactCursor.moveToNext());
        }
        return contactlist;
    }

}
