package me.star.geektime.geektimehelper

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import me.star.geektime.geektimehelper.utils.listToString

class GeekTimeAccessibilityService : AccessibilityService() {
    val TAG = "GeekTimeService"
    private var mHandler: Handler = Handler(Looper.getMainLooper())

    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        dispatchEvent(event, rootInActiveWindow)
    }

    private fun dispatchEvent(event: AccessibilityEvent?, rootInActiveWindow: AccessibilityNodeInfo?) {
        val pkgName = event?.packageName.toString()
        val eventType = event?.eventType
        val eventText = listToString(event?.text)
        Log.i(TAG, "pkgName:$pkgName     eventType:$eventType      className:${event?.className.toString()}      " +
                "event.text:$eventText event?.getContentChangeTypes():${event?.contentChangeTypes}")
        when (eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED, AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {//32 2048
                val className = event.className.toString()
                if (className == "org.geekbang.geekTime.view.activity.MainActivity") {
                    clickMeButton()
                } else if (className == "android.support.v7.widget.RecyclerView" || className == "android.view.View") {
//                    goPresentActivity()
                } else if (className == "org.geekbang.geekTime.view.activity.PresentActivity") {
                    Log.e(TAG, "org.geekbang.geekTime.view.activity.PresentActivity invoked!")
                    Thread.sleep(200)
                    val titleList: List<AccessibilityNodeInfo> = rootInActiveWindow?.findAccessibilityNodeInfosByText("已购") ?: arrayListOf()
                    val list: List<AccessibilityNodeInfo>? = rootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/content")
                    val downloadList: List<AccessibilityNodeInfo> = rootInActiveWindow?.findAccessibilityNodeInfosByText("下载") ?: arrayListOf()
                    if (titleList.isNotEmpty()) {
                        parsePresentActivity(list)
                    } else if (downloadList.isNotEmpty()) {
                        Log.e(TAG, "allSelectClicked() invoked!")
                        allSelectClicked()
                    } else {
                        parsePresentActivityDetail()
                    }
                }
            }
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                if (eventText == "我的\t") {
                    goPresentActivity()
                } else if (eventText == "" && event.className.toString() == "android.widget.FrameLayout") {//标识收到听音频的点击事件，可以执行下一步的逻辑
                    goPatchDownloadPage()
                }
            }
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                if (event.className.toString() == "android.support.v7.widget.RecyclerView") {
                    Log.e(TAG, "RecyclerView scrolled invoked!")
                }
            }

        }
        rootInActiveWindow?.recycle()
    }

    private fun clickMeButton() {
        mHandler.postDelayed({
            Log.e(TAG, "clickMeButton invoked!")
            val list: List<AccessibilityNodeInfo> = rootInActiveWindow?.findAccessibilityNodeInfosByText("我的") ?: arrayListOf()
            if (list.isNotEmpty()) {
                val nodeInfo: AccessibilityNodeInfo  = list[0].parent
                if (nodeInfo.className == "android.widget.RelativeLayout") {
                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
            }
        }, 10000)
    }

    private fun goPresentActivity() {
        mHandler.postDelayed({
            var list: List<AccessibilityNodeInfo> = rootInActiveWindow?.findAccessibilityNodeInfosByText("已购") ?: arrayListOf()
            Log.e(TAG, "lgoPresentActivity() list.isNotEmpty() == " + list.isNotEmpty())
            if (list.isNotEmpty()) {
                Log.e(TAG, "list?.isNotEmpty() == true")
                val nodeInfo: AccessibilityNodeInfo  = list[0].parent
                if (nodeInfo.className == "android.widget.FrameLayout") {
                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
            }
        }, 3000)
    }

    //解析已购产品页面,activity为PresentActivity
    private fun parsePresentActivity(list: List<AccessibilityNodeInfo>?) {
        Log.e(TAG, "刚进入PresentActivity，title为已购")
        // 由于是刚进入PresentActivity页面就执行这个方法，recyclerView可能还未渲染出来,所以逻辑都移动到postDelayed()中
        // 找到recyclerview 然后遍历每一个itemview，这是课程列表
        if (list?.isEmpty() == true) {
            return
        }
        Log.e(TAG, "刚进入PresentActivity，title为已购 111")
        mHandler.postDelayed({
            val contentList = rootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/content")
            val contentNodeInfo: AccessibilityNodeInfo = contentList?.get(0)!!
            val recyclerViewNodeInfo: AccessibilityNodeInfo = findRecyclerViewNodeInfo(contentNodeInfo)
                    ?: return@postDelayed
            //选择第二条点击,应该维护一个列表保存全部的课程，目前只先一门课程的处理
            //child(0) 感觉没用
            Log.e(TAG, "刚进入PresentActivity，title为已购 222")
            val frameLayoutNodeInfo = recyclerViewNodeInfo.getChild(1)?.getChild(0)
            frameLayoutNodeInfo?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }, 6000)
    }

    //选中PresentActivity的听音频按钮
    private fun parsePresentActivityDetail() {
        mHandler.postDelayed({
            Log.e(TAG, "刚进入PresentActivity，title为图片，显示的是具体课程的课程详细列表")
            val list: List<AccessibilityNodeInfo> = rootInActiveWindow?.findAccessibilityNodeInfosByText("听音频") ?: arrayListOf()
            if (list.isNotEmpty() && list[0].parent != null) {
                val nodeInfo: AccessibilityNodeInfo  = list[0].parent
                if (nodeInfo.className == "android.widget.FrameLayout") {
                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
            }
        }, 3000)
    }

    private fun goPatchDownloadPage() {
        val fileList: List<AccessibilityNodeInfo> = rootInActiveWindow?.findAccessibilityNodeInfosByText("看图文") ?: arrayListOf()
        val audioList: List<AccessibilityNodeInfo> = rootInActiveWindow?.findAccessibilityNodeInfosByText("听音频") ?: arrayListOf()
        if (fileList.isNotEmpty() && audioList.isNotEmpty()) {
            val downloadList: List<AccessibilityNodeInfo> = rootInActiveWindow?.findAccessibilityNodeInfosByText("批量下载") ?: arrayListOf()
            mHandler.postDelayed({
                if (downloadList[0].parent != null) {
                    val nodeInfo: AccessibilityNodeInfo  = downloadList[0].parent
                    if (nodeInfo.className == "android.widget.FrameLayout") {
                        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }
                }
            }, 3000)
        }
    }

    private fun scrollToBottom() {
        mHandler.postDelayed({
            Log.e(TAG, "scrollToBottom()")
            val contentList = rootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/content") ?: arrayListOf()
            val recyclerViewNodeInfo: AccessibilityNodeInfo = findRecyclerViewNodeInfo(contentList[0]) ?: return@postDelayed
            Log.e(TAG, "recyclerViewNodeInfo.childCount == " + recyclerViewNodeInfo.childCount)
            printNode(recyclerViewNodeInfo)
            scrollView(recyclerViewNodeInfo)
            Thread.sleep(1000)
            checkScroll()
        }, 3000)
    }

    private fun scrollView(nodeInfo: AccessibilityNodeInfo?): Boolean {
        if (nodeInfo == null) return false
        if (nodeInfo.isScrollable) {
            return nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
        }
        for (index in 0..nodeInfo.childCount) {
            if (scrollView(nodeInfo.getChild(index))) {
                return true
            }
        }
        return false
    }

    private fun checkScroll() {
        mHandler.postDelayed({
            Log.e(TAG, "checkScroll()")
            val list: List<AccessibilityNodeInfo> = rootInActiveWindow?.findAccessibilityNodeInfosByText("全选") ?: arrayListOf()
            val allSelectNodeInfo: AccessibilityNodeInfo? = list.findLast { it.contentDescription.toString() == "全选" }
            val checkBoxNodeInfo: AccessibilityNodeInfo? = allSelectNodeInfo?.parent?.getChild(4)
            Log.e(TAG, "checkBoxNodeInfo, childCount == " + checkBoxNodeInfo?.childCount + ", toString==" + checkBoxNodeInfo.toString())
//            if (allSelectNodeInfo?.isChecked == false) {
//            printNode(allSelectNodeInfo?.parent)
            if (checkBoxNodeInfo != null && checkBoxNodeInfo.className == "android.widget.FrameLayout" && checkBoxNodeInfo.childCount == 0) {
                allSelectClicked()
            } else if (checkBoxNodeInfo != null && checkBoxNodeInfo.className == "android.widget.FrameLayout" && checkBoxNodeInfo.childCount == 1) {
//                接着执行全部下载的操作
                downloadInternal()
            }
        }, 10000)
    }

    private fun printNode(rootNodeInfo: AccessibilityNodeInfo?) {
        if (rootNodeInfo == null) {
            return
        }
        for(index in 0..rootNodeInfo.childCount) {
            Log.e(TAG, "printNode(), rootNodeInfo($index) == ${rootNodeInfo.getChild(index)}")
        }
    }

    //先全选，然后在进行滑动
    private fun allSelectClicked() {
        mHandler.postDelayed({
            Log.e(TAG, "allSelectClicked()")
            val list: List<AccessibilityNodeInfo> = rootInActiveWindow?.findAccessibilityNodeInfosByText("全选") ?: arrayListOf()
            val allSelectNodeInfo: AccessibilityNodeInfo? = list.findLast { it.contentDescription.toString() == "全选" }
            allSelectNodeInfo?.let { allSelectNodeInfo ->
                Log.e(TAG, "downloadStep1() , allSelectNodeInfo.isChecked == " + allSelectNodeInfo.isChecked)
                if (allSelectNodeInfo.className == "android.view.View") {
                    allSelectNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
                Thread.sleep(300)
            }
            scrollToBottom()
        }, 3000)
    }

    private fun downloadInternal() {
        mHandler.postDelayed({
            Log.e(TAG, "downloadInternal()")
            val list: List<AccessibilityNodeInfo> = rootInActiveWindow?.findAccessibilityNodeInfosByText("下载") ?: arrayListOf()
            val downloadNodeInfo: AccessibilityNodeInfo? = list.findLast { it.contentDescription.toString() == "下载" }
            downloadNodeInfo?.also {
                if (downloadNodeInfo.className == "android.view.View") {
                    downloadNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
            }
        }, 3000)
    }



    private fun findRecyclerViewNodeInfo(rootNodeInfo: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (rootNodeInfo.className == "android.support.v7.widget.RecyclerView") {
            return rootNodeInfo
        }
        if (rootNodeInfo.childCount == 0) {
            return null
        }
        for (index in 0 until rootNodeInfo.childCount) {
            val childNodeInfo = rootNodeInfo.getChild(index)
//            Log.e(TAG, "childNodeInfo == " + childNodeInfo + ", index == " + index)
            if (childNodeInfo.className == "android.support.v7.widget.RecyclerView") {
                return childNodeInfo
            } else {
                if (childNodeInfo.childCount != 0) {
                    val recyclerViewNodeInfo = findRecyclerViewNodeInfo(childNodeInfo)
                    if (recyclerViewNodeInfo != null) {
                        return recyclerViewNodeInfo
                    }
                }
            }
        }
        return null
    }
}