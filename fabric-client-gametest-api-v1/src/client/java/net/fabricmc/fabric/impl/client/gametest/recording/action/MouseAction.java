package net.fabricmc.fabric.impl.client.gametest.recording.action;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.StringReader;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.impl.client.gametest.recording.TestRecordingImpl;

import net.fabricmc.fabric.impl.client.gametest.util.StringIOUtil;

import net.minecraft.client.input.MouseButtonInfo;

public record MouseAction(MouseButtonInfo rawButtonInfo, int action) implements RecordingAction {
	private static final BiMap<String, Integer> BUTTONS = ImmutableBiMap.of(
			"left", InputConstants.MOUSE_BUTTON_LEFT,
			"middle", InputConstants.MOUSE_BUTTON_MIDDLE,
			"right", InputConstants.MOUSE_BUTTON_RIGHT
	);
	private static final BiMap<String, Integer> ACTIONS = ImmutableBiMap.of(
			"press", InputConstants.PRESS,
			"release", InputConstants.RELEASE
	);

	public static MouseAction read(StringReader reader) throws CommandSyntaxException {
		int button = StringIOUtil.readIntEnum("button", reader, BUTTONS::get);
		reader.expect(' ');
		int action = StringIOUtil.readIntEnum("action", reader, ACTIONS::get);
		int modifiers = KeyAction.readModifiers(reader);
		return new MouseAction(new MouseButtonInfo(button, modifiers), action);
	}

	@Override
	public void write(StringBuilder out) {
		out.append("mouse ");
		StringIOUtil.writeIntEnum(rawButtonInfo.button(), out, BUTTONS.inverse()::get);
		out.append(' ');
		StringIOUtil.writeIntEnum(action, out, ACTIONS.inverse()::get);
		KeyAction.writeModifiers(rawButtonInfo.modifiers(), out);
	}

	@Override
	public void run(TestRecordingImpl recording) {
		recording.getContext().runOnClient(client -> client.mouseHandler.onButton(client.getWindow().handle(), rawButtonInfo, action));
	}
}
