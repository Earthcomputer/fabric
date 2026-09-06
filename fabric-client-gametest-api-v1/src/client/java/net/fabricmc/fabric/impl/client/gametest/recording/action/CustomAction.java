package net.fabricmc.fabric.impl.client.gametest.recording.action;

import com.mojang.brigadier.StringReader;

import net.fabricmc.fabric.impl.client.gametest.recording.TestRecordingImpl;

public record CustomAction(String name) implements RecordingAction {
	public static CustomAction read(StringReader reader) {
		return new CustomAction(reader.readUnquotedString());
	}

	@Override
	public void write(StringBuilder out) {
		out.append("custom ").append(name);
	}

	@Override
	public void run(TestRecordingImpl recording) {
		recording.runCustomAction(name);
	}
}
