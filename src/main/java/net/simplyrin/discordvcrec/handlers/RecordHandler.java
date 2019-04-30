package net.simplyrin.discordvcrec.handlers;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.dv8tion.jda.api.audio.UserAudio;
import net.dv8tion.jda.api.entities.Guild;
import net.simplyrin.discordvcrec.Main;

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

	public RecordHandler(Main instance, Guild guild) {
		this.instance = instance;
		this.guild = guild;
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
		if (this.canProvide()) {
			this.lastBytes = bytes;
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
