package com.generativimt12.myring;
import android.content.*;import android.net.Uri;import android.os.*;import android.provider.MediaStore;import java.io.*;
public final class SilentRingtone{
 private static final String NAME="Myring Silent Ringtone.wav";
 public static Uri ensure(Context c)throws Exception{ContentResolver cr=c.getContentResolver();String[] p={MediaStore.Audio.Media._ID};String sel=MediaStore.Audio.Media.DISPLAY_NAME+"=?";String[] args={NAME};
 try(android.database.Cursor cur=cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,p,sel,args,null)){if(cur!=null&&cur.moveToFirst())return Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,cur.getString(0));}
 ContentValues v=new ContentValues();v.put(MediaStore.Audio.Media.DISPLAY_NAME,NAME);v.put(MediaStore.Audio.Media.MIME_TYPE,"audio/wav");v.put(MediaStore.Audio.Media.IS_RINGTONE,1);
 if(Build.VERSION.SDK_INT>=29)v.put(MediaStore.Audio.Media.RELATIVE_PATH,Environment.DIRECTORY_RINGTONES);else{File d=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES);d.mkdirs();v.put(MediaStore.Audio.Media.DATA,new File(d,NAME).getAbsolutePath());}
 Uri u=cr.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,v);if(u==null)throw new IOException("MediaStore insert failed");try(OutputStream out=cr.openOutputStream(u)){writeSilentWav(out,1);}return u;}
 private static void writeSilentWav(OutputStream o,int seconds)throws Exception{int rate=8000,ch=1,bps=16;int data=rate*seconds*ch*2;ByteArrayOutputStream h=new ByteArrayOutputStream();DataOutputStream d=new DataOutputStream(h);d.writeBytes("RIFF");le(d,36+data);d.writeBytes("WAVEfmt ");le(d,16);les(d,(short)1);les(d,(short)ch);le(d,rate);le(d,rate*ch*2);les(d,(short)(ch*2));les(d,(short)bps);d.writeBytes("data");le(d,data);d.flush();o.write(h.toByteArray());byte[] z=new byte[4096];int n=data;while(n>0){int q=Math.min(n,z.length);o.write(z,0,q);n-=q;}o.flush();}
 private static void le(DataOutputStream d,int x)throws Exception{d.writeByte(x);d.writeByte(x>>8);d.writeByte(x>>16);d.writeByte(x>>24);}private static void les(DataOutputStream d,short x)throws Exception{d.writeByte(x);d.writeByte(x>>8);}
}