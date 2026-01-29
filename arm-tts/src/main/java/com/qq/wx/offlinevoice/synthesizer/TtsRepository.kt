package com.qq.wx.offlinevoice.synthesizer

import androidx.collection.LruCache
import com.qq.wx.offlinevoice.synthesizer.cache.TtsCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class TtsRepository(
    private val cache: TtsCache,
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        private val pcmMemoryCache =
            object : LruCache<String, DecodedPcm>(10 * 1024 * 1024) {
                override fun sizeOf(
                    key: String,
                    value: DecodedPcm,
                ): Int {
                    // 假设 DecodedPcm 有一个 pcmData 属性，存储 PCM 字节数组
                    // LruCache 的 size 参数单位是 bytes，因此这里返回字节数
                    return value.pcmData.size
                }
            }
    }

    fun clearCache() {
        scope.launch {
            cache.clear()
            pcmMemoryCache.evictAll()
        }
    }
}

internal class ForbiddenNetworkException(
    message: String,
) : Exception(message)
