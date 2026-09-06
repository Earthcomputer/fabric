package net.fabricmc.fabric.impl.client.gametest.recording.action;

import com.mojang.brigadier.StringReader;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.impl.client.gametest.recording.TestRecordingImpl;

public record MouseMotionAction(double xpos, double ypos, double xrel, double yrel) implements RecordingAction {
	public static MouseMotionAction read(StringReader reader) throws CommandSyntaxException {
		double xpos = reader.readDouble();
		reader.expect(' ');
		double ypos = reader.readDouble();
		reader.expect(' ');
		double xrel = reader.readDouble();
		reader.expect(' ');
		double yrel = reader.readDouble();
		return new MouseMotionAction(xpos, ypos, xrel, yrel);
	}

	@Override
	public void write(StringBuilder out) {
		out.append("mouse_motion ").append(xpos).append(' ').append(ypos).append(' ').append(xrel).append(' ').append(yrel);
	}

	@Override
	public void run(TestRecordingImpl recording) {
		recording.getContext().runOnClient(client -> client.mouseHandler.onMove(client.getWindow().handle(), xpos, ypos, xrel, yrel));
	}
}
