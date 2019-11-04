package com.jplus.jvideoview.utils

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.view.WindowManager
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor

/**
 * @author JPlus
 * @date 2019/9/2.
 */
object JVideoUtil {
        /**
         * 播放进度转换为时间
         * @param progress 进度(整型)
         */
        fun progress2Time(progress:Int?):String{
            return if(progress==null) {
                "00:00"
            }else {
                val simpleDate = SimpleDateFormat("mm:ss", Locale.CHINA)
                simpleDate.timeZone = TimeZone.getTimeZone("GMT+00:00")
                simpleDate.format(Date(progress.toLong()))
            }
        }
        /**
         * 将滑动的距离转为进度显示
         * @param distance 滑动的距离
         * @param duration 总进度
         * @param proportion 一次屏幕的滑动所占总进度的比例
         * @param all 总高度/总宽度
         */
        fun dt2progress(distance: Float, duration: Int, all: Int, proportion: Double): Double {
            return (distance * duration * proportion / all)
        }
        /**
         * 获取屏幕宽度
         * @param context 上下文
         */
        fun getPhoneDisplayWidth(context:Context): Int {
            return (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.width
        }
        /**
         * 获取屏幕高度
         * @param context 上下文
         */
        fun getPhoneDisplayHeight(context:Context): Int {
            return (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.height
        }

        /**
         * 获取视频流的第一帧图片
         * @param videoUri 在线视频的播放地址/本地视频的uri
         * @return Bitmap
         */
         fun getNetVideoBitmap(videoUri: String): Bitmap? {
            var bitmap: Bitmap? = null
            val retriever = MediaMetadataRetriever()
            try {
                //根据url获取缩略图
                retriever.setDataSource(videoUri, HashMap())
                //获得第一帧图片
                bitmap = retriever.frameAtTime
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            } finally {
                retriever.release()
            }
            return bitmap
        }
}