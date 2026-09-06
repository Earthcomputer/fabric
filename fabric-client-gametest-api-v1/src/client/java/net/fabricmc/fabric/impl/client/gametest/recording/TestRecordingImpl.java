package net.fabricmc.fabric.impl.client.gametest.recording;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import com.mojang.brigadier.StringReader;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.impl.client.gametest.FabricClientGameTestRunner;

import net.fabricmc.fabric.impl.client.gametest.TestSystemProperties;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.input.IMECandidatesEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonInfo;

import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.impl.client.gametest.recording.action.IMECandidatesAction;
import net.fabricmc.fabric.impl.client.gametest.recording.action.IMEPreeditAction;
import net.fabricmc.fabric.impl.client.gametest.recording.action.KeyAction;
import net.fabricmc.fabric.impl.client.gametest.recording.action.MouseAction;
import net.fabricmc.fabric.impl.client.gametest.recording.action.MouseMotionAction;
import net.fabricmc.fabric.impl.client.gametest.recording.action.MouseWheelAction;
import net.fabricmc.fabric.impl.client.gametest.recording.action.RecordingAction;
import net.fabricmc.fabric.impl.client.gametest.recording.action.TextInputAction;
import net.fabricmc.fabric.impl.client.gametest.recording.action.TickAction;

public class TestRecordingImpl {
	@Nullable
	public static TestRecordingImpl currentRecording;
	private final ClientGameTestContext context;
	private final String fileName;
	private final int stopKey;
	private final Map<Integer, TestRecordingCustomAction> customActionsByKeyCode;
	private final Map<String, TestRecordingCustomAction> customActionsByName;
	private final boolean accumulateMouseMovement;
	private boolean isRecording;
	private final List<RecordingAction> actions = new ArrayList<>();

	public TestRecordingImpl(
			ClientGameTestContext context,
			String fileName,
			int stopKey,
			Map<Integer, TestRecordingCustomAction> customActionsByKeyCode,
			Map<String, TestRecordingCustomAction> customActionsByName,
			boolean accumulateMouseMovement
	) {
		this.context = context;
		this.fileName = fileName;
		this.stopKey = stopKey;
		this.customActionsByKeyCode = customActionsByKeyCode;
		this.customActionsByName = customActionsByName;
		this.accumulateMouseMovement = accumulateMouseMovement;
	}

	public static boolean isRecording() {
		return currentRecording != null && currentRecording.isRecording;
	}

	public ClientGameTestContext getContext() {
		return context;
	}

	public void load() {
		Path recordingPath = FabricClientGameTestRunner.currentlyRunningGameTest.getProvider()
				.findPath("recordings/" + fileName + ".txt")
				.orElse(null);
		int lineNumber = 0;

		try (BufferedReader reader = Files.newBufferedReader(recordingPath)) {
			String line;
			lineNumber++;

			while ((line = reader.readLine()) != null) {
				if (line.isBlank() || line.startsWith("#")) {
					continue;
				}

				StringReader stringReader = new StringReader(line);
				stringReader.skipWhitespace();
				String actionName = stringReader.readUnquotedString();
				RecordingAction.Reader actionReader = RecordingAction.READERS.get(actionName);

				if (actionReader == null) {
					throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(stringReader);
				}

				RecordingAction action = actionReader.read(stringReader);
				stringReader.skipWhitespace();

				if (stringReader.canRead()) {
					while (stringReader.canRead() && !Character.isWhitespace(stringReader.peek())) {
						stringReader.skip();
					}

					throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(stringReader);
				}

				this.actions.add(action);
			}
		} catch (NoSuchFileException _) {
			// record if the file doesn't exist
			if (TestSystemProperties.TEST_MOD_RESOURCES_PATH == null) {
				throw new AssertionError("Recording file " + recordingPath + " does not exist and no test mod resources path is set");
			}

			isRecording = true;
		} catch (IOException e) {
			throw new AssertionError("Failed to load recording file " + recordingPath, e);
		} catch (CommandSyntaxException e) {
			throw new AssertionError("Failed to parse recording file " + recordingPath + " at line " + lineNumber, e);
		}
	}

	public void save() {
		Preconditions.checkState(TestSystemProperties.TEST_MOD_RESOURCES_PATH != null, "Cannot save recording file because no test mod resources path is set");

		Path savePath = Path.of(TestSystemProperties.TEST_MOD_RESOURCES_PATH).resolve("recordings").resolve(fileName + ".txt");

		try {
			Files.createDirectories(savePath.getParent());
			StringBuilder builder = new StringBuilder();

			try (BufferedWriter writer = Files.newBufferedWriter(savePath)) {
				for (RecordingAction action : actions) {
					builder.setLength(0);
					action.write(builder);
					builder.append(System.lineSeparator());
					writer.append(builder);
				}
			}
		} catch (IOException e) {
			throw new AssertionError("Failed to save recording file " + savePath, e);
		}
	}

	public void record() {

	}

	public void play() {
		for (RecordingAction action : actions) {
			action.run(this);
		}
	}

	public boolean runCustomAction(String name) {
		TestRecordingCustomAction action = customActionsByName.get(name);

		if (action != null) {
			action.action().run();
			return true;
		} else {
			return false;
		}
	}

	public boolean handleCustomKeyAction(int action, KeyEvent event) {
		// TODO
		return false;
	}

	public void recordKeyEvent(int action, KeyEvent event) {
		actions.add(new KeyAction(action, event));
	}

	public void recordTextEditingEvent(@Nullable String text, int selectionStart, int selectionLength) {
		actions.add(new IMEPreeditAction(Strings.nullToEmpty(text), selectionStart, selectionLength));
	}

	public void recordTextEditingCandidatesEvent(IMECandidatesEvent event) {
		actions.add(new IMECandidatesAction(event));
	}

	public void recordTextInputEvent(String text) {
		actions.add(new TextInputAction(text));
	}

	public void recordMouseMotionEvent(double xpos, double ypos, double xrel, double yrel) {
		if (accumulateMouseMovement) {
			int index = actions.size() - 1;

			if (index > 0 && actions.get(index) instanceof TickAction) {
				index--;
			}

			if (index >= 0 && actions.get(index) instanceof MouseMotionAction(double xpos1, double ypos1, double xrel1, double yrel1)) {
				actions.remove(index);
				actions.add(new MouseMotionAction(xpos, ypos, xrel + xrel1, ypos + yrel1));
				return;
			}
		}

		actions.add(new MouseMotionAction(xpos, ypos, xrel, yrel));
	}

	public void recordMouseButtonEvent(MouseButtonInfo rawButtonInfo, int action) {
		actions.add(new MouseAction(rawButtonInfo, action));
	}

	public void recordMouseWheelEvent(double xoffset, double yoffset) {
		actions.add(new MouseWheelAction(xoffset, yoffset));
	}

	public void recordTick() {
		if (!actions.isEmpty() && actions.getLast() instanceof TickAction tickAction) {
			actions.set(actions.size() - 1, new TickAction(tickAction.ticks() + 1));
		} else {
			actions.add(new TickAction(1));
		}
	}
}
