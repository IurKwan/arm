package com.qq.wx.offlinevoice.synthesizer

import android.util.Log

internal class SynthesisStrategyManager {
    // 默认策略可以根据您的产品需求设定
    var currentStrategy: TtsStrategy = TtsStrategy.OFFLINE_ONLY
        private set

    fun setStrategy(strategy: TtsStrategy) {
        Log.i("StrategyManager", "TTS 策略已变更为: $strategy")
        this.currentStrategy = strategy
    }

    /**
     * --- 修改：让 getDesiredMode 成为一个纯函数 ---
     * 根据给定的策略和当前的网络状态，决定理想的合成模式。
     * 这使得 TtsSynthesizer 中的逻辑更可预测。
     *
     * @param strategy 要评估的TTS策略。
     * @return 理想的合成模式 (ONLINE 或 OFFLINE)。
     */
    fun getDesiredMode(strategy: TtsStrategy): SynthesisMode = SynthesisMode.OFFLINE

    fun release() {
        // networkMonitor.release()
    }
}

// 定义一个简单的枚举来代表单次会话的合成模式
enum class SynthesisMode {
    ONLINE,
    OFFLINE,
}
