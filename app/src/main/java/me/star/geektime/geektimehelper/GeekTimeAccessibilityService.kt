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
        Log.i(TAG, "pkgName:$pkgName     eventType:$eventType      className:${event?.className.toString()}      " +
                "event.text:${listToString(event?.text)} event?.getContentChangeTypes():${event?.contentChangeTypes}")
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
                            }, 3000)
                        }
                    }
                } else if (className == "android.widget.TextView") {
                    val list: List<AccessibilityNodeInfo>? = rootInActiveWindow?.findAccessibilityNodeInfosByText("已购")
                    if (list?.isNotEmpty() == true) {
                        val nodeInfo: AccessibilityNodeInfo  = list[0].parent
                        if (nodeInfo.className == "android.widget.FrameLayout") {
                            mHandler.postDelayed({
                                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            }, 3000)
                        }
                    }
                } else if (className == "org.geekbang.geekTime.view.activity.PresentActivity") {
                    val titleList: List<AccessibilityNodeInfo>? = rootInActiveWindow?.findAccessibilityNodeInfosByText("已购")
                    val list: List<AccessibilityNodeInfo>? = rootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/content")
                    if (titleList?.isNotEmpty() == true) {
                        parsePresentActivity(list)
                    } else {
//                        parsePresentActivityDetail()
                }
                } else if (className == "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI") {
                }
            }
        }
//        if (event?.getContentChangeTypes() == AccessibilityEvent.CONTENT_CHANGE_TYPE_TEXT) {
//            Withdraw().withDraw(event, rootInActiveWindow)//防消息撤回
//        }
        rootInActiveWindow?.recycle()//避免重复创建实例通过recycle方法回收掉nodeInfo（我们自己手动去回收）
    }

    //解析已购产品页面,activity为PresentActivity
    private fun parsePresentActivity(list: List<AccessibilityNodeInfo>?) {
        // 找到recyclerview 然后遍历每一个itemview，这是课程列表
        if (list?.isEmpty() == true) {
            return
        }
        val contentNodeInfo: AccessibilityNodeInfo = list?.get(0)!!
        val recyclerViewNodeInfo: AccessibilityNodeInfo? = findRecyclerViewNodeInfo(contentNodeInfo)
        //选择第二条点击,应该维护一个列表保存全部的课程，目前只先一门课程的处理
        //child(0) 感觉没用
        val frameLayoutNodeInfo = recyclerViewNodeInfo?.getChild(1)?.getChild(0)
        mHandler.postDelayed({
            frameLayoutNodeInfo?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }, 3000)
    }

    private fun findRecyclerViewNodeInfo(rootNodeInfo: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (rootNodeInfo.className == "android.support.v7.widget.RecyclerView") {
            return rootNodeInfo
        }
        for (index in 0 until rootNodeInfo.childCount) {
            val childNodeInfo = rootNodeInfo.getChild(index)
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