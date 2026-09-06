package net.fabricmc.fabric.api.client.gametest.v1.context;

import com.google.common.base.Preconditions;

import net.fabricmc.fabric.impl.client.gametest.recording.TestRecordingBuilderImpl;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface TestRecordingBuilder {
	static TestRecordingBuilder create(String fileName) {
		Preconditions.checkNotNull(fileName, "fileName");

		return new TestRecordingBuilderImpl(fileName);
	}

	TestRecordingBuilder setStopKey(int keyCode);

	TestRecordingBuilder addCustomAction(String name, Runnable action);

	TestRecordingBuilder addCustomAction(int keyCode, String name, Runnable action);

	TestRecordingBuilder accumulateMouseMovement();
}
