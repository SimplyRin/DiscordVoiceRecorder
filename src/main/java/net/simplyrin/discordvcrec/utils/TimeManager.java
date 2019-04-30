package net.simplyrin.discordvcrec.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import lombok.Getter;
import net.simplyrin.discordvcrec.Main;

/**
 * Created by SimplyRin on 2019/04/04.
 *
 * Copyright (c) 2019 SimplyRin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class TimeManager {

	private Main instance;
	private String manageId;

	private static HashMap<String, TimeManager> maps = new HashMap<>();

	public TimeManager(Main instance) {
		this.instance = instance;
	}

	public TimeManager(Main instance, String manageId) {
		this.instance = instance;
		this.manageId = manageId;
	}

	public TimeManager getManager(String manageId) {
		if (maps.get(manageId) == null) {
			maps.put(manageId, new TimeManager(this.instance, manageId));
		}
		return maps.get(manageId);
	}


	private String userId;
	private Date date;

	@Getter
	private boolean isJoined;

	public void joined() {
		this.date = new Date();
		this.isJoined = true;
	}

	public void quit() {
		this.date = null;
		this.isJoined = false;
		maps.put(this.userId, null);
	}

	public String getJoinedTime() {
		return this.date.getHours() + "h" + this.date.getMinutes() + "m";
	}

	public String getCurrentTime() {
		return this.getUptime(this.date);
	}

	public String getUptime(Date createdTime) {
		/**
		 * https://teratail.com/questions/28238
		 */
		int[] units = { Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND };
		Date today = new Date();
		int[] result = new int[units.length];

		Calendar sCal = Calendar.getInstance();
		Calendar tCal = Calendar.getInstance();

		sCal.setTime(createdTime);
		tCal.setTime(today);

		for (int i = units.length - 1; i >= 0; i--) {
			result[i] = tCal.get(units[i]) - sCal.get(units[i]);
			if (result[i] < 0) {
				tCal.add(units[i - 1], -1);
				int add = tCal.getActualMaximum(units[i]);
				result[i] += (units[i] == Calendar.DAY_OF_MONTH) ? add : add + 1;
			}
		}

		String uptime = "";
		int year = result[0];
		int month = result[1];
		int day = result[2];
		int hour = result[3];
		int minute = result[4];
		int second = result[5];

		if (year > 0) {
			uptime += year + "y";
		}
		if (month > 0) {
			uptime += month + "m";
		}
		if (day > 0) {
			uptime += day + "d ";
		}

		if (hour > 0) {
			uptime += hour + "h";
		}
		if (minute > 0) {
			uptime += minute + "m";
		}
		if (second > 0) {
			uptime += second + "s";
		}

		return uptime;
	}

	public void close() {
		maps.put(this.manageId, null);
	}

}
