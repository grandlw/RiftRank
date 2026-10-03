package com.grandl.rankwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews

class RankWidgetProvider : AppWidgetProvider() {
 override fun onUpdate(context:Context,manager:AppWidgetManager,ids:IntArray){ids.forEach{update(context,manager,it)}}
 override fun onAppWidgetOptionsChanged(context:Context,manager:AppWidgetManager,id:Int,options:Bundle){update(context,manager,id)}
 companion object{
  fun updateAll(context:Context){val m=AppWidgetManager.getInstance(context);val c=ComponentName(context,RankWidgetProvider::class.java);m.getAppWidgetIds(c).forEach{update(context,m,it)}}
  private fun backgroundFor(t:String)=when(t.lowercase()){"gold"->R.drawable.widget_gold;"platinum"->R.drawable.widget_platinum;"emerald"->R.drawable.widget_emerald;"diamond"->R.drawable.widget_diamond;"master"->R.drawable.widget_master;"grandmaster"->R.drawable.widget_grandmaster;"challenger"->R.drawable.widget_challenger;else->R.drawable.widget_neutral}
  private fun update(c:Context,m:AppWidgetManager,id:Int){
   val p=c.getSharedPreferences("rank",Context.MODE_PRIVATE);val tier=p.getString("tier","Platinum")?:"Platinum";val d=p.getInt("delta",0);val v=RemoteViews(c.packageName,R.layout.rank_widget)
   v.setInt(R.id.widgetRoot,"setBackgroundResource",backgroundFor(tier))
   val badge=RankRepository.cachedEmblem(c,tier)
   if(badge!=null){v.setViewVisibility(R.id.wBadge,View.VISIBLE);v.setImageViewBitmap(R.id.wBadge,badge)}else{v.setViewVisibility(R.id.wBadge,View.INVISIBLE)}
   v.setTextViewText(R.id.wRank,"$tier ${p.getString("div","1")}");v.setTextViewText(R.id.wLp,"${p.getInt("lp",30)} LP");v.setTextViewText(R.id.wRecord,"${p.getInt("wins",68)}W • ${p.getInt("losses",55)}L • %${p.getInt("wr",55)}");v.setTextViewText(R.id.wDelta,"Bugün ${if(d>=0) "+" else ""}$d LP")
   m.updateAppWidget(id,v)
  }
 }
}
