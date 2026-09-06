package net.fabricmc.fabric.impl.client.gametest.recording;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;

import com.mojang.brigadier.StringReader;

import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestRecordingBuilder;
import net.fabricmc.fabric.impl.client.gametest.util.StringIOUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestRecordingBuilderImpl implements TestRecordingBuilder {
	private final String fileName;
	private int stopKey = InputConstants.KEY_RETURN;
	private final Map<Integer, TestRecordingCustomAction> customActionsByKeyCode = new HashMap<>();
	private final Map<String, TestRecordingCustomAction> customActionsByName = new HashMap<>();
	private boolean accumulateMouseMovement = false;

	public TestRecordingBuilderImpl(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public TestRecordingBuilder setStopKey(int keyCode) {
		Preconditions.checkArgument(keyCode >= 0, "keyCode must be non-negative");

		if (customActionsByKeyCode.containsKey(keyCode)) {
			throw new IllegalArgumentException("Stop key " + keyCode + " conflicts with custom action " + customActionsByKeyCode.get(keyCode).name());
		}

		stopKey = keyCode;
		return this;
	}

	@Override
	public TestRecordingBuilder addCustomAction(String name, Runnable action) {
		Preconditions.checkNotNull(name, "name");
		Preconditions.checkArgument(StringIOUtil.isUnquotedString(name), "Invalid custom action name: %s", name);
		Preconditions.checkNotNull(action, "action");

		if (customActionsByName.containsKey(name)) {
			throw new IllegalArgumentException("A custom action with name " + name + " already exists");
		}

		customActionsByName.put(name, new TestRecordingCustomAction(name, action));
		return this;
	}

	@Override
	public TestRecordingBuilder addCustomAction(int keyCode, String name, Runnable action) {
		Preconditions.checkArgument(keyCode >= 0, "keyCode must be non-negative");
		Preconditions.checkNotNull(name, "name");
		Preconditions.checkArgument(StringIOUtil.isUnquotedString(name), "Invalid custom action name: %s", name);
		Preconditions.checkNotNull(action, "action");

		if (customActionsByName.containsKey(name)) {
			throw new IllegalArgumentException("A custom action with name " + name + " already exists");
		}

		if (customActionsByKeyCode.containsKey(keyCode)) {
			throw new IllegalArgumentException("Key code " + keyCode + " conflicts with existing custom action " + customActionsByKeyCode.get(keyCode).name());
		}

		if (keyCode == stopKey) {
			throw new IllegalArgumentException("Key code " + keyCode + " conflicts with the stop key");
		}

		TestRecordingCustomAction customAction = new TestRecordingCustomAction(name, action);
		customActionsByKeyCode.put(keyCode, customAction);
		customActionsByName.put(name, customAction);
		return this;
	}

	@Override
	public TestRecordingBuilder accumulateMouseMovement() {
		accumulateMouseMovement = true;
		return this;
	}

	public TestRecordingImpl build(ClientGameTestContext context) {
		return new TestRecordingImpl(context, fileName, stopKey, customActionsByKeyCode, customActionsByName, accumulateMouseMovement);
	}
}
