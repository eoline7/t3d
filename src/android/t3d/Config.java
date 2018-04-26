package android.t3d;

import java.util.Arrays;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

class Config {
	static SharedPreferences getConfig(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
	static Editor getEditor(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).edit();
	}

	static String sLightName = "light";
	static boolean getLight(Context context) {
		return getConfig(context).getBoolean(sLightName, true);
	}
	static void setLight(Context context, boolean light) {
		getEditor(context).putBoolean(sLightName, light).commit();
	}

	static String sVoiceName = "voice";
	static boolean getVoice(Context context) {
		return getConfig(context).getBoolean(sVoiceName, true);
	}
	static void setVoice(Context context, boolean voice) {
		getEditor(context).putBoolean(sVoiceName, voice).commit();
	}

	static String sScoreName = "Score";
	static int mScoreTop = 10;
	static int[] getScores(Context context) {
		SharedPreferences sp = getConfig(context);
		int[] top = new int[mScoreTop];
		for (int i=0; i<top.length; i++) {
			top[i] = sp.getInt(sScoreName+i, 0);
		}
		return top;
	}
	static boolean addScore(Context context, int score) {
		int[] top = getScores(context);
		int last = top.length - 1;
		if (score > top[last]) {
			top[last] = score;
			Arrays.sort(top);
			Editor edit = getEditor(context);
			for (int i=0; i<top.length; i++) {
				edit.putInt(sScoreName+i, top[last-i]);
			}
			edit.commit();
			return true;
		}
		return false;
	}
	static int getScore(Context context, int index) {
		SharedPreferences sp = getConfig(context);
		return sp.getInt(sScoreName+index, 0);
	}
	static int getScore(Context context) {
		SharedPreferences sp = getConfig(context);
		return sp.getInt(sScoreName, 0);
	}
	static void setScore(Context context, int score) {
		getEditor(context).putInt(sScoreName, score).commit();
	}
}
