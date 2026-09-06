package net.fabricmc.fabric.impl.client.gametest.recording.action;

import com.mojang.brigadier.StringReader;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.impl.client.gametest.recording.TestRecordingImpl;

import net.fabricmc.fabric.impl.client.gametest.util.StringIOUtil;

import net.minecraft.client.input.IMECandidatesEvent;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public record IMECandidatesAction(IMECandidatesEvent event) implements RecordingAction {
	public static IMECandidatesAction read(StringReader reader) throws CommandSyntaxException {
		int selectedCandidate = reader.readInt();
		reader.expect(' ');

		boolean horizontal = switch (reader.readUnquotedString()) {
			case "horizontal" -> true;
			case "vertical" -> false;
			default -> throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedSymbol().createWithContext(reader, "horizontal/vertical");
		};

		List<String> candidates = new ArrayList<>();

		while (reader.canRead()) {
			reader.expect(' ');
			candidates.add(StringIOUtil.readQuotableString(reader));
		}

		if (selectedCandidate < 0 || selectedCandidate >= candidates.size()) {
			selectedCandidate = -1;
		}

		return new IMECandidatesAction(new IMECandidatesEvent(candidates, selectedCandidate, horizontal));
	}

	@Override
	public void write(StringBuilder out) {
		out.append("ime_candidates ").append(event.selectedCandidate()).append(' ').append(event.horizontal() ? "horizontal" : "vertical");

		for (String candidate : event.candidates()) {
			out.append(' ');
			StringIOUtil.writeQuotableString(candidate, out);
		}
	}

	@Override
	public void run(TestRecordingImpl recording) {
		recording.getContext().runOnClient(client -> client.keyboardHandler.textEditingCandidates(client.getWindow().handle(), event));
	}
}
