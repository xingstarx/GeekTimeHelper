package me.star.geektime.geektimehelper

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import me.star.geektime.geektimehelper.utils.listToString

class GeekTimeAccessibilityService : AccessibilityService() {
    val TAG = "GeekTimeAccessibilityService"
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
                    val list: List<AccessibilityNodeInfo>? = rootInActiveWindow?.findAccessibilityNodeInfosByText("我的")
                    if (list?.get(0)?.parent != null) {
                        val nodeInfo: AccessibilityNodeInfo  = list[0].parent
                        if (nodeInfo.className == "android.widget.RelativeLayout") {
                            mHandler.postDelayed({
                                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            }, 10000)
                        }
                    }
                } else if (className == "android.support.v7.widget.RecyclerView" || className == "android.view.View") {
//                    goPresentActivity()
                } else if (className == "org.geekbang.geekTime.view.activity.PresentActivity") {
                    val titleList: List<AccessibilityNodeInfo> = rootInActiveWindow?.findAccessibilityNodeInfosByText("已购") ?: arrayListOf()
                    val list: List<AccessibilityNodeInfo>? = rootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/content")
                    val downloadList: List<AccessibilityNodeInfo> = rootInActiveWindow?.findAccessibilityNodeInfosByText("下载") ?: arrayListOf()
                    if (titleList.isNotEmpty()) {
                        parsePresentActivity(list)
                    } else if (downloadList.isNotEmpty()) {
//                        downloadStep1()
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

        }
        rootInActiveWindow?.recycle()
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
        }, 10000)
    }

    //选中PresentActivity的听音频按钮
    private fun parsePresentActivityDetail() {
        Log.e(TAG, "刚进入PresentActivity，title为图片，显示的是具体课程的课程详细列表")
        mHandler.postDelayed({
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

    private fun downloadStep11() {
        mHandler.postDelayed({
            val contentList = rootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/content") ?: arrayListOf()
            val recyclerViewNodeInfo: AccessibilityNodeInfo = findRecyclerViewNodeInfo(contentList[0]) ?: return@postDelayed
            recyclerViewNodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
            Thread.sleep(300)
        }, 3000)
    }

    private fun downloadStep1() {
        mHandler.postDelayed({
            val list: List<AccessibilityNodeInfo> = rootInActiveWindow?.findAccessibilityNodeInfosByText("全选") ?: arrayListOf()
            val allSelectNodeInfo: AccessibilityNodeInfo? = list.findLast { it.contentDescription.toString() == "全选" }
            allSelectNodeInfo?.also {
                if (allSelectNodeInfo.className == "android.view.View") {
                    allSelectNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
            }
            //接着执行全部下载的操作
            downloadStep2()
        }, 3000)
    }

    private fun downloadStep2() {
        mHandler.postDelayed({
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
            Log.e(TAG, "childNodeInfo == " + childNodeInfo + ", index == " + index)
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