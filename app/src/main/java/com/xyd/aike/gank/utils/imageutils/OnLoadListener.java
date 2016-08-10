package com.xyd.aike.gank.utils.imageutils;

import android.graphics.Bitmap;

/**
 * Created by xyd-dev on 16/8/9.
 *
 * @desc 图片加载完成的监听类
 */
public interface OnLoadListener {

    /**
     * 加载成功
     *
     * @param bitmap 加载成功返回的bitmap对象
     */
    void onSuccess(Bitmap bitmap);

    /**
     * 加载失败
     */
    void onFail();
}
