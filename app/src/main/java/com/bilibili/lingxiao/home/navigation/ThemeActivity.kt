package com.bilibili.lingxiao.home.navigation

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.bilibili.lingxiao.R
import com.bilibili.lingxiao.widget.RippleAnimation
import com.camera.lingxiao.common.Common
import com.camera.lingxiao.common.app.BaseActivity
import com.camera.lingxiao.common.rxbus.SkinChangedEvent
import com.camera.lingxiao.common.utills.SpUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.github.zackratos.ultimatebar.UltimateBar
import com.lingxiao.skinlibrary.SkinLib
import kotlinx.android.synthetic.main.activity_theme.*
import kotlinx.android.synthetic.main.title_bar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ThemeActivity : BaseActivity() {
    private var themeList = arrayListOf<ThemeData>()
    override val contentLayoutId: Int
        get() = R.layout.activity_theme

    override fun initWidget() {
        super.initWidget()
        var names = resources.getStringArray(R.array.theme_name)
        var colors = resources.getIntArray(R.array.theme_color)
        var tags = resources.getStringArray(R.array.theme_tag)
        var checkedColor = SpUtils.getInt(this@ThemeActivity, Common.SKIN_ID,0)
        for ((index,name) in names.withIndex()){
            if (index < 3){
                themeList.add(ThemeData(colors[index],name,tags[index],false,false))
            }else{
                themeList.add(ThemeData(colors[index],name,tags[index],true,false))
            }
            if (checkedColor == colors[index])
                checkedColor = index
        }

        themeList[checkedColor].choose = true
        var adapter = ThemeAdapter(R.layout.item_theme,themeList)
        recycerView.adapter = adapter
        recycerView.layoutManager = LinearLayoutManager(this)
        setToolbarBack(title_bar)
        title_bar.title = "主题颜色"

        adapter.setOnItemClickListener { adapter, view, position ->
            RippleAnimation.create(view).setDuration(1000).start()
            for ((index,item) in themeList.withIndex()){
                item.choose = false
                if (index == position)
                    item.choose = true
            }
            adapter.notifyDataSetChanged()
            if (position == 0) {
                SkinLib.resetSkin()
            } else {
                SkinLib.loadSkin(tags.get(position))
            }
            SpUtils.putInt(this@ThemeActivity, Common.SKIN_ID,themeList[position].color)
            EventBus.getDefault().postSticky(SkinChangedEvent(themeList[position].color))
        }
    }
    override fun isRegisterEventBus(): Boolean {
        return true
    }

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    fun onSkinChanged(event : SkinChangedEvent){
        UltimateBar.newColorBuilder()
            .statusColor(event.color)   // 状态栏颜色
            .applyNav(true)             // 是否应用到导航栏
            .navColor(event.color)         // 导航栏颜色
            .build(this)
            .apply()

    }

    data class ThemeData(var color:Int,var name:String,var tag:String,var pay:Boolean,var choose:Boolean){

    }

    inner class ThemeAdapter(layoutResId: Int, data: MutableList<ThemeData>?) :
    BaseQuickAdapter<ThemeData,BaseViewHolder>(layoutResId,data){
        override fun convert(helper: BaseViewHolder, item: ThemeData) {
            helper.setBackgroundColor(R.id.image_color,item.color)
            helper.setText(R.id.text_theme_name,item.name)
            if (item.pay){
                helper.setVisible(R.id.text_theme_price,true)
                helper.setVisible(R.id.button_pay,true)
            }else{
                //需要GONE而不是INVISIBLE
                //helper.setVisible(R.id.text_theme_price,false)
                helper.getView<TextView>(R.id.text_theme_price).visibility = View.GONE
                helper.setVisible(R.id.button_pay,false)
            }
            if (item.choose){
                helper.setVisible(R.id.image_choose,true)
            }else{
                helper.setVisible(R.id.image_choose,false)
            }
        }
    }
}
