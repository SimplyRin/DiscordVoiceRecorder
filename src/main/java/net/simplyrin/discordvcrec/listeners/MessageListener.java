package net.simplyrin.discordvcrec.listeners;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
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
import net.simplyrin.discordvcrec.utils.MultiProcess;
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

		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setColor(Color.GREEN);

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
					embedBuilder.setColor(Color.RED);
					embedBuilder.setDescription("あなたはボイスチャンネルに参加している必要があります！");
					channel.sendMessage(embedBuilder.build()).complete();
					return;
				}

				AudioManager audioManager = guild.getAudioManager();
				RecordHandler recordHandler = new RecordHandler(this.instance, guild);
				audioManager.setReceivingHandler(recordHandler);
				audioManager.setSendingHandler(recordHandler);
				audioManager.openAudioConnection(voiceChannel);

				this.instance.getTimeManager().getManager(guild.getId() + ":" + channel.getId()).joined();

				embedBuilder.setDescription("ボイスチャンネルの録音を開始します...。");
				channel.sendMessage(embedBuilder.build()).complete();

				System.out.println("サーバー: " + guild.getName() + ", チャンネル: " + voiceChannel.getName() + " に参加しました。");
				return;
			}

			if (args[0].equalsIgnoreCase("!quit")) {
				embedBuilder.setDescription("録音ファイルの処理が終了しました！");

				AudioManager audioManager = guild.getAudioManager();

				RecordHandler recordHandler = (RecordHandler) audioManager.getReceivingHandler();

				MultiProcess multiProcess = new MultiProcess();
				multiProcess.addProcess(() -> recordHandler.saveAndQuit());
				multiProcess.setFinishedTask(() -> {
					embedBuilder.clear();
					embedBuilder.setColor(Color.GREEN);
					embedBuilder.setDescription("ファイルの処理が終了しました。");
					channel.sendMessage(embedBuilder.build()).complete();
				});
				multiProcess.start();

				audioManager.closeAudioConnection();

				TimeManager timeManager = this.instance.getTimeManager().getManager(guild.getId() + ":" + channel.getId());
				embedBuilder.setDescription("録音が終了しました。\nファイルを処理しています...。");
				embedBuilder.addField("録音時間", timeManager.getCurrentTime(), true);
				channel.sendMessage(embedBuilder.build()).complete();
				timeManager.close();
				return;
			}
		}
	}

}
