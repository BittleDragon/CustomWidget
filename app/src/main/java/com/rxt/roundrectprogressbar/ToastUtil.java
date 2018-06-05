package com.rxt.roundrectprogressbar;

import android.content.Context;
import android.widget.Toast;

/**
 * Desc:
 * Company: xuehai
 * Copyright: Copyright (c) 2016
 *
 * @author raoxuting
 * @version 1.0
 * @since 2018/3/20 0020
 */


public class ToastUtil {

    private static Toast mToast;

    public static void showToast(Context context, String content) {
        if (mToast == null) {
            mToast = Toast.makeText(context.getApplicationContext(), content, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(content);
        }
        mToast.show();
    }
}
