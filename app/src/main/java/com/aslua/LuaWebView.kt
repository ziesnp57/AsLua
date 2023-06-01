package com.aslua

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.net.http.SslError
import android.os.Message
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.KeyEvent
import android.webkit.ClientCertRequest
import android.webkit.DownloadListener
import android.webkit.HttpAuthHandler
import android.webkit.JavascriptInterface
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import com.luajava.LuaError
import com.luajava.LuaFunction
import com.yongle.aslua.R
import java.io.File
import java.util.Arrays


@SuppressLint("ViewConstructor", "SetJavaScriptEnabled")
class LuaWebView(context: LuaActivity) : WebView(context), LuaGcable {
    private var mDownloadBroadcastReceiver: DownloadBroadcastReceiver? = null
    private val mDownload = HashMap<Long, Array<String?>>()
    private var mOnDownloadCompleteListener: OnDownloadCompleteListener? = null
    private val mContext: LuaActivity
    private var mProgressbar: ProgressBar
    private val dm: DisplayMetrics
    private var open_dlg: Dialog? = null
    private var open_list: ListView? = null
    private var mUploadMessage: ValueCallback<Uri>? = null
    private var mDir = "/"
    private var mAdsFilter: LuaFunction<Boolean>? = null
    private var mfinished: LuaFunction<*>? = null
    private var mGc = false
    override fun gc() {
        destroy()
        mGc = true
    }

    override fun isGc(): Boolean {
        return mGc
    }

    fun setProgressBarEnabled(visibility: Boolean) {
        if (visibility) mProgressbar.visibility = VISIBLE else mProgressbar.visibility = GONE
    }

    fun setProgressBar(pb: ProgressBar) {
        mProgressbar = pb
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        val lp = mProgressbar.layoutParams as LayoutParams
        lp.x = l
        lp.y = t
        mProgressbar.layoutParams = lp
        super.onScrollChanged(l, t, oldl, oldt)
    }

    fun setOnDownloadStartListener(listener: OnDownloadStartListener) {
        setDownloadListener { p1, p2, p3, p4, p5 -> listener.onDownloadStart(p1, p2, p3, p4, p5) }
    }

    fun setOnDownloadCompleteListener(listener: OnDownloadCompleteListener?) {
        mOnDownloadCompleteListener = listener
    }

    override fun destroy() {
        if (mDownloadBroadcastReceiver != null) {
            mContext.unregisterReceiver(mDownloadBroadcastReceiver)
        }
        super.destroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && canGoBack()) {
            goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun setOnKeyListener(l: OnKeyListener) {
        // TODO: Implement this method
        super.setOnKeyListener(l)
    }

    @SuppressLint("AddJavascriptInterface")
    fun addJSInterface(`object`: JsInterface, name: String?) {
        // TODO: Implement this method
        super.addJavascriptInterface(JsObject(`object`), name!!)
    }

    @SuppressLint("AddJavascriptInterface")
    fun addJsInterface(`object`: JsInterface, name: String?) {
        // TODO: Implement this method
        super.addJavascriptInterface(JsObject(`object`), name!!)
    }

    fun setWebViewClient(client: LuaWebViewClient) {
        // TODO: Implement this method
        super.setWebViewClient(SimpleLuaWebViewClient(client))
    }

    fun openFile(dir: String?) {
        if (open_dlg == null) {
            open_dlg = Dialog(context)
            open_list = ListView(context)
            open_list!!.isFastScrollEnabled = true
            open_list!!.isFastScrollAlwaysVisible = true
            open_dlg!!.setContentView(open_list!!)
            open_list!!.onItemClickListener = OnItemClickListener { _, p2, _, _ ->
                val t = (p2 as TextView).text.toString()
                if (t == "../") {
                    mDir = File(mDir).parent!! + "/"
                    openFile(mDir)
                    return@OnItemClickListener
                }
                val fn = mDir + t
                val f = File(fn)
                if (f.isDirectory) {
                    mDir = fn
                    openFile(mDir)
                    return@OnItemClickListener
                }
                mUploadMessage!!.onReceiveValue(Uri.parse(fn))
            }
        }
        val d = File(dir)
        val ns = ArrayList<String>()
        ns.add("../")
        val fs = d.list()
        if (fs != null) {
            Arrays.sort(fs)
            for (k in fs) {
                if (File(mDir + k).isDirectory) ns.add("$k/")
            }
            for (k in fs) {
                if (File(mDir + k).isFile) ns.add(k)
            }
        }
        val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, ns)
        open_list!!.adapter = adapter
        open_dlg!!.setTitle(mDir)
        open_dlg!!.show()
    }

