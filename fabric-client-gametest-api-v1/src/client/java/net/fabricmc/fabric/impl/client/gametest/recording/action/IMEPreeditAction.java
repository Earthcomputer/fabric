package net.fabricmc.fabric.impl.client.gametest.recording.action;

import com.mojang.brigadier.StringReader;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.impl.client.gametest.recording.TestRecordingImpl;
import net.fabricmc.fabric.impl.client.gametest.util.StringIOUtil;

import net.minecraft.client.input.PreeditEvent;

public record IMEPreeditAction(String text, int selectionStart, int selectionLength) implements RecordingAction {
	public static IMEPreeditAction read(StringReader reader) throws CommandSyntaxException {
		int selectionStart = reader.readInt();
		reader.expect(' ');
		int selectionLength = reader.readInt();
		reader.expect(' ');
		String text = StringIOUtil.readQuotableString(reader);
		return new IMEPreeditAction(text, selectionStart, selectionLength);
	}

	@Override
	public void write(StringBuilder out) {
		out.append("ime_preedit ").append(selectionStart).append(' ').append(selectionLength).append(' ');
		StringIOUtil.writeQuotableString(text, out);
	}

	@Override
	public void run(TestRecordingImpl recording) {
		recording.getContext().runOnClient(client -> {
			PreeditEvent event = PreeditEvent.fromSdlTextEditing(text, selectionStart, selectionLength);
			client.keyboardHandler.textEditing(client.getWindow().handle(), event);
		});
	}
}
