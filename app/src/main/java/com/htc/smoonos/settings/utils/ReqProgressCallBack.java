package com.htc.smoonos.settings.utils;
/**
 * @author  作者：zgr
 * @version 创建时间：2017年7月4日 上午9:05:46
 * 类说明
 */
public interface ReqProgressCallBack<T> {
    /**
     * 响应成功
     */
     void onReqSuccess(T result);

    /**
     * 响应失败
     */
     void onReqFailed(String errorMsg);
     
     
     void onReqProgress(long max, long progress);
}