package net.fabricmc.fabric.impl.client.gametest.recording.action;

import com.mojang.brigadier.StringReader;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.impl.client.gametest.recording.TestRecordingImpl;
import net.minecraft.util.Util;

import java.util.HashMap;
import java.util.Map;

public interface RecordingAction {
	Map<String, Reader> READERS = Util.make(new HashMap<>(), map -> {
		map.put("custom", CustomAction::read);
		map.put("key", KeyAction::read);
		map.put("ime_preedit", IMEPreeditAction::read);
		map.put("ime_candidates", IMECandidatesAction::read);
		map.put("text_input", TextInputAction::read);
		map.put("mouse_motion", MouseMotionAction::read);
		map.put("mouse", MouseAction::read);
		map.put("mouse_wheel", MouseWheelAction::read);
		map.put("tick", TickAction::read);
	});

	void write(StringBuilder out);

	void run(TestRecordingImpl recording);

	@FunctionalInterface
	interface Reader {
		RecordingAction read(StringReader reader) throws CommandSyntaxException;
	}
}
