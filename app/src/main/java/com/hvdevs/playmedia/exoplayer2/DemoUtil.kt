//package com.hvdevs.playmedia.exoplayer2
//
//import android.content.Context
//import com.google.android.exoplayer2.database.DatabaseProvider
//import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
//import com.google.android.exoplayer2.upstream.DataSource
//import com.google.android.exoplayer2.upstream.DefaultDataSource
//import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
//import com.google.android.exoplayer2.upstream.HttpDataSource
//import com.google.android.exoplayer2.upstream.cache.Cache
//import com.google.android.exoplayer2.upstream.cache.CacheDataSource
//import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
//import com.google.android.exoplayer2.upstream.cache.SimpleCache
//import org.checkerframework.checker.nullness.qual.MonotonicNonNull
//import org.chromium.net.CronetEngine
//import java.io.File
//import java.net.CookieHandler
//import java.net.CookieManager
//import java.net.CookiePolicy
//import java.util.concurrent.Executors
//
///** Utility methods for the demo app.  */
//object DemoUtil {
//    const val DOWNLOAD_NOTIFICATION_CHANNEL_ID = "download_channel"
//
//    /**
//     * Whether the demo application uses Cronet for networking. Note that Cronet does not provide
//     * automatic support for cookies (https://github.com/google/ExoPlayer/issues/5975).
//     *
//     *
//     * If set to false, the platform's default network stack is used with a [CookieManager]
//     * configured in [.getHttpDataSourceFactory].
//     */
//    private const val USE_CRONET_FOR_NETWORKING = true
//    private const val TAG = "DemoUtil"
//    private const val DOWNLOAD_CONTENT_DIRECTORY = "downloads"
//    private var dataSourceFactory: @MonotonicNonNull DataSource.Factory? = null
//    private var httpDataSourceFactory: @MonotonicNonNull HttpDataSource.Factory? = null
//    private var databaseProvider: @MonotonicNonNull DatabaseProvider? = null
//    private var downloadDirectory: @MonotonicNonNull File? = null
//    private var downloadCache: @MonotonicNonNull Cache? = null
//
//    @Synchronized
//    fun getHttpDataSourceFactory(context: Context): HttpDataSource.Factory? {
//        var context = context
//        if (httpDataSourceFactory == null) {
//            if (USE_CRONET_FOR_NETWORKING) {
//                context = context.applicationContext
//                val cronetEngine: CronetEngine = CronetUtil.buildCronetEngine(context)!!
//                if (cronetEngine != null) {
//                    httpDataSourceFactory =
//                        com.google.android.exoplayer2.ext.cronet.CronetDataSource.Factory(
//                            cronetEngine,
//                            Executors.newSingleThreadExecutor()
//                        )
//                }
//            }
//            if (httpDataSourceFactory == null) {
//                // We don't want to use Cronet, or we failed to instantiate a CronetEngine.
//                val cookieManager = CookieManager()
//                cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER)
//                CookieHandler.setDefault(cookieManager)
//                httpDataSourceFactory = DefaultHttpDataSource.Factory()
//            }
//        }
//        return httpDataSourceFactory
//    }
//
//    /** Returns a [DataSource.Factory].  */
//    @Synchronized
//    fun getDataSourceFactory(context: Context): DataSource.Factory? {
//        var context = context
//        if (dataSourceFactory == null) {
//            context = context.applicationContext
//            val upstreamFactory = DefaultDataSource.Factory(
//                context,
//                getHttpDataSourceFactory(context)!!
//            )
//            dataSourceFactory =
//                buildReadOnlyCacheDataSource(upstreamFactory, getDownloadCache(context))
//        }
//        return dataSourceFactory
//    }
//
//    @Synchronized
//    private fun getDownloadCache(context: Context): Cache? {
//        if (downloadCache == null) {
//            val downloadContentDirectory =
//                File(getDownloadDirectory(context), DOWNLOAD_CONTENT_DIRECTORY)
//            downloadCache = SimpleCache(
//                downloadContentDirectory, NoOpCacheEvictor(),
//                getDatabaseProvider(context)!!
//            )
//        }
//        return downloadCache
//    }
//
//    @Synchronized
//    private fun getDatabaseProvider(context: Context): DatabaseProvider? {
//        if (databaseProvider == null) {
//            databaseProvider = StandaloneDatabaseProvider(context)
//        }
//        return databaseProvider
//    }
//
//    @Synchronized
//    private fun getDownloadDirectory(context: Context): File? {
//        if (downloadDirectory == null) {
//            downloadDirectory = context.getExternalFilesDir( /* type= */null)
//            if (downloadDirectory == null) {
//                downloadDirectory = context.filesDir
//            }
//        }
//        return downloadDirectory
//    }
//
//    private fun buildReadOnlyCacheDataSource(
//        upstreamFactory: DataSource.Factory, cache: Cache?
//    ): CacheDataSource.Factory {
//        return CacheDataSource.Factory()
//            .setCache(cache!!)
//            .setUpstreamDataSourceFactory(upstreamFactory)
//            .setCacheWriteDataSinkFactory(null)
//            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
//    }
//}
