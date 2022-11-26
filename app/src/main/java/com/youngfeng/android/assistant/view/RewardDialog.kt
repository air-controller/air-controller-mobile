package com.youngfeng.android.assistant.view

import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.bumptech.glide.Glide
import com.youngfeng.android.assistant.R
import com.youngfeng.android.assistant.util.PathHelper
import java.io.File

class RewardDialog(context: Context) : Dialog(context) {
    private val mAlipayImage by lazy { findViewById<ImageView>(R.id.image_alipay) }
    private val mWechatPayImage by lazy { findViewById<ImageView>(R.id.image_wechat_pay) }
    private val mAlipayBtn by lazy { findViewById<AppCompatButton>(R.id.btn_alipay) }
    private val mWechatPayBtn by lazy { findViewById<AppCompatButton>(R.id.btn_wechat_pay) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_reward)

        initView()
        initListener()
    }

    private fun initView() {
        Glide.with(context).load(Uri.parse("file:///android_asset/images/alipay.png")).into(mAlipayImage)
        Glide.with(context).load(Uri.parse("file:///android_asset/images/wechat_pay.png")).into(mWechatPayImage)
    }

    private fun initListener() {
        mAlipayBtn.setOnClickListener {
            saveAssetImageToGallery("images/alipay.png", "Alipay.png")
        }
        mWechatPayBtn.setOnClickListener {
            saveAssetImageToGallery("images/wechat_pay.png", "WechatPay.png")
        }
    }

    private fun saveAssetImageToGallery(path: String, fileName: String) {
        context.assets.open(path).use { inputStream ->
            val file = File(PathHelper.photoRootDir(), fileName)
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            val values = ContentValues()

            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            values.put(MediaStore.MediaColumns.DATA, file.absolutePath)

            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

            Toast.makeText(context, R.string.qr_code_saved, Toast.LENGTH_SHORT).show()
        }
    }
}
