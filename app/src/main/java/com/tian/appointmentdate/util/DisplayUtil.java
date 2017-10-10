package com.tian.appointmentdate.util;

import android.content.Context;

/**
 * Created by hp on 2017/8/12.
 */

public class DisplayUtil {
    /**
     * dp转px
     *
     * @param dpValue dp值
     * @return px值
     */
    public static int dp2px(Context context,final float dpValue) {
        final float scale = context.getApplicationContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
