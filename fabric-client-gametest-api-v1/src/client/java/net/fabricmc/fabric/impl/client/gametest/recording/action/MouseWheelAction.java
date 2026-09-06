package net.fabricmc.fabric.impl.client.gametest.recording.action;

import com.mojang.brigadier.StringReader;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.impl.client.gametest.recording.TestRecordingImpl;

public record MouseWheelAction(double xoffset, double yoffset) implements RecordingAction {
	public static MouseWheelAction read(StringReader reader) throws CommandSyntaxException {
		double xoffset = reader.readDouble();
		reader.expect(' ');
		double yoffset = reader.readDouble();
		return new MouseWheelAction(xoffset, yoffset);
	}

	@Override
	public void write(StringBuilder out) {
		out.append("mouse_wheel ").append(xoffset).append(' ').append(yoffset);
	}

	@Override
	public void run(TestRecordingImpl recording) {
		recording.getContext().runOnClient(client -> client.mouseHandler.onScroll(client.getWindow().handle(), xoffset, yoffset));
	}
}
