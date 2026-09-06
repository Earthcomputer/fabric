package net.fabricmc.fabric.impl.client.gametest.util;

import com.ibm.icu.lang.UCharacter;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;
import java.util.function.IntFunction;

public final class StringIOUtil {
	private StringIOUtil() {
	}

	public static int readIntEnum(String name, StringReader reader, Function<String, @Nullable Integer> parser) throws CommandSyntaxException {
		int mark = reader.getCursor();
		String enumName = reader.readUnquotedString();
		int endMark = reader.getCursor();
		Integer value = parser.apply(enumName);

		if (value != null) {
			return value;
		}

		reader.setCursor(mark);
		try {
			return reader.readInt();
		} catch (CommandSyntaxException _) {
		}

		reader.setCursor(endMark);
		throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedSymbol().createWithContext(reader, name);
	}

	public static void writeIntEnum(int value, StringBuilder out, IntFunction<@Nullable String> writer) {
		String str = writer.apply(value);

		if (str != null) {
			out.append(str);
		} else {
			out.append(value);
		}
	}

	public static String readQuotableString(StringReader reader) throws CommandSyntaxException {
		if (!reader.canRead() || reader.peek() != '"') {
			return reader.readUnquotedString();
		}

		reader.skip();
		StringBuilder result = new StringBuilder();

		while (reader.canRead() && reader.peek() != '"') {
			char c = reader.read();

			if (c == '\\') {
				if (!reader.canRead()) {
					break;
				}

				c = reader.read();

				switch (c) {
					case 'n' -> result.append('\n');
					case 'r' -> result.append('\r');
					case 't' -> result.append('\t');
					case 'f' -> result.append('\f');
					case 'b' -> result.append('\b');
					case '\\', '"', '\'' -> result.append(c);
					case 'u' -> {
						if (reader.getRemainingLength() < 4) {
							throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidEscape().createWithContext(reader, "u" + reader.getRemaining());
						}

						String escapeSequence = reader.getString().substring(reader.getCursor(), reader.getCursor() + 4);
						reader.setCursor(reader.getCursor() + 4);

						try {
							result.append((char) Integer.parseInt(escapeSequence, 16));
						} catch (NumberFormatException e) {
							throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidEscape().createWithContext(reader, "u" + escapeSequence);
						}
					}
					default -> throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidEscape().createWithContext(reader, c);
				}
			} else {
				result.append(c);
			}
		}

		if (!reader.canRead()) {
			throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedEndOfQuote().createWithContext(reader);
		}

		reader.skip();
		return result.toString();
	}

	public static void writeQuotableString(String value, StringBuilder out) {
		if (isUnquotedString(value)) {
			out.append(value);
			return;
		}

		out.append('"');
		value.codePoints().forEach(c -> {
			switch (c) {
				case '\n' -> out.append("\\n");
				case '\r' -> out.append("\\r");
				case '\t' -> out.append("\\t");
				case '\f' -> out.append("\\f");
				case '\b' -> out.append("\\b");
				case '\\', '"' -> out.append('\\').appendCodePoint(c);
				default -> {
					if (UCharacter.isPrintable(c)) {
						out.appendCodePoint(c);
					} else {
						out.append("\\u").append("%04x".formatted(c));
					}
				}
			}
		});
		out.append('"');
	}

	public static boolean isUnquotedString(String string) {
		if (string.isEmpty()) {
			return false;
		}

		for (int i = 0; i < string.length(); i++) {
			if (!StringReader.isAllowedInUnquotedString(string.charAt(i))) {
				return false;
			}
		}

		return true;
	}
}
