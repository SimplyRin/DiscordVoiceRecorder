package net.simplyrin.discordvcrec.listeners;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.apache.commons.lang.ArrayUtils;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import net.simplyrin.discordvcrec.Main;
import net.simplyrin.discordvcrec.handlers.RecordHandler;
import net.simplyrin.discordvcrec.utils.TimeManager;

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
public class MessageListener extends ListenerAdapter {

	private Main instance;

	public MessageListener(Main instance) {
		this.instance = instance;
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.isFromType(ChannelType.PRIVATE)) {
			return;
		}

		User user = event.getAuthor();
		Guild guild = event.getGuild();
		MessageChannel channel = event.getChannel();

		if (user.isBot() || user.isFake()) {
			return;
		}

		String[] args = event.getMessage().getContentRaw().split(" ");

		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("!record") || args[0].equalsIgnoreCase("!rec")) {
				VoiceChannel voiceChannel = null;
				for (VoiceChannel vc : guild.getVoiceChannels()) {
					for (Member member : vc.getMembers()) {
						if (user.getId().equals(member.getId())) {
							voiceChannel = vc;
						}
					}
				}

				if (voiceChannel == null) {
					channel.sendMessage("あなたはボイスチャンネルに参加している必要があります！").complete();
					return;
				}

				AudioManager audioManager = guild.getAudioManager();
				RecordHandler recordHandler = new RecordHandler(this.instance, guild);
				audioManager.setReceivingHandler(recordHandler);
				audioManager.setSendingHandler(recordHandler);
				audioManager.openAudioConnection(voiceChannel);

				this.instance.getTimeManager().getManager(guild.getId() + ":" + channel.getId()).joined();

				channel.sendMessage("ボイスチャンネルの録音を開始します...。").complete();

				System.out.println("サーバー: " + guild.getName() + ", チャンネル: " + voiceChannel.getName() + " に参加しました。");
				return;
			}

			if (args[0].equalsIgnoreCase("!quit")) {
				AudioManager audioManager = guild.getAudioManager();

				RecordHandler recordHandler = (RecordHandler) audioManager.getReceivingHandler();

				List<Byte> list = recordHandler.getBytes();

				List<Byte> copiped = new ArrayList<>();
				copiped.addAll(list);

				byte[] _bytes = ArrayUtils.toPrimitive(copiped.toArray(new Byte[copiped.size()]));

				String id = recordHandler.getGuild().getId();

				File guildFolder = new File(this.instance.getRecordFolder(), id);
				guildFolder.mkdir();

				String timeStamp = this.instance.getTimeStamp();
				File file = new File(guildFolder, timeStamp + ".wav");
				if (!file.exists()) {
					try {
						file.createNewFile();
					} catch (Exception e) {
					}
				}

				System.out.println("Byte size: " + _bytes.length);

				try {
					ByteArrayInputStream inputStream = new ByteArrayInputStream(_bytes);

					AudioFormat audioFormat = new AudioFormat(48000F, 16, 2, true, true);
					AudioInputStream audioInputStream = new AudioInputStream(inputStream, audioFormat, _bytes.length);

					AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, file);
					System.out.println("File saved: " + file.getName() + ", bytes: " + _bytes.length);
				} catch (Exception e) {
					e.printStackTrace();
				}

				audioManager.closeAudioConnection();

				if (this.instance.isFFMpegExists()) {
					System.out.println("録音したファイルを ffmpeg.exe を使用して MP3 に変換しています...");

					File mp3 = new File(guildFolder, timeStamp + ".mp3");

					ProcessBuilder processBuilder = new ProcessBuilder("ffmpeg.exe",
							"-i",
							"\"" + file.getPath() + "\"",
							"\"" + mp3.getPath() + "\"");

					Process process = null;
					try {
						process = processBuilder.start();
					} catch (IOException e) {
						e.printStackTrace();
					}

					Scanner scanner = new Scanner(process.getInputStream());
					while (scanner.hasNext()) {
						System.out.println("ffmpeg.exe: " + scanner.nextLine());
					}
					scanner.close();

					file.delete();
				}

				TimeManager timeManager = this.instance.getTimeManager().getManager(guild.getId() + ":" + channel.getId());
				channel.sendMessage("録音が終了しました。\n録音時間: " + timeManager.getCurrentTime()).complete();
				timeManager.close();
				return;
			}
		}
	}

}
