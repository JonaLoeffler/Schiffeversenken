package com.jona.utility;

import android.os.Message;
import android.util.Log;

public class Util {

	public static final String TAG = "Utility";

	public static int getX(int positionID) {
		return positionID % 10;
	}

	public static int getY(int positionID) {
		return positionID / 10;
	}

	public static int getPositionID(int y, int x) {
		return (y * 10) + x;
	}

	public static void printArray(int[][] array) {
		for (int i = 0; i < 10; i++) {
			Log.d(TAG, array[0][i] + " " + array[1][i] + " " + array[2][i] + " " + array[3][i] + " " + array[4][i] + " "
					+ array[5][i] + " " + array[6][i] + " " + array[7][i] + " " + array[8][i] + " " + array[9][i]);

		}
	}

	/**
	 * expects strings that are to be parsed to a Message object
	 * <"what;arg1;arg2">
	 */
	public static Message stringToMessage(String string) {
		Message msg = new Message();
		int first;
		int second;
		first = string.indexOf(";");
		second = string.indexOf(";", first + 1);
		// third = string.indexOf(";", second + 1);

		Log.d(TAG, first + " " + second + ", " + string.length());

		String stringWhat = (String) string.subSequence(0, first);
		String stringArg1 = (String) string.subSequence(first, second);
		String stringArg2 = (String) string.subSequence(second, string.length());

		stringWhat = stringWhat.replace(";", "");
		stringArg1 = stringArg1.replace(";", "");
		stringArg2 = stringArg2.replace(";", "");

		Log.d(TAG, stringWhat + " " + stringArg1 + " " + stringArg2);

		if (stringWhat.equals(""))
			msg.what = 0;
		else
			msg.what = Integer.parseInt(stringWhat);
		if (stringArg1.equals(""))
			msg.arg1 = 0;
		else
			msg.arg1 = Integer.parseInt(stringArg1);
		if (stringArg2.equals(""))
			msg.arg2 = 0;
		else
			msg.arg2 = Integer.parseInt(stringArg2);

		return msg;
	}

	/**
	 * converts a message object to a string formatted: <"what;arg1;arg2">
	 */
	public static String messageToString(Message msg) {
		return msg.what + ";" + msg.arg1 + ";" + msg.arg2 + "#";
	}

	/**
	 * builds a message from integers, overloaded
	 */
	public static Message getMessage(int what) {
		Message msg = new Message();
		msg.what = what;
		return msg;
	}

	public static Message getMessage(int what, int arg1) {
		Message msg = new Message();
		msg.what = what;
		msg.arg1 = arg1;
		return msg;
	}

	public static Message getMessage(int what, int arg1, int arg2) {
		Message msg = new Message();
		msg.what = what;
		msg.arg1 = arg1;
		msg.arg2 = arg2;
		return msg;
	}

	public static Message getMessage(int what, int arg1, int arg2, int obj) {
		Message msg = new Message();
		msg.what = what;
		msg.arg1 = arg1;
		msg.arg2 = arg2;
		msg.obj = obj;
		return msg;
	}
}