    interface OnDownloadCompleteListener {
        fun onDownloadComplete(fileName: String?, mimetype: String?)
    }

    interface OnDownloadStartListener {
        fun onDownloadStart(
            url: String?,
            userAgent: String?,
            contentDisposition: String?,
            mimetype: String?,
            contentLength: Long
        )
    }

    interface JsInterface {
        @JavascriptInterface
        fun execute(arg: String?): String
    }

    interface LuaWebViewClient {
        fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean
        fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?)
        fun onPageFinished(view: WebView?, url: String?)
        fun onLoadResource(view: WebView?, url: String?)
        fun shouldInterceptRequest(
            view: WebView?,
            url: String?
        ): WebResourceResponse?

        @Deprecated("")
        fun onTooManyRedirects(
            view: WebView?, cancelMsg: Message?,
            continueMsg: Message?
        )

        fun onReceivedError(
            view: WebView?, errorCode: Int,
            description: String?, failingUrl: String?
        )

        fun onFormResubmission(
            view: WebView?, dontResend: Message?,
            resend: Message?
        )

        fun doUpdateVisitedHistory(
            view: WebView?, url: String?,
            isReload: Boolean
        )

        fun onReceivedSslError(
            view: WebView?, handler: SslErrorHandler?,
            error: SslError?
        )

        fun onProceededAfterSslError(view: WebView?, error: SslError?)
        fun onReceivedClientCertRequest(
            view: WebView?,
            handler: ClientCertRequest?, host_and_port: String?
        )

        fun onReceivedHttpAuthRequest(
            view: WebView?,
            handler: HttpAuthHandler?, host: String?, realm: String?
        )

        fun shouldOverrideKeyEvent(view: WebView?, event: KeyEvent?): Boolean
        fun onUnhandledKeyEvent(view: WebView?, event: KeyEvent?)
        fun onScaleChanged(view: WebView?, oldScale: Float, newScale: Float)
        fun onReceivedLoginRequest(
            view: WebView?, realm: String?,
            account: String?, args: String?
        )

