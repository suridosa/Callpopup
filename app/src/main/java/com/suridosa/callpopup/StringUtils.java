package com.suridosa.callpopup;

/**
 * Created by Administrator on 2018-03-11.
 */

public class StringUtils {

    public static String nvl(String source) {
        if( source == null ) return "";
        else                 return source;
    }

    public static String nvl(String source, String replace) {
        if( source == null ) return replace;
        else                 return source;
    }

}
