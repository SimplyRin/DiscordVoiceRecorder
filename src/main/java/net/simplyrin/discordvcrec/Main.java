package net.simplyrin.discordvcrec;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.Getter;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.md_5.bungee.config.Configuration;
import net.simplyrin.config.Config;
import net.simplyrin.discordvcrec.listeners.MessageListener;
import net.simplyrin.discordvcrec.utils.TimeManager;
import net.simplyrin.rinstream.RinStream;

/**
 * Created by SimplyRin on 2019/04/29.
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
public class Main {

	public static void main(String[] args) {
		new Main().run();
	}

	@Getter
	private Configuration config;
	@Getter
	private JDA jda;

	@Getter
	private File recordFolder;
	@Getter
	private File cacheFolder;
	@Getter
	private File batFolder;

	@Getter
	private TimeManager timeManager;

	public void run() {
		new RinStream();

		System.out.println("読み込み中...");

		File file = new File("config.yml");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

			Configuration config = Config.getConfig(file);
			config.set("Token", "BOT_TOKEN_HERE");
			config.set("PrintFFMpegLog", true);

			Config.saveConfig(config, file);
		}

		this.recordFolder = new File("records");
		this.recordFolder.mkdir();

		this.cacheFolder = new File("caches");
		this.removeFolder(this.cacheFolder);
		this.cacheFolder.mkdir();

		this.batFolder = new File("bats");
		this.removeFolder(this.batFolder);
		this.batFolder.mkdir();

		this.timeManager = new TimeManager(this);

		this.config = Config.getConfig(file);

		String token = this.config.getString("Token");
		if (token.equals("BOT_TOKEN_HERE")) {
			System.out.println("Discord Bot Token を config.yml に入力してください！");
			System.exit(0);
			return;
		}

		JDABuilder jdaBuilder = new JDABuilder(AccountType.BOT);
		jdaBuilder.setToken(token);
		jdaBuilder.addEventListeners(new MessageListener(this));

		try {
			jdaBuilder.build();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("読み込み完了！");

		System.out.println("ffmpeg.exe ダウンロード: http://ffmpeg.org/download.html");
	}

	public String getTimeStamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
		return sdf.format(new Date());
	}

	/**
	 * Code from ns777.
	 * URL: https://qiita.com/ns777/items/0e959a9c35753b178003
	 */
	private void removeFolder(File file) {
		if (!file.exists()) {
			return;
		}
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				this.removeFolder(child);
			}
		}
		file.delete();
	}

}
