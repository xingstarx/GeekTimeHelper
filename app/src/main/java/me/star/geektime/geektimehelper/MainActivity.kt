package me.star.geektime.geektimehelper

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import me.star.geektime.geektimehelper.utils.isAccessibilityServiceOn


class MainActivity : AppCompatActivity() {
    val targetPackageName = "org.geekbang.geekTime"
    var num: Int = 3
    private var isOnResume: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button?.also {
            val isOn = isAccessibilityServiceOn()
            it.text = if (isOn) "已经开启" else "点击开启"
            it.isEnabled = !isOn
            it.setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isOnResume = true
        val isOn = isAccessibilityServiceOn()
        if (isOn) {
            if (!isInstallApp(this, targetPackageName)) {
                return
            }
            numView.text = "$num 秒后进入极客时间App"
            numView.postDelayed({
                goGeekTimeApp()
            }, 1000)
        }
    }

    override fun onPause() {
        super.onPause()
        isOnResume = false
    }

    private fun goGeekTimeApp() {
        if (isFinishing) {
            return
        }
        if (!isOnResume) {
            return
        }
        num--
        numView.text = "$num 秒后进入极客时间App"
        if (num == 0) {
            num = 3
            openGeekTimeApp()
        } else {
            numView.postDelayed({
                goGeekTimeApp()
            }, 1000)
        }
    }

    private fun openGeekTimeApp() {
        val intent = packageManager.getLaunchIntentForPackage(targetPackageName)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun isInstallApp(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }

    }

}
