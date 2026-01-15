package com.king.keyboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.drawable.DrawableCompat
import io.github.iur.arm.keyboard.R

/**
 * KingKeyboardView 自定义键盘View
 */
@Suppress("unused")
open class KingKeyboardView : KeyboardView {
    private var isCap = false

    private var isAllCaps = false

    private lateinit var config: Config

    private val paint by lazy { Paint() }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr,
    ) {
        init(context, attrs)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int,
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }

    /**
     * 初始化
     */
    private fun init(
        context: Context,
        attrs: AttributeSet?,
    ) {
        context.withStyledAttributes(attrs, R.styleable.KingKeyboardView) {
            val count = indexCount
            config =
                Config(context).apply {
                    for (i in 0 until count) {
                        when (val attr = getIndex(i)) {
                            R.styleable.KingKeyboardView_kkbDeleteDrawable -> {
                                deleteDrawable =
                                    getDrawable(attr)
                            }

                            R.styleable.KingKeyboardView_kkbCapitalDrawable -> {
                                capitalDrawable =
                                    getDrawable(attr)
                            }

                            R.styleable.KingKeyboardView_kkbCapitalLockDrawable -> {
                                capitalLockDrawable =
                                    getDrawable(attr)
                            }

                            R.styleable.KingKeyboardView_kkbCancelDrawable -> {
                                cancelDrawable =
                                    getDrawable(attr)
                            }

                            R.styleable.KingKeyboardView_kkbSpaceDrawable -> {
                                spaceDrawable =
                                    getDrawable(attr)
                            }

                            R.styleable.KingKeyboardView_android_labelTextSize -> {
                                labelTextSize =
                                    getDimensionPixelSize(attr, labelTextSize)
                            }

                            R.styleable.KingKeyboardView_android_keyTextSize -> {
                                keyTextSize =
                                    getDimensionPixelSize(attr, keyTextSize)
                            }

                            R.styleable.KingKeyboardView_android_keyTextColor -> {
                                keyTextColor =
                                    getColor(attr, keyTextColor)
                            }

                            R.styleable.KingKeyboardView_kkbKeyIconColor -> {
                                keyIconColor =
                                    getColor(
                                        attr,
                                        ContextCompat.getColor(
                                            context,
                                            R.color.king_keyboard_key_icon_color,
                                        ),
                                    )
                            }

                            R.styleable.KingKeyboardView_kkbKeySpecialTextColor -> {
                                keySpecialTextColor =
                                    getColor(attr, keySpecialTextColor)
                            }

                            R.styleable.KingKeyboardView_kkbKeyDoneTextColor -> {
                                keyDoneTextColor =
                                    getColor(attr, keyDoneTextColor)
                            }

                            R.styleable.KingKeyboardView_kkbKeyNoneTextColor -> {
                                keyNoneTextColor =
                                    getColor(attr, keyNoneTextColor)
                            }

                            R.styleable.KingKeyboardView_android_keyBackground -> {
                                keyBackground =
                                    getDrawable(attr)
                            }

                            R.styleable.KingKeyboardView_kkbSpecialKeyBackground -> {
                                specialKeyBackground =
                                    getDrawable(attr)
                            }

                            R.styleable.KingKeyboardView_kkbDoneKeyBackground -> {
                                doneKeyBackground =
                                    getDrawable(attr)
                            }

                            R.styleable.KingKeyboardView_kkbNoneKeyBackground -> {
                                noneKeyBackground =
                                    getDrawable(attr)
                            }

                            R.styleable.KingKeyboardView_kkbKeyDoneTextSize -> {
                                keyDoneTextSize =
                                    getDimensionPixelSize(attr, keyDoneTextSize)
                            }

                            R.styleable.KingKeyboardView_kkbKeyDoneText -> {
                                keyDoneText = getString(attr)
                            }
                        }
                    }
                }
        }

        paint.textAlign = Paint.Align.CENTER
        paint.isAntiAlias = true
    }

    fun getConfig(): Config = config

    fun setConfig(config: Config) {
        this.config = config
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawKeyboard(canvas, keyboard?.keys)
    }

    /**
     * 绘制键盘
     */
    private fun drawKeyboard(
        canvas: Canvas,
        keys: List<Keyboard.Key>?,
    ) {
        keys?.also {
            for (key in it) {
                drawKey(canvas, key)
            }
        }
    }

    /**
     * 绘制键盘按键
     */
    private fun drawKey(
        canvas: Canvas,
        key: Keyboard.Key,
    ) {
        when (key.codes[0]) {
            KingKeyboard.KEYCODE_SHIFT -> {
                drawShiftKey(canvas, key)
            }

            KingKeyboard.KEYCODE_MODE_CHANGE -> {
                drawKey(
                    canvas,
                    key,
                    config.specialKeyBackground,
                    config.keySpecialTextColor,
                )
            }

            KingKeyboard.KEYCODE_CANCEL -> {
                drawCancelKey(canvas, key)
            }

            KingKeyboard.KEYCODE_DONE -> {
                drawDoneKey(canvas, key)
            }

            KingKeyboard.KEYCODE_DELETE -> {
                drawDeleteKey(canvas, key)
            }

            KingKeyboard.KEYCODE_ALT -> {
                drawAltKey(canvas, key)
            }

            KingKeyboard.KEYCODE_SPACE -> {
                drawKey(
                    canvas,
                    key,
                    config.keyBackground,
                    config.keyTextColor,
                    config.spaceDrawable,
                )
            }

            KingKeyboard.KEYCODE_NONE -> {
                drawNoneKey(canvas, key)
            }

            KingKeyboard.KEYCODE_MODE_BACK -> {
                drawKey(
                    canvas,
                    key,
                    config.specialKeyBackground,
                    config.keySpecialTextColor,
                )
            }

            KingKeyboard.KEYCODE_BACK -> {
                drawKey(
                    canvas,
                    key,
                    config.specialKeyBackground,
                    config.keySpecialTextColor,
                )
            }

            KingKeyboard.KEYCODE_MORE -> {
                drawKey(
                    canvas,
                    key,
                    config.specialKeyBackground,
                    config.keySpecialTextColor,
                )
            }

            in -399..-300 -> {
                drawKey(
                    canvas,
                    key,
                    config.specialKeyBackground,
                    config.keySpecialTextColor,
                )
            }

            else -> {
                drawKey(canvas, key, config.keyBackground, config.keyTextColor)
            }
        }
    }

    /**
     * 绘制Cancel键，常见于关闭键盘键
     */
    private fun drawCancelKey(
        canvas: Canvas,
        key: Keyboard.Key,
    ) {
        drawKey(
            canvas,
            key,
            config.specialKeyBackground,
            config.keySpecialTextColor,
            config.cancelDrawable,
        )
    }

    /**
     * 绘制Done键，常见于右下角蓝色的“确定”按键
     */
    private fun drawDoneKey(
        canvas: Canvas,
        key: Keyboard.Key,
    ) {
        config.keyDoneText?.also {
            key.label = it
        }
        drawKey(canvas, key, config.doneKeyBackground, config.keyDoneTextColor, null, true)
    }

    /**
     * 绘制None键
     */
    private fun drawNoneKey(
        canvas: Canvas,
        key: Keyboard.Key,
    ) {
        drawKey(canvas, key, config.noneKeyBackground, config.keyNoneTextColor)
    }

    /**
     * 绘制Alt键
     */
    private fun drawAltKey(
        canvas: Canvas,
        key: Keyboard.Key,
    ) {
        drawKey(canvas, key, config.specialKeyBackground, config.keySpecialTextColor)
    }

    /**
     * 绘制Delete键
     */
    private fun drawDeleteKey(
        canvas: Canvas,
        key: Keyboard.Key,
    ) {
        drawKey(
            canvas,
            key,
            config.specialKeyBackground,
            config.keySpecialTextColor,
            config.deleteDrawable,
        )
    }

    /**
     * 绘制Shift键
     */
    private fun drawShiftKey(
        canvas: Canvas,
        key: Keyboard.Key,
    ) {
        when {
            isAllCaps -> {
                drawKey(
                    canvas,
                    key,
                    config.specialKeyBackground,
                    config.keySpecialTextColor,
                    config.capitalLockDrawable,
                )
            }

            isCap -> {
                drawKey(
                    canvas,
                    key,
                    config.specialKeyBackground,
                    config.keySpecialTextColor,
                    config.capitalDrawable,
                )
            }

            else -> {
                drawKey(
                    canvas,
                    key,
                    config.specialKeyBackground,
                    config.keySpecialTextColor,
                    config.lowerDrawable,
                )
            }
        }
    }

    /**
     * 绘制键盘按键
     */
    private fun drawKey(
        canvas: Canvas,
        key: Keyboard.Key,
        keyBackground: Drawable?,
        textColor: Int,
        iconDrawable: Drawable? = key.icon,
        isDone: Boolean = false,
    ) {
        // 绘制按键背景
        keyBackground?.run {
            if (key.codes[0] != 0) {
                state = key.currentDrawableState
            }

            setBounds(
                key.x.plus(paddingLeft),
                key.y.plus(paddingTop),
                key.x.plus(paddingLeft).plus(key.width),
                key.y.plus(paddingTop).plus(key.height),
            )
            draw(canvas)
        }

        // 绘制键盘图标
        iconDrawable?.run {
            val drawable = DrawableCompat.wrap(this)
            config.keyIconColor?.takeIf { it != 0 }?.also {
                drawable.setTint(it)
            }

            key.icon = drawable

            var iconWidth = key.icon.intrinsicWidth.toFloat()
            var iconHeight = key.icon.intrinsicHeight.toFloat()

            val widthRatio = iconWidth.div(key.width.toFloat())
            val heightRatio = iconHeight.div(key.height.toFloat())

            // 当图标的宽占比小于等于高占比时，以高度比例为基准并控制在iconRatio比例范围内，进行同比例缩放
            if (widthRatio <= heightRatio) {
                val ratio = heightRatio.coerceAtMost(ICON_RATIO)
                iconWidth = iconWidth.div(heightRatio).times(ratio)
                iconHeight = iconHeight.div(heightRatio).times(ratio)
            } else { // 反之，则以宽度比例为基准并控制在iconRatio比例范围内，进行同比例缩放
                val ratio = widthRatio.coerceAtMost(ICON_RATIO)
                iconWidth = iconWidth.div(widthRatio).times(ratio)
                iconHeight = iconHeight.div(widthRatio).times(ratio)
            }
            val left =
                key.x
                    .plus(paddingLeft)
                    .plus(key.width.minus(iconWidth).div(2f))
                    .toInt()
            val top =
                key.y
                    .plus(paddingTop)
                    .plus(key.height.minus(iconHeight).div(2f))
                    .toInt()
            val right = left.plus(iconWidth).toInt()
            val bottom = top.plus(iconHeight).toInt()
            key.icon.setBounds(left, top, right, bottom)
            key.icon.draw(canvas)
        } ?: key.label?.also { label ->
            // 绘制键盘文字
            if (isDone) {
                paint.textSize = config.keyDoneTextSize.toFloat()
            } else if (label.length > 1 && key.codes.size < 2) { // 键盘key内容多个字符
                paint.textSize = config.labelTextSize.toFloat()
            } else {
                paint.textSize = config.keyTextSize.toFloat()
            }
            paint.color = textColor
            paint.typeface = Typeface.DEFAULT

            if (label.toString().contains("\n")) {
                canvas.drawMultilineText(
                    "保\n存",
                    key.x + paddingLeft + key.width / 2f,
                    key.y + paddingTop + key.height / 2f + (paint.textSize - paint.descent()) / 2f,
                    paint,
                )
            } else {
                canvas.drawText(
                    label.toString(),
                    key.x.plus(paddingLeft).plus(key.width.div(2f)),
                    key.y.plus(paddingTop).plus(key.height.div(2.0f)).plus(
                        paint.textSize.minus(paint.descent()).div(2.0f),
                    ),
                    paint,
                )
            }
        }
    }

    private fun Canvas.drawMultilineText(
        text: String,
        x: Float,
        y: Float,
        paint: Paint,
    ) {
        val lines = text.split("\n")
        var yOffset = 0f
        for (line in lines) {
            drawText(line, x, y + yOffset, paint)
            yOffset += paint.fontSpacing
        }
    }

    fun setCap(isCap: Boolean) {
        this.isCap = isCap
    }

    fun isCap(): Boolean = isCap

    fun setAllCaps(isAllCaps: Boolean) {
        this.isAllCaps = isAllCaps
    }

    fun isAllCaps(): Boolean = isAllCaps

    companion object {
        internal const val ICON_RATIO = 0.5f
    }

    /**
     * Config为KingKeyboard的配置类，方便统一管理配置信息
     */
    open class Config(
        context: Context,
    ) {
        var deleteDrawable =
            AppCompatResources.getDrawable(context, R.drawable.king_keyboard_key_delete)
        var lowerDrawable =
            AppCompatResources.getDrawable(context, R.drawable.king_keyboard_key_lower)
        var capitalDrawable =
            AppCompatResources.getDrawable(context, R.drawable.king_keyboard_key_cap)
        var capitalLockDrawable =
            AppCompatResources.getDrawable(context, R.drawable.king_keyboard_key_all_caps)
        var cancelDrawable =
            AppCompatResources.getDrawable(context, R.drawable.king_keyboard_key_cancel)
        var spaceDrawable =
            AppCompatResources.getDrawable(context, R.drawable.king_keyboard_key_space)

        var labelTextSize =
            context.resources.getDimensionPixelSize(R.dimen.king_keyboard_label_text_size)

        var keyTextSize = context.resources.getDimensionPixelSize(R.dimen.king_keyboard_text_size)

        var keyTextColor = ContextCompat.getColor(context, R.color.king_keyboard_key_text_color)

        var keyIconColor: Int? = null

        var keySpecialTextColor =
            ContextCompat.getColor(context, R.color.king_keyboard_key_special_text_color)

        var keyDoneTextColor =
            ContextCompat.getColor(context, R.color.king_keyboard_key_done_text_color)

        var keyNoneTextColor =
            ContextCompat.getColor(context, R.color.king_keyboard_key_none_text_color)

        var keyBackground =
            AppCompatResources.getDrawable(context, R.drawable.king_keyboard_key_bg_selector)

        var specialKeyBackground =
            AppCompatResources.getDrawable(
                context,
                R.drawable.king_keyboard_special_key_bg_selector,
            )

        var doneKeyBackground =
            AppCompatResources.getDrawable(context, R.drawable.king_keyboard_done_key_bg_selector)
        var noneKeyBackground =
            AppCompatResources.getDrawable(context, R.drawable.king_keyboard_none_key_bg_selector)

        var keyDoneTextSize =
            context.resources.getDimensionPixelSize(R.dimen.king_keyboard_done_text_size)

        var keyDoneText: CharSequence? = context.getString(R.string.king_keyboard_key_done_text)
    }
}
