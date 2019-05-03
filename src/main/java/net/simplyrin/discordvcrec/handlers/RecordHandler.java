package net.simplyrin.discordvcrec.handlers;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.apache.commons.lang.ArrayUtils;

import lombok.Getter;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.dv8tion.jda.api.audio.UserAudio;
import net.dv8tion.jda.api.entities.Guild;
import net.simplyrin.discordvcrec.Main;
import net.simplyrin.discordvcrec.utils.ThreadPool;

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
public class RecordHandler implements AudioReceiveHandler, AudioSendHandler {

	@Getter
	private Main instance;
	@Getter
	private Guild guild;

	@Getter
	private List<Byte> bytes = new ArrayList<>();

	@Getter
	private byte[] lastBytes;

	@Getter
	private String cacheId;
	@Getter
	private File cacheFolder;
	private int cacheCount = 0;

	public RecordHandler(Main instance, Guild guild) {
		this.instance = instance;
		this.guild = guild;

		this.cacheId = UUID.randomUUID().toString().split("-")[0];
		this.cacheFolder = new File(this.instance.getCacheFolder(), this.cacheId);
		this.cacheFolder.mkdirs();
	}

	public boolean canReceiveCombined() {
		return true;
	}

	public boolean canReceiveUser() {
		return false;
	}

	public void handleCombinedAudio(CombinedAudio combinedAudio) {
		byte[] bytes = combinedAudio.getAudioData(1.0);

		for (byte _byte : bytes) {
			this.bytes.add(_byte);
		}

		// 1 min
		// 3840 (20ms) * 50 (= 1s) * 60 (= 1m) => 11520000
		//                         * 10 (= 10s) => 1920000
		if (this.bytes.size() >= 11520000) {
			List<Byte> copiped = new ArrayList<>();
			copiped.addAll(this.bytes);

			this.bytes.clear();

			ThreadPool.run(() -> {
				try {
					File file = new File(this.cacheFolder, this.cacheCount + ".wav");
					File mp3 = new File(this.cacheFolder, this.cacheCount + ".mp3");

					System.out.println("サーバー: " + this.guild.getName() + " (" + this.guild.getId() + "), ファイル: " + file.getPath());

					this.cacheCount++;

					byte[] _bytes = ArrayUtils.toPrimitive(copiped.toArray(new Byte[copiped.size()]));

					ByteArrayInputStream inputStream = new ByteArrayInputStream(_bytes);

					AudioFormat audioFormat = new AudioFormat(48000F, 16, 2, true, true);
					AudioInputStream audioInputStream = new AudioInputStream(inputStream, audioFormat, _bytes.length);

					AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, file);

					this.convertToMP3(file, mp3, true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}

		if (this.canProvide()) {
			this.lastBytes = bytes;
		}
	}

	public void saveAndQuit() {
		{
			File file = new File(this.cacheFolder, this.cacheCount + ".wav");
			File mp3 = new File(this.cacheFolder, this.cacheCount + ".mp3");

			byte[] _bytes = ArrayUtils.toPrimitive(this.bytes.toArray(new Byte[this.bytes.size()]));

			ByteArrayInputStream inputStream = new ByteArrayInputStream(_bytes);

			AudioFormat audioFormat = new AudioFormat(48000F, 16, 2, true, true);
			AudioInputStream audioInputStream = new AudioInputStream(inputStream, audioFormat, _bytes.length);

			try {
				AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, file);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("サーバー: " + this.guild.getName() + " (" + this.guild.getId() + "), ファイル: " + file.getPath());

			this.convertToMP3(file, mp3, true);
		}

		File cacheFolder = this.cacheFolder;
		File[] files = cacheFolder.listFiles();

		if (this.instance.getConfig().getBoolean("PrintFFMpegLog")) {
			System.out.println("サーバー: " + this.guild.getName() + " (" + this.guild.getId() + ")");
			System.out.println("処理ファイル一覧 (" + files.length + "): " + Arrays.asList(files).toString());
		}

		String rawCommand = "ffmpeg ";
		List<String> command = new ArrayList<>();
		command.add("ffmpeg");

		for (File file : files) {
			rawCommand += "-i ";
			rawCommand += "\"" + file.getPath() + "\" ";

			command.add("-i");
			command.add("\"" + file.getPath() + "\"");
		}

		File guildFolder = new File(this.instance.getRecordFolder(), this.guild.getId());
		guildFolder.mkdir();

		String timeStamp = this.instance.getTimeStamp();
		File outputMP3 = new File(guildFolder, timeStamp + ".mp3");

		rawCommand += "-filter_complex ";
		rawCommand += "\"concat=n=" + files.length + ":v=0:a=1\" ";
		rawCommand += "\"" + outputMP3.getPath() + "\"";

		command.add("-filter_complex");
		command.add("concat=n=" + files.length + ":v=0:a=1");
		command.add("\"" + outputMP3.getPath() + "\"");

		ProcessBuilder processBuilder = new ProcessBuilder(command);

		if (this.instance.getConfig().getBoolean("PrintFFMpegLog")) {
			System.out.println("コマンド: " + rawCommand);
			System.out.println("コマンド: " + command.toString());
		}

		String uniqueId = UUID.randomUUID().toString().split("-")[0];

		File batFile = new File(uniqueId + ".bat");
		if (!batFile.exists()) {
			try {
				batFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(uniqueId + ".bat");
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter printWriter = new PrintWriter(new BufferedWriter(fileWriter));
		printWriter.println("@echo off");
		printWriter.println(rawCommand);
		printWriter.println("pause");
		printWriter.close();

		String runtime = "cmd.exe /c start " + uniqueId + ".bat";
		try {
			Runtime.getRuntime().exec(runtime);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// batFile.delete();

		/* Process process = null;
		try {
			process = processBuilder.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (this.instance.getConfig().getBoolean("PrintFFMpegLog")) {
			Scanner scanner;

			scanner = new Scanner(process.getInputStream());
			while (scanner.hasNext()) {
				System.out.println("[FFMPEG] " + scanner.nextLine());
			}
			scanner.close();
			scanner = new Scanner(process.getErrorStream());
			while (scanner.hasNext()) {
				System.out.println("[FFMPEG] " + scanner.nextLine());
			}
			scanner.close();
		} */
	}

	public void convertToMP3(File old, File _new, boolean removeFile) {
		ProcessBuilder processBuilder = new ProcessBuilder("ffmpeg.exe",
				"-i",
				"\"" + old.getPath() + "\"",
				"\"" + _new.getPath() + "\"");

		try {
			Process process = processBuilder.start();
			process.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (removeFile) {
			old.delete();
		}
	}

	public void handleUserAudio(UserAudio userAudio) {
	}

	@Override
	public boolean canProvide() {
		return false;
	}

	@Override
	public ByteBuffer provide20MsAudio() {
		if (this.lastBytes != null) {
			return ByteBuffer.wrap(this.lastBytes);
		}
		return null;
	}

}
