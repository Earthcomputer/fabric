package net.fabricmc.fabric.impl.client.gametest.recording.action;

import com.mojang.brigadier.StringReader;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.impl.client.gametest.recording.TestRecordingImpl;
import net.fabricmc.fabric.impl.client.gametest.util.StringIOUtil;

public record TextInputAction(String text) implements RecordingAction {
	public static TextInputAction read(StringReader reader) throws CommandSyntaxException {
		return new TextInputAction(StringIOUtil.readQuotableString(reader));
	}

	@Override
	public void write(StringBuilder out) {
		out.append("text_input ");
		StringIOUtil.writeQuotableString(text, out);
	}

	@Override
	public void run(TestRecordingImpl recording) {
		recording.getContext().runOnClient(client -> client.keyboardHandler.textInput(client.getWindow().handle(), text));
	}
}
