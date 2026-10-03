package com.grandl.rankwidget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.jsoup.Jsoup
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.time.LocalDate
import java.util.Locale

data class RankData(val tier:String,val division:String,val lp:Int,val wins:Int,val losses:Int,val wr:Int)

object RankRepository {
 const val DEFAULT_PROFILE_URL="https://op.gg/tr/lol/summoners/tr/grandl-wave"
 private val rankRe=Regex("(Iron|Bronze|Silver|Gold|Platinum|Emerald|Diamond|Master|Grandmaster|Challenger)\\s*([1-4IVX]*)\\s+(\\d+)\\s+LP",RegexOption.IGNORE_CASE)
 private val wlTr=Regex("(\\d+)G\\s+(\\d+)M\\s+Kazanma oranı\\s+(\\d+)%",RegexOption.IGNORE_CASE)
 private val wlEn=Regex("(\\d+)W\\s+(\\d+)L\\s+Win rate\\s+(\\d+)%",RegexOption.IGNORE_CASE)

 fun profileUrl(c:Context)=c.getSharedPreferences("settings",Context.MODE_PRIVATE).getString("profileUrl",DEFAULT_PROFILE_URL)?:DEFAULT_PROFILE_URL

 fun normalizeProfileUrl(raw:String):String {
  val s=raw.trim().removeSuffix("/")
  require(s.startsWith("https://op.gg/",true)||s.startsWith("https://www.op.gg/",true)){"Geçerli bir OP.GG profil linki gir"}
  require(Regex("/lol/summoners/[^/]+/[^/?#]+",RegexOption.IGNORE_CASE).containsMatchIn(s)){"OP.GG oyuncu profil linki tanınmadı"}
  return s.replace("https://www.op.gg/","https://op.gg/",true)
 }

 fun saveProfile(c:Context,raw:String):String {
  val url=normalizeProfileUrl(raw)
  c.getSharedPreferences("settings",Context.MODE_PRIVATE).edit().putString("profileUrl",url).apply()
  c.getSharedPreferences("rank",Context.MODE_PRIVATE).edit().clear().apply()
  return url
 }

 fun displayName(url:String):String {
  val slug=url.substringAfterLast('/').substringBefore('?')
  val decoded=java.net.URLDecoder.decode(slug,"UTF-8")
  val cut=decoded.lastIndexOf('-')
  return if(cut>0) decoded.substring(0,cut)+"#"+decoded.substring(cut+1) else decoded
 }
 fun region(url:String):String=Regex("/summoners/([^/]+)/",RegexOption.IGNORE_CASE).find(url)?.groupValues?.get(1)?.uppercase(Locale.ROOT)?:""

 fun fetch(c:Context):RankData=fetch(profileUrl(c))
 fun fetch(profileUrl:String):RankData{
  val doc=Jsoup.connect(profileUrl).userAgent("Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120 Mobile Safari/537.36").timeout(20000).get()
  val text=doc.body().wholeText().replace(Regex("\\s+")," ")
  val solo=when {
   text.contains("Dereceli Tek/Çift",true)->text.substringAfter("Dereceli Tek/Çift","").substringBefore("Dereceli Esnek")
   text.contains("Ranked Solo/Duo",true)->text.substringAfter("Ranked Solo/Duo","").substringBefore("Ranked Flex")
   else->text
  }
  val r=rankRe.find(solo)?:rankRe.find(text)?:error("Solo/Duo rank bulunamadı")
  val w=wlTr.find(solo)?:wlEn.find(solo)?:wlTr.find(text)?:wlEn.find(text)?:error("Solo/Duo W/L bulunamadı")
  return RankData(r.groupValues[1].lowercase().replaceFirstChar{it.uppercase()},norm(r.groupValues[2]),r.groupValues[3].toInt(),w.groupValues[1].toInt(),w.groupValues[2].toInt(),w.groupValues[3].toInt())
 }
 private fun norm(s:String)=when(s.uppercase()){"I","1"->"1";"II","2"->"2";"III","3"->"3";"IV","4"->"4";else->s}

 fun emblemUrl(tier:String)="https://opgg-static.akamaized.net/images/medals_new/${tier.lowercase()}.png?image=q_auto:good,f_png,w_288"
 fun fetchEmblem(tier:String):Bitmap?=try{URL(emblemUrl(tier)).openConnection().run{connectTimeout=15000;readTimeout=15000;getInputStream().use{BitmapFactory.decodeStream(it)}}}catch(_:Exception){null}
 fun cacheEmblem(c:Context,tier:String):Bitmap?{
  val bmp=fetchEmblem(tier)?:return cachedEmblem(c,tier)
  return try{val f=File(c.filesDir,"rank_${tier.lowercase()}.png");FileOutputStream(f).use{bmp.compress(Bitmap.CompressFormat.PNG,100,it)};bmp}catch(_:Exception){bmp}
 }
 fun cachedEmblem(c:Context,tier:String):Bitmap?=try{val f=File(c.filesDir,"rank_${tier.lowercase()}.png");if(f.exists())BitmapFactory.decodeFile(f.absolutePath)else null}catch(_:Exception){null}

 fun saveAndDailyDelta(c:Context,d:RankData):Int{
  val p=c.getSharedPreferences("rank",Context.MODE_PRIVATE);val today=LocalDate.now().toString();val now=score(d)
  if(p.getString("day",null)!=today)p.edit().putString("day",today).putInt("baseScore",now).apply()
  val delta=now-p.getInt("baseScore",now)
  p.edit().putString("tier",d.tier).putString("div",d.division).putInt("lp",d.lp).putInt("wins",d.wins).putInt("losses",d.losses).putInt("wr",d.wr).putInt("delta",delta).putLong("updated",System.currentTimeMillis()).apply();return delta
 }
 private fun score(d:RankData):Int{
  val tiers=listOf("Iron","Bronze","Silver","Gold","Platinum","Emerald","Diamond");val ti=tiers.indexOfFirst{it.equals(d.tier,true)}
  if(ti>=0){val div=d.division.toIntOrNull()?.coerceIn(1,4)?:4;return ti*400+(4-div)*100+d.lp}
  return 10000+when(d.tier.lowercase()){"master"->0;"grandmaster"->2000;"challenger"->4000;else->0}+d.lp
 }
}
