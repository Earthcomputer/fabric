package net.fabricmc.fabric.impl.client.gametest.recording.action;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.impl.client.gametest.recording.TestRecordingImpl;

import net.fabricmc.fabric.impl.client.gametest.util.StringIOUtil;

import net.minecraft.client.input.KeyEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

import org.lwjgl.sdl.SDLKeycode;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

public record KeyAction(int action, KeyEvent event) implements RecordingAction {
	private static final BiMap<String, Integer> ACTIONS = ImmutableBiMap.of(
			"press", InputConstants.PRESS,
			"release", InputConstants.RELEASE,
			"repeat", InputConstants.REPEAT
	);
	private static final BiMap<String, Integer> KEYCODES = Util.make(HashBiMap.create(), map -> {
		for (Field field : SDLKeycode.class.getFields()) {
			if (field.getType() == int.class && Modifier.isStatic(field.getModifiers())) {
				String fieldName = field.getName();

				if (fieldName.startsWith("SDLK_") && !fieldName.endsWith("_MASK")) {
					try {
						map.put(fieldName.substring("SDLK_".length()), field.getInt(null));
					} catch (ReflectiveOperationException e) {
						throw new AssertionError(e);
					}
				}
			}
		}
	});
	private static final Map<String, Integer> MODIFIERS = Util.make(new LinkedHashMap<>(), map -> {
		for (Field field : SDLKeycode.class.getFields()) {
			if (field.getType() == int.class && Modifier.isStatic(field.getModifiers())) {
				String fieldName = field.getName();

				if (fieldName.startsWith("SDL_KMOD_")) {
					int value;

					try {
						value = field.getInt(null);
					} catch (ReflectiveOperationException e) {
						throw new AssertionError(e);
					}

					if (Mth.isPowerOfTwo(value)) {
						map.put(fieldName.substring("SDL_KMOD_".length()), value);
					}
				}
			}
		}
	});

	public static KeyAction read(StringReader reader) throws CommandSyntaxException {
		int action = StringIOUtil.readIntEnum("action", reader, ACTIONS::get);
		reader.expect(' ');
		int scancode = StringIOUtil.readIntEnum("scancode", reader, name -> {
			try {
				InputConstants.Key key = InputConstants.getKey(name);
				return key.getType() == InputConstants.Type.KEYBOARD ? key.getValue() : null;
			} catch (IllegalArgumentException _) {
				return null;
			}
		});
		reader.expect(' ');
		int keycode = StringIOUtil.readIntEnum("keycode", reader, KEYCODES::get);
		int modifiers = readModifiers(reader);
		return new KeyAction(action, new KeyEvent(scancode, keycode, modifiers));
	}

	public static int readModifiers(StringReader reader) throws CommandSyntaxException {
		int modifiers = 0;

		while (reader.canRead()) {
			reader.expect(' ');
			int modifier = StringIOUtil.readIntEnum("modifier", reader, MODIFIERS::get);
			modifiers |= modifier;
		}

		return modifiers;
	}

	@Override
	public void write(StringBuilder out) {
		out.append("key ");
		StringIOUtil.writeIntEnum(action, out, ACTIONS.inverse()::get);
		out.append(' ');
		StringIOUtil.writeIntEnum(event.key(), out, value -> InputConstants.Type.KEYBOARD.getOrCreate(value).getName());
		out.append(' ');
		StringIOUtil.writeIntEnum(event.shortcutKey(), out, KEYCODES.inverse()::get);
		writeModifiers(event.modifiers(), out);
	}

	public static void writeModifiers(int modifiers, StringBuilder out) {
		for (Map.Entry<String, Integer> entry : MODIFIERS.entrySet()) {
			if ((modifiers & entry.getValue()) == entry.getValue()) {
				modifiers &= ~entry.getValue();
				out.append(' ').append(entry.getKey());
			}
		}

		if (modifiers != 0) {
			out.append(' ').append(modifiers);
		}
	}

	@Override
	public void run(TestRecordingImpl recording) {
		recording.getContext().runOnClient(client -> client.keyboardHandler.keyPress(client.getWindow().handle(), action, event));
	}
}
