package com.example.onyxapp

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder

class OnyxApp : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        SupabaseConfig.init(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(SvgDecoder.Factory())
            }
            .crossfade(true)
            .build()
    }
}