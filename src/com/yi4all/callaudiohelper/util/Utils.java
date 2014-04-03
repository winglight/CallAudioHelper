package com.yi4all.callaudiohelper.util;

import java.util.ArrayList;
import java.util.List;

import com.yi4all.callaudiohelper.R;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.widget.Toast;

public class Utils {

	public static String stripPhoneNumbers(String numbers){
		if(numbers == null) return numbers;
		if(numbers.startsWith("|") && numbers.endsWith("|")){
			numbers = numbers.substring(1, numbers.length()-1);
		}
		return numbers.replace("|", ",");
	}
	
	public static void call(final Context context, String number) {
		if (number == null)
			return;
		final String[] nums = stripPhoneNumbers(number).split(",");
		if (nums.length == 1) {
			Intent callIntent = new Intent(Intent.ACTION_CALL);
			callIntent.setData(Uri.parse("tel:" + number));
			context.startActivity(callIntent);
		} else {
			AlertDialog.Builder b = new Builder(context);
			b.setTitle(R.string.choose_number);
			b.setItems(nums, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

					dialog.dismiss();
					String num = nums[which];

					Intent callIntent = new Intent(Intent.ACTION_CALL);
					callIntent.setData(Uri.parse("tel:" + num));
					context.startActivity(callIntent);
				}

			});

			b.show();
		}
	}
	
	public static void showToastMessage(final Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}

	public static void showToastMessage(final Context context, int message, Object...obj) {
		Toast.makeText(context, context.getString(message, obj), Toast.LENGTH_LONG).show();
	}
	
	public static <T> List<T> convertSelectedItem(List<T> list, SparseBooleanArray selectedArray){
		List<T> res = new ArrayList<T>();
		for(int i=0; i < selectedArray.size() ; i++){
			res.add(list.get(selectedArray.keyAt(i)));
		}
		return res;
	}
	
	public static void playPCM(byte[] soundByteArray) {
		try {
			// 8000, little endian, 16 bit PCM, mono channel did the trick
			AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
					8000, AudioFormat.CHANNEL_OUT_MONO,
					AudioFormat.ENCODING_PCM_16BIT, 500000,
					AudioTrack.MODE_STATIC);

			audioTrack.write(soundByteArray, 0, soundByteArray.length);
			audioTrack.play();

		} catch (Throwable t) {
			Log.d("Audio", "Playback Failed");
		}
	}
	
	public static byte[] combineByteArray(byte[] one, byte[] two) {
		if(one == null || two == null) return null;
		
		byte[] combined = new byte[one.length + two.length];

		System.arraycopy(one,0,combined,0         ,one.length);
		System.arraycopy(two,0,combined,one.length,two.length);
		
		return combined;
	}
	
	public static boolean hasFroyo() {
        // Can use static final constants like FROYO, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }
}