        companion object {
            /**
             * Generic error
             */
            const val ERROR_UNKNOWN = -1

            /**
             * Server or proxy hostname lookup failed
             */
            const val ERROR_HOST_LOOKUP = -2

            /**
             * Unsupported authentication scheme (not basic or digest)
             */
            const val ERROR_UNSUPPORTED_AUTH_SCHEME = -3

            /**
             * User authentication failed on server
             */
            const val ERROR_AUTHENTICATION = -4

            /**
             * User authentication failed on proxy
             */
            const val ERROR_PROXY_AUTHENTICATION = -5

            /**
             * Failed to connect to the server
             */
            const val ERROR_CONNECT = -6
            // These ints must match up to the hidden values in EventHandler.
            /**
             * Failed to read or write to the server
             */
            const val ERROR_IO = -7

            /**
             * Connection timed out
             */
            const val ERROR_TIMEOUT = -8

            /**
             * Too many redirects
             */
            const val ERROR_REDIRECT_LOOP = -9

            /**
             * Unsupported URI scheme
             */
            const val ERROR_UNSUPPORTED_SCHEME = -10

            /**
             * Failed to perform SSL handshake
             */
            const val ERROR_FAILED_SSL_HANDSHAKE = -11

            /**
             * Malformed URL
             */
            const val ERROR_BAD_URL = -12

            /**
             * Generic file error
             */
            const val ERROR_FILE = -13

            /**
             * File not found
             */
            const val ERROR_FILE_NOT_FOUND = -14

            /**
             * Too many requests during this load
             */
            const val ERROR_TOO_MANY_REQUESTS = -15
        }
    }

    private inner class DownloadBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(p1: Context, p2: Intent) {

            //id=p2.getLongExtra("flg", 0);
            //int id=p2.getFlags();
            val id = p2.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0)
            val bundle = p2.extras
            //bundle.g
            if (mDownload.containsKey(id)) {
                if (mOnDownloadCompleteListener != null) {
                    val data = mDownload[id]!!
                    mOnDownloadCompleteListener!!.onDownloadComplete(data[0], data[1])
                } else {
                }
            }
        }
    }

    private inner class Download : DownloadListener {
        var file_input_field: EditText? = null
        private var mUrl: String? = null
        private var mUserAgent: String? = null
        private var mContentDisposition: String? = null
        private var mMimetype: String? = null
        private var mContentLength: Long = 0
        private var mFilename: String? = null
        @SuppressLint("DefaultLocale")
        override fun onDownloadStart(
            url: String,
            userAgent: String,
            contentDisposition: String,
            mimetype: String,
            contentLength: Long
        ) {
            // TODO: Implement this method
            mUrl = url
            mUserAgent = userAgent
            mContentDisposition = contentDisposition
            mMimetype = mimetype
            mContentLength = contentLength
            val uri = Uri.parse(mUrl)
            mFilename = uri.lastPathSegment
            val p = "filename=\""
            var i = contentDisposition.indexOf(p)
            if (i != -1) {
                i += p.length
                val n = contentDisposition.indexOf('"', i)
                if (n > i) mFilename = contentDisposition.substring(i, n)
            }
            file_input_field = EditText(mContext)
            //file_input_field.setTextColor(0xff000000);
            file_input_field!!.setText(mFilename)
            var size = contentLength.toString() + "B"
            if (contentLength > 1024 * 1024) size = String.format(
                "%.2f MB",
                java.lang.Long.valueOf(contentLength).toDouble() / (1024 * 1024)
            ) else if (contentLength > 1024) size =
                String.format("%.2f KB", java.lang.Long.valueOf(contentLength).toDouble() / 1024)
            AlertDialog.Builder(mContext)
                .setTitle(DOWNLOAD)
                .setMessage("Type: $mimetype\nSize: $size")
                .setView(file_input_field)
                .setPositiveButton(DOWNLOAD) { _, _ ->
                    mFilename = file_input_field!!.text.toString()
                    download(false)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton("Only Wifi") { _, _ ->
                    mFilename = file_input_field!!.text.toString()
                    download(true)
                }
                .create()
                .show()
        }

        private fun download(isWifi: Boolean): Long {
            if (mDownloadBroadcastReceiver == null) {
                val filter = IntentFilter()
                filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                mDownloadBroadcastReceiver = DownloadBroadcastReceiver()
                mContext.registerReceiver(mDownloadBroadcastReceiver, filter)
            }
            val downloadManager =
                mContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(mUrl)
            uri.lastPathSegment
            val request = DownloadManager.Request(uri)
            val dir = mContext.getLuaExtDir(DOWNLOAD)
            request.setDestinationInExternalPublicDir(
                File(mContext.luaExtDir).name + "/" + DOWNLOAD,
                mFilename
            )
            request.setTitle(mFilename)
            request.setDescription(mUrl)

            //request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            if (isWifi) request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)

            //request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
            val f = File(dir, mFilename!!)
            if (f.exists()) f.delete()
            request.setMimeType(mMimetype)
            //Environment.getExternalStoragePublicDirectory(dirType)
            val downloadId = downloadManager.enqueue(request)
            mDownload[downloadId] = arrayOf(File(dir, mFilename!!).absolutePath, mMimetype)
            return downloadId
        }
    }

    internal inner class JsObject(private val mJs: JsInterface) {
        @JavascriptInterface
        fun execute(arg: String?): String {
            return mJs.execute(arg)
        }
    }

    private inner class LuaJavaScriptInterface(private val mMain: LuaActivity) {
        @JavascriptInterface
        fun callLuaFunction(name: String?): Any {
            return mMain.runFunc(name)
        }

        @JavascriptInterface
        fun callLuaFunction(name: String?, arg: String?): Any {
            return mMain.runFunc(name, arg)
        }

        @JavascriptInterface
        fun doLuaString(name: String?): Any {
            return mMain.doString(name)
        }
    }

    fun setAdsFilter(filter: LuaFunction<Boolean>?) {
        mAdsFilter = filter
    }

    fun setFinished(f: LuaFunction<*>?) {
        mfinished = f
    }

    private inner class SimpleLuaWebViewClient(private val mLuaWebViewClient: LuaWebViewClient) :
        WebViewClient() {
        @Deprecated("Deprecated in Java")
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            return mLuaWebViewClient.shouldOverrideUrlLoading(view, url)
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap) {
            mLuaWebViewClient.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView, url: String) {
            mLuaWebViewClient.onPageFinished(view, url)
        }

        override fun onLoadResource(view: WebView, url: String) {
            mLuaWebViewClient.onLoadResource(view, url)
        }

        @Deprecated("Deprecated in Java")
        override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
            if (mAdsFilter != null) {
                try {
                    if (mAdsFilter!!.call(url)) return WebResourceResponse(null, null, null)
                } catch (e: LuaError) {
                    e.printStackTrace()
                }
            }
            return mLuaWebViewClient.shouldInterceptRequest(view, url)
        }

        override fun shouldInterceptRequest(
            view: WebView,
            request: WebResourceRequest
        ): WebResourceResponse? {
            return super.shouldInterceptRequest(view, request)
        }

        @Deprecated("", ReplaceWith("cancelMsg.sendToTarget()"))
        override fun onTooManyRedirects(
            view: WebView, cancelMsg: Message,
            continueMsg: Message
        ) {
            cancelMsg.sendToTarget()
        }

        @Deprecated("Deprecated in Java")
        override fun onReceivedError(
            view: WebView, errorCode: Int,
            description: String, failingUrl: String
        ) {
            mLuaWebViewClient.onReceivedError(view, errorCode, description, failingUrl)
        }

        override fun onFormResubmission(
            view: WebView, dontResend: Message,
            resend: Message
        ) {
            dontResend.sendToTarget()
        }

        override fun doUpdateVisitedHistory(
            view: WebView, url: String,
            isReload: Boolean
        ) {
            mLuaWebViewClient.doUpdateVisitedHistory(view, url, isReload)
        }

        override fun onReceivedSslError(
            view: WebView, handler: SslErrorHandler,
            error: SslError
        ) {
            mLuaWebViewClient.onReceivedSslError(view, handler, error)
        }

        fun onProceededAfterSslError(view: WebView?, error: SslError?) {
            mLuaWebViewClient.onProceededAfterSslError(view, error)
        }

        fun onReceivedClientCertRequest(
            view: WebView?,
            handler: ClientCertRequest?, hostandport: String?
        ) {
            mLuaWebViewClient.onReceivedClientCertRequest(view, handler, hostandport)
        }

        override fun onReceivedHttpAuthRequest(
            view: WebView,
            handler: HttpAuthHandler, host: String, realm: String
        ) {
            mLuaWebViewClient.onReceivedHttpAuthRequest(view, handler, host, realm)
        }

        override fun shouldOverrideKeyEvent(view: WebView, event: KeyEvent): Boolean {
            return mLuaWebViewClient.shouldOverrideKeyEvent(view, event)
        }

        override fun onUnhandledKeyEvent(view: WebView, event: KeyEvent) {
            mLuaWebViewClient.onUnhandledKeyEvent(view, event)
        }

        override fun onScaleChanged(view: WebView, oldScale: Float, newScale: Float) {
            mLuaWebViewClient.onScaleChanged(view, oldScale, newScale)
        }

        override fun onReceivedLoginRequest(
            view: WebView, realm: String,
            account: String?, args: String
        ) {
            mLuaWebViewClient.onReceivedLoginRequest(view, realm, account, args)
        }
    }

    internal inner class LuaWebChromeClient : WebChromeClient() {
        private var prompt_input_field = EditText(mContext)
        override fun onJsAlert(
            view: WebView,
            url: String,
            message: String,
            result: JsResult
        ): Boolean {
            AlertDialog.Builder(mContext)
                .setTitle(url)
                .setMessage(message)
                .setPositiveButton(
                    android.R.string.ok
                ) { _, _ -> result.confirm() }
                .setCancelable(false)
                .create()
                .show()
            return true
        }

        override fun onJsConfirm(
            view: WebView, url: String,
            message: String, result: JsResult
        ): Boolean {
            val b = AlertDialog.Builder(mContext)
            b.setTitle(url)
            b.setMessage(message)
            b.setPositiveButton(
                android.R.string.ok
            ) { _, _ -> result.confirm() }
            b.setNegativeButton(
                android.R.string.cancel
            ) { _, _ -> result.cancel() }
            b.setCancelable(false)
            b.create()
            b.show()
            return true
        }

        override fun onJsPrompt(
            view: WebView, url: String, message: String,
            defaultValue: String, result: JsPromptResult
        ): Boolean {
            prompt_input_field.setText(defaultValue)
            val b = AlertDialog.Builder(mContext)
            b.setTitle(url)
            b.setMessage(message)
            b.setView(prompt_input_field)
            b.setPositiveButton(
                android.R.string.ok
            ) { _, _ ->
                val value = prompt_input_field
                    .text.toString()
                result.confirm(value)
            }
            b.setNegativeButton(
                android.R.string.cancel
            ) { _, _ -> result.cancel() }
            b.setOnCancelListener { result.cancel() }
            b.show()
            return true
        }

        override fun onProgressChanged(view: WebView, newProgress: Int) {
            //mContext.setProgressBarVisibility(true);
            //mContext.setProgress(newProgress * 100);
            //mContext.setSecondaryProgress(newProgress * 100);
            if (newProgress == 100) {
                mProgressbar.visibility = GONE
            } else {
                mProgressbar.visibility = VISIBLE
                mProgressbar.progress = newProgress
            }
            super.onProgressChanged(view, newProgress)
        }

        override fun onReceivedTitle(view: WebView, title: String) {
            //mContext.setTitle(title);
            super.onReceivedTitle(view, title)
            if (mOnReceivedTitleListener != null) mOnReceivedTitleListener!!.onReceivedTitle(title)
        }

        override fun onReceivedIcon(view: WebView, icon: Bitmap) {
            // TODO: Implement this method
            //mContext.setIcon(new BitmapDrawable(icon));
            super.onReceivedIcon(view, icon)
            if (mOnReceivedIconListener != null) mOnReceivedIconListener!!.onReceivedIcon(icon)
        }

        override fun getDefaultVideoPoster(): Bitmap? {
            return BitmapFactory.decodeResource(mContext.resources, R.mipmap.ic_launcher)
        }

        // For Android 3.0+
        // For Android < 3.0
        @JvmOverloads
        fun openFileChooser(uploadMsg: ValueCallback<Uri>?, acceptType: String? = "") {
            if (mUploadMessage != null) return
            mUploadMessage = uploadMsg
            openFile(mDir)
        }

        // For Android  > 4.1.1
        fun openFileChooser(uploadMsg: ValueCallback<Uri>?, acceptType: String?, capture: String?) {
            openFileChooser(uploadMsg, acceptType)
        }
    }

    private var mOnReceivedTitleListener: OnReceivedTitleListener? = null
    private var mOnReceivedIconListener: OnReceivedIconListener? = null

    init {
        context.regGc(this)
        mContext = context
        settings.javaScriptEnabled = true
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.displayZoomControls = true
        settings.setSupportZoom(true)
        settings.domStorageEnabled = true
        /*setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });*/settings.mixedContentMode =
            WebSettings.MIXED_CONTENT_ALWAYS_ALLOW //允许混合内容，5.0以上默认禁止了http和https混合内容
        //getSettings().setUseWideViewPort(true);
        //getSettings().setLoadWithOverviewMode(true);
        //getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        addJavascriptInterface(LuaJavaScriptInterface(context), "aslua")
        //requestFocus();
        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (mAdsFilter != null) {
                    try {
                        val ret = mAdsFilter!!.call(url)
                        if (ret != null && ret) return true
                    } catch (e: LuaError) {
                        e.printStackTrace()
                    }
                }
                return if (url.startsWith("http") || url.startsWith("file")) {
                    view.loadUrl(url)
                    true
                } else {
                    try {
                        mContext.startActivityForResult(
                            Intent(Intent.ACTION_VIEW, Uri.parse(url)),
                            0
                        )
                    } catch (e: Exception) {
                        mContext.sendError("LuaWebView", e)
                    }
                    true
                }
            }

            override fun onPageFinished(view: WebView, url: String) {
                if (mfinished != null) {
                    try {
                        mfinished!!.call(url)
                    } catch (e: LuaError) {
                        e.printStackTrace()
                    }
                }
                super.onPageFinished(view, url)
            }

            override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
                if (mAdsFilter != null) {
                    try {
                        val ret = mAdsFilter!!.call(url)
                        if (ret != null && ret) return WebResourceResponse(null, null, null)
                    } catch (e: LuaError) {
                        e.printStackTrace()
                    }
                }
                return null
            }

            override fun onReceivedSslError(
                view: WebView, handler: SslErrorHandler,
                error: SslError
            ) {
                val b = AlertDialog.Builder(mContext)
                b.setTitle("SSL错误")
                b.setMessage(error.toString())
                b.setPositiveButton(
                    android.R.string.ok
                ) { _, _ -> handler.proceed() }
                b.setNegativeButton(
                    android.R.string.cancel
                ) { _, _ -> handler.cancel() }
                b.setCancelable(false)
                b.create()
                b.show()
            }
        }
        dm = context.resources.displayMetrics
        val top = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, dm).toInt()
        mProgressbar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal)
        mProgressbar.layoutParams = LayoutParams(LayoutParams.FILL_PARENT, top, 0, 0)
        addView(mProgressbar)
        webChromeClient = LuaWebChromeClient()
        setDownloadListener(Download())
    }

    fun setOnReceivedTitleListener(listener: OnReceivedTitleListener?) {
        mOnReceivedTitleListener = listener
    }

    fun setOnReceivedIconListener(listener: OnReceivedIconListener?) {
        mOnReceivedIconListener = listener
    }

    interface OnReceivedTitleListener {
        fun onReceivedTitle(string: String?)
    }

    interface OnReceivedIconListener {
        fun onReceivedIcon(bitmap: Bitmap?)
    }

    companion object {
        private const val DOWNLOAD = "Download"
    }
}
