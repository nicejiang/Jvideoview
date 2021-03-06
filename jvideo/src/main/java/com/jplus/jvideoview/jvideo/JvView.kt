package com.jplus.jvideoview.jvideo

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.SurfaceTexture
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.jplus.jvideoview.R
import com.jplus.jvideoview.common.JvConstant.PlayForm
import com.jplus.jvideoview.common.JvConstant.SwitchMode
import kotlinx.android.synthetic.main.layout_control_bottom.view.*
import kotlinx.android.synthetic.main.layout_control_center.view.*
import kotlinx.android.synthetic.main.layout_control_slide.view.*
import kotlinx.android.synthetic.main.layout_controller.view.*
import kotlinx.android.synthetic.main.layout_controller_top.view.*
import kotlinx.android.synthetic.main.layout_jvideo.view.*
import kotlinx.android.synthetic.main.layout_line_progress.view.*
import kotlinx.android.synthetic.main.view_jv_loading.view.*
import kotlinx.android.synthetic.main.view_jv_notice.view.*
import kotlinx.android.synthetic.main.view_jv_play.view.*


/**
 * @author JPlus
 * @date 2019/8/30.
 */
@RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class JvView : LinearLayout, JvContract.View, TextureView.SurfaceTextureListener {
    companion object {
        private const val BOTH_SIDES_MODE = 0
        private const val LEFT_SIDES_MODE = 1
        private const val RIGHT_SIDES_MODE = 2
        private const val NO_SIDES_MODE = 3
    }

    private var mPresenter: JvContract.Presenter? = null
    private var mView: View? = null
    private var mCenterIsShow = false

    constructor(context: Context) : super(context) {
        initControllerView(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initControllerView(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initControllerView(context, attrs)
    }

    @SuppressLint("Recycle")
    private fun initControllerView(context: Context, attrs: AttributeSet?) {
        mView = LayoutInflater.from(context).inflate(R.layout.layout_jvideo, this)

        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.JvView)
        val playColor = typeArray.getColor(
            R.styleable.JvView_play_btn_color,
            ContextCompat.getColor(context, R.color.video_play_color)
        )
        val progressBackground =
            typeArray.getDrawable(R.styleable.JvView_progress_drawer) ?: ContextCompat.getDrawable(
                context,
                R.drawable.draw_seek_bar
            )

        val lineBackground =
            typeArray.getDrawable(R.styleable.JvView_lines_drawer) ?: ContextCompat.getDrawable(
                context,
                R.drawable.draw_line_progress
            )

        val thumbColor = typeArray.getColor(
            R.styleable.JvView_thumb_color,
            ContextCompat.getColor(context, R.color.video_play_color)
        )
        val loadingColor = typeArray.getColor(
            R.styleable.JvView_loading_color,
            ContextCompat.getColor(context, R.color.video_play_color)
        )
        val numMode = typeArray.getInt(R.styleable.JvView_num_progress_mode, 0)

        //设置两个播放按钮的颜色
        vpv_video_center_play?.setColor(playColor, playColor)
        vpv_video_control_play?.setColor(playColor, playColor)
        //设置进度条
        progressBackground.let {
            seek_video_progress?.progressDrawable = progressBackground
        }
        //设置滑块颜色
        seek_video_progress?.thumb?.setColorFilter(thumbColor, PorterDuff.Mode.SRC_ATOP)
        //设置底部进度条
        lineBackground?.let {
            pgb_video_line_progress?.progressDrawable = lineBackground
        }
        //设置loading的颜色
        vlv_video_loading?.setColor(loadingColor)
        when (numMode) {
            BOTH_SIDES_MODE -> {
                ly_tv_progress_left?.findViewById<TextView>(R.id.tv_video_playing_count)?.visibility =
                    GONE
                ly_tv_progress_left?.findViewById<TextView>(R.id.tv_video_playing_split)?.visibility =
                    GONE
                ly_tv_progress_right?.findViewById<TextView>(R.id.tv_video_playing_split)
                    ?.visibility = GONE
                ly_tv_progress_right?.findViewById<TextView>(R.id.tv_video_playing_progress)
                    ?.visibility = GONE
            }
            LEFT_SIDES_MODE -> ly_tv_progress_right?.visibility = GONE
            RIGHT_SIDES_MODE -> ly_tv_progress_left?.visibility = GONE
            NO_SIDES_MODE -> {
                ly_tv_progress_right?.visibility = GONE
                ly_tv_progress_left?.visibility = GONE
            }
        }

    }

    private fun isBanTouch(isBan: Boolean) {
        //第一次预加载完成前禁止点击
        this?.setOnTouchListener { _, _ -> isBan }
        if (!isBan) {
            ly_video_center?.setOnTouchListener { v, event ->
                //屏幕滑动设置播放参数
                mPresenter?.slideJudge(v, event)
                true
            }
        }
    }

    private fun initListener(context: Context) {
        isBanTouch(true)
        val typeFace = Typeface.createFromAsset(context.assets, "iconfont.ttf");
        mPresenter?.setPlayForm(PlayForm.PLAY_FORM_TURN)
        //设置Texture监听
        ttv_video_player?.surfaceTextureListener = this

        //控件的点击、拖动、滑动事件
        vpv_video_center_play?.setOnClickListener {
            mPresenter?.controlPlay()
        }
        vpv_video_control_play?.setOnClickListener {
            mPresenter?.controlPlay()
        }
        seek_video_progress?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                //seekBar滑动中的回调
                mPresenter?.seekingPlay(seekBar.progress.toLong(), false)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                //seekBar开始滑动的回调
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                //seekBar滑动结束的回调
                mPresenter?.seekCompletePlay(seekBar.progress.toLong())
            }

        })
        //设置图标typeface
        tv_icon_volume_open.typeface = typeFace
        tv_icon_screen_change.typeface = typeFace
        tv_icon_back.typeface = typeFace
        tv_battery_info.typeface = typeFace
        tv_battery_info.typeface = typeFace
        tv_next_video.typeface = typeFace

        tv_icon_screen_change?.setOnClickListener {
            mPresenter?.switchFullOrWindowMode(SwitchMode.SWITCH_FULL_OR_NORMAL, false)
        }
        tv_video_refresh?.setOnClickListener {
            //重新播放
            mPresenter?.reStartPlay()
        }
        tv_icon_volume_open?.setOnClickListener {
            mPresenter?.switchVolumeMute()
        }
        tv_icon_back?.setOnClickListener {
            mPresenter?.exitFullOrWindowMode(isBackNormal = true, isRotateScreen = false)
        }
        tv_next_video?.setOnClickListener {
            mPresenter?.nextPlay()
        }
    }

    override fun reset() {
        seek_video_progress?.max = 0
        seek_video_progress?.progress = 0
        seek_video_progress?.secondaryProgress = 0

        pgb_video_line_progress?.max = 0
        pgb_video_line_progress?.progress = 0
        pgb_video_line_progress?.secondaryProgress = 0
    }

    override fun showSwitchEngine(isSupport: Boolean) {

    }

    override fun setVolumeMute(isMute: Boolean) {
        if (isMute) {
            tv_icon_volume_open?.text = resources.getString(R.string.icon_audio_close)
        } else {
            tv_icon_volume_open?.text = resources.getString(R.string.icon_audio_open)
        }
    }

    override fun setThumbnail(bitmap: Bitmap?) {
        rl_controller_layout?.background = BitmapDrawable(null, bitmap)
    }

    override fun showNetSpeed(speed: String) {
        if (tv_video_loading?.visibility == VISIBLE) {
            tv_video_loading?.text = speed
        }
    }

    override fun setPresenter(t: JvContract.Presenter) {
        mPresenter = t
        initListener(context)
    }

    override fun setTitle(title: String) {
        tv_video_title?.text = title
    }

    override fun showMessagePrompt(message: String, isShow: Boolean) {
        ly_video_hint?.visibility = View.VISIBLE
        tv_video_refresh?.text = "重新播放"
        tv_video_refresh?.visibility = if (isShow) View.VISIBLE else View.GONE
        tv_video_hint?.text = message
    }

    override fun closeMessagePrompt() {
        ly_video_hint?.visibility = View.GONE
    }

    override fun preparedVideo(videoTime: String, start: Int, max: Int) {
        isBanTouch(false)
        setNumProgress(videoTime)

        seek_video_progress?.max = max
        seek_video_progress?.progress = start

        pgb_video_line_progress?.max = max
        pgb_video_line_progress?.progress = start

        showBottomControlUi()
        showTopBarUi()
        showCenterPlayView()

        vpv_video_control_play?.pause()
        vpv_video_center_play?.pause()
    }

    override fun startVideo() {
//        rl_controller_layout.setBackgroundResource(0)
//        closeCenterHintView()
        vpv_video_control_play?.play()
        vpv_video_center_play?.play()
    }


    override fun showBuffering(percent: Int) {
        seek_video_progress?.secondaryProgress = (seek_video_progress.max) * percent / 100
        if (ly_video_line?.visibility == VISIBLE) {
            pgb_video_line_progress?.secondaryProgress = (seek_video_progress.max) * percent / 100
        }
    }

    override fun continueVideo() {
        vpv_video_control_play?.play()
        vpv_video_center_play?.play()
    }

    override fun pauseVideo() {
        vpv_video_control_play?.pause()
        vpv_video_center_play?.pause()
    }

    private fun setNumProgress(videoTime: String) {
        ly_tv_progress_left?.findViewById<TextView>(R.id.tv_video_playing_progress)?.text =
            videoTime.split("&")[0]
        ly_tv_progress_left?.findViewById<TextView>(R.id.tv_video_playing_count)?.text =
            videoTime.split("&")[1]
        ly_tv_progress_right?.findViewById<TextView>(R.id.tv_video_playing_progress)?.text =
            videoTime.split("&")[0]
        ly_tv_progress_right?.findViewById<TextView>(R.id.tv_video_playing_count)?.text =
            videoTime.split("&")[1]
    }

    override fun playing(videoTime: String, position: Long) {
        setNumProgress(videoTime)
        seek_video_progress?.progress = position.toInt()
        if (ly_video_line?.visibility == VISIBLE) {
            pgb_video_line_progress?.progress = position.toInt()
        }
    }

    override fun setLightUi(light: Int) {
        showTopAdjustUi("亮度", light, 100)
    }

    override fun setVolumeUi(volumePercent: Double) {
        Log.d(JvCommon.TAG, "volumePercent:${volumePercent}")
        tv_icon_volume_open?.text = resources.getString(R.string.icon_audio_open)
        showTopAdjustUi("音量", volumePercent.toInt(), 100)
    }

    override fun seekingVideo(videoTime: String, position: Long, isSlide: Boolean) {
        setNumProgress(videoTime)
        if (isSlide) {
            showTopAdjustUi(
                "进度\n${videoTime.split("&")[0]}/${videoTime.split("&")[1]}",
                position.toInt(),
                seek_video_progress?.max ?: 0
            )
            seek_video_progress?.progress = position.toInt()
            if (ly_video_line?.visibility == VISIBLE) {
                pgb_video_line_progress?.progress = position.toInt()
            }
        }
    }

    override fun showLoading(text: String) {
        Log.d(JvCommon.TAG, "showLoading")
        tv_video_loading?.text = text
        ly_video_loading?.visibility = VISIBLE
        //中间播放按钮的ui是否显示
        if (ly_video_play?.visibility == VISIBLE) {
            closeCenterPlayView()
        }
//        if(ly_video_hint.visibility == VISIBLE){
//            closeCenterHintView()
//        }
    }

    override fun showSysInfo(isShow: Boolean) {
        tc_video_sys_time?.visibility = if (isShow) VISIBLE else GONE
        tv_battery_info?.visibility = if (isShow) VISIBLE else GONE
    }

    override fun closeLoading(text: String) {
        Log.d(JvCommon.TAG, "closeLoading")
        ly_video_loading?.visibility = GONE
        if (!mCenterIsShow && ly_video_play.visibility == GONE) {
            showCenterPlayView()
        }
    }

    private fun showBottomLineUi() {
        Log.d(JvCommon.TAG, "showBottomLineUi")
        ly_video_line?.visibility = VISIBLE
    }

    private fun hideBottomLineUi() {
        Log.d(JvCommon.TAG, "hideBottomLineUi")
        ly_video_line?.visibility = GONE
    }

    private fun closeBottomControlUi() {
        Log.d(JvCommon.TAG, "closeBottomControlUi")
        ly_video_bottom_controller?.visibility = GONE
    }

    private fun showBottomControlUi() {
        Log.d(JvCommon.TAG, "showBottomControlUi")
        ly_video_bottom_controller?.visibility = VISIBLE
    }

    private fun closeTopBarUi() {
        Log.d(JvCommon.TAG, "closeTopControlUi")
        ly_video_title?.visibility = GONE
    }

    private fun showTopBarUi() {
        Log.d(JvCommon.TAG, "showTopControlUi")
        ly_video_title?.visibility = VISIBLE
    }

    override fun closeCenterPlayView() {
        mCenterIsShow = false
        Log.d(JvCommon.TAG, "closeCenterControlView")
        ly_video_play?.visibility = GONE
    }

    override fun showCenterPlayView() {
        mCenterIsShow = true
        Log.d(JvCommon.TAG, "showCenterControlView")
        ly_video_play?.visibility = VISIBLE

    }

    private fun showTopAdjustUi(title: String, progress: Int, max: Int) {
        Log.d(JvCommon.TAG, "showTopAdjustUi")
        ly_video_slide?.visibility = VISIBLE
        tv_slide_top?.text = title
        pgb_slide_top?.max = max
        pgb_slide_top?.progress = progress
    }

    override fun hideTopAdjustUi() {
        Log.d(JvCommon.TAG, "hideAdjustUi")
        ly_video_slide?.visibility = GONE
    }

    override fun showBatteryInfo(battery: Double, isCharge: Boolean) {
        Log.d(JvCommon.TAG, "battery:${battery}, isCharge:${isCharge}")
        when {
            battery > 90.0 -> tv_battery_info?.text =
                resources.getString(if (isCharge) R.string.icon_battery_recharge_full else R.string.icon_battery_full)
            battery < 90.0 && battery > 75.0 -> tv_battery_info?.text =
                resources.getString(if (isCharge) R.string.icon_battery_recharge_three_quarters else R.string.icon_battery_three_quarters)
            battery < 75.0 && battery > 50.0 -> tv_battery_info?.text =
                resources.getString(if (isCharge) R.string.icon_battery_recharge_half else R.string.icon_battery_half)
            battery < 50.0 && battery > 25.0 -> tv_battery_info?.text =
                resources.getString(if (isCharge) R.string.icon_battery_recharge_one else R.string.icon_battery_one)
            battery < 25.0 -> tv_battery_info?.text =
                resources.getString(if (isCharge) R.string.icon_battery_recharge_empty else R.string.icon_battery_empty)
        }
    }

    override fun entryFullMode() {
        Log.d(JvCommon.TAG, "entryFullMode")
        tv_icon_screen_change?.text = resources.getString(R.string.icon_normal_screen)
        //显示返回键
        tv_icon_back?.visibility = VISIBLE
        ly_video_bottom_controller?.setPadding(resources.getDimensionPixelSize(R.dimen.view_margin_bigger), 0, resources.getDimensionPixelSize(R.dimen.view_margin_bigger), 0)
        ly_video_title?.setPadding(resources.getDimensionPixelSize(R.dimen.view_margin_bigger), 0, resources.getDimensionPixelSize(R.dimen.view_margin_bigger), 0)
    }

    override fun exitMode() {
        Log.d(JvCommon.TAG, "exitMode")
        tv_icon_screen_change?.text = resources.getString(R.string.icon_full_screen)
        //隐藏返回键
        tv_icon_back?.visibility = GONE
        ly_video_title?.setPadding(0, 0, 0, 0)
        ly_video_bottom_controller?.setPadding(0, 0, 0, 0)
    }

    override fun hideController() {
        closeBottomControlUi()
        closeTopBarUi()
        closeCenterPlayView()
        showBottomLineUi()
    }

    override fun showController() {
        showBottomControlUi()
        showTopBarUi()
        hideBottomLineUi()
    }

    override fun onConfigChanged() {

    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
        //这里是改变后的画布大小
        Log.d(JvCommon.TAG, "onSurfaceTextureSizeChanged:$width - $height")
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {

    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        Log.d(JvCommon.TAG, "onSurfaceTextureDestroyed")
        mPresenter?.releasePlay(false)
        vlv_video_loading?.close()
        return false
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        //这里是原始画布大小
        Log.d(JvCommon.TAG, "onSurfaceTextureAvailable:$width - $height")
        mPresenter?.textureReady(surface, ttv_video_player)
    }
}