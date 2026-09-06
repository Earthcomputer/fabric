package net.fabricmc.fabric.impl.client.gametest.recording.action;

import com.mojang.brigadier.StringReader;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.impl.client.gametest.recording.TestRecordingImpl;

public record TickAction(int ticks) implements RecordingAction {
	public static TickAction read(StringReader reader) throws CommandSyntaxException {
		int ticks = reader.readInt();

		if (ticks <= 0) {
			throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooLow().createWithContext(reader, ticks, 1);
		}

		return new TickAction(ticks);
	}

	@Override
	public void write(StringBuilder out) {
		out.append("tick ").append(ticks);
	}

	@Override
	public void run(TestRecordingImpl recording) {
		recording.getContext().waitTicks(ticks);
	}
}
