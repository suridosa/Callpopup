package com.android.internal.telephony;

/**
 * Created by suridosa on 2018-02-14.
 */
public interface ITelephony {
    void answerRingingCall();
    boolean endCall();
    void silenceRinger();
    boolean showCallScreenWithDialpad(boolean showDialpad);
}
