package de.doccrazy.shared.core;

public class Debug {
	public static boolean ON = false;

	private Debug() {
	}

	static {
		assert enableDebug();
	}

	private static boolean enableDebug() {
		ON = true;
		return true;
	}
}
