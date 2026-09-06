/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.mixin.client.gametest.input;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.SDLEventHandler;
import org.lwjgl.sdl.SDL_TextEditingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.IMECandidatesEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonInfo;

import net.fabricmc.fabric.impl.client.gametest.recording.TestRecordingImpl;

@Mixin(SDLEventHandler.class)
public class SDLEventHandlerMixin {
	@WrapWithCondition(method = "lambda$handleKeyEvent$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyboardHandler;keyPress(JILnet/minecraft/client/input/KeyEvent;)V"))
	private static boolean handleKeyEvent(KeyboardHandler instance, long handle, int action, KeyEvent event) {
		if (TestRecordingImpl.isRecording() && handle == Minecraft.getInstance().getWindow().handle()) {
			if (!TestRecordingImpl.currentRecording.handleCustomKeyAction(action, event)) {
				TestRecordingImpl.currentRecording.recordKeyEvent(action, event);
				return true;
			}
		}

		return false;
	}

	@WrapWithCondition(method = "handleTextEditingEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;execute(Ljava/lang/Runnable;)V"))
	private static boolean handleTextEditingEvent(Minecraft instance, Runnable runnable, @Local(name = "handle") long handle, @Local(name = "edit") SDL_TextEditingEvent edit) {
		if (TestRecordingImpl.isRecording() && handle == instance.getWindow().handle()) {
			TestRecordingImpl.currentRecording.recordTextEditingEvent(edit.textString(), edit.start(), edit.length());
			return true;
		}

		return false;
	}

	@WrapWithCondition(method = "lambda$handleTextEditingCandidatesEvent$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyboardHandler;textEditingCandidates(JLnet/minecraft/client/input/IMECandidatesEvent;)V"))
	private static boolean handleTextEditingCandidatesEvent(KeyboardHandler instance, long handle, IMECandidatesEvent event) {
		if (TestRecordingImpl.isRecording() && handle == Minecraft.getInstance().getWindow().handle()) {
			TestRecordingImpl.currentRecording.recordTextEditingCandidatesEvent(event);
			return true;
		}

		return false;
	}

	@WrapWithCondition(method = "lambda$handleTextInputEvent$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyboardHandler;textInput(JLjava/lang/String;)V"))
	private static boolean handleTextInputEvent(KeyboardHandler instance, long handle, String text) {
		if (TestRecordingImpl.isRecording() && handle == Minecraft.getInstance().getWindow().handle()) {
			TestRecordingImpl.currentRecording.recordTextInputEvent(text);
			return true;
		}

		return false;
	}

	@WrapWithCondition(method = "lambda$handleMouseMotionEvent$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;onMove(JDDDD)V"))
	private static boolean handleMouseMotionEvent(MouseHandler instance, long handle, double xpos, double ypos, double xrel, double yrel) {
		if (TestRecordingImpl.isRecording() && handle == Minecraft.getInstance().getWindow().handle()) {
			TestRecordingImpl.currentRecording.recordMouseMotionEvent(xpos, ypos, xrel, yrel);
			return true;
		}

		return false;
	}

	@WrapWithCondition(method = "lambda$handleMouseButtonEvent$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;onButton(JLnet/minecraft/client/input/MouseButtonInfo;I)V"))
	private static boolean handleMouseButtonEvent(MouseHandler instance, long handle, MouseButtonInfo rawButtonInfo, int action) {
		if (TestRecordingImpl.isRecording() && handle == Minecraft.getInstance().getWindow().handle()) {
			TestRecordingImpl.currentRecording.recordMouseButtonEvent(rawButtonInfo, action);
			return true;
		}

		return false;
	}

	@WrapWithCondition(method = "lambda$handleMouseWheelEvent$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;onScroll(JDD)V"))
	private static boolean handleMouseWheelEvent(MouseHandler instance, long handle, double xoffset, double yoffset) {
		if (TestRecordingImpl.isRecording() && handle == Minecraft.getInstance().getWindow().handle()) {
			TestRecordingImpl.currentRecording.recordMouseWheelEvent(xoffset, yoffset);
			return true;
		}

		return false;
	}

	@Inject(method = {
			"handleDropFileEvent",
			"handleDropBeginEvent",
			"handleDropCompleteEvent"
	}, at = @At("HEAD"), cancellable = true)
	private void handleEventsThatAlwaysDisableRealInput(CallbackInfo ci) {
		ci.cancel();
	}
}
