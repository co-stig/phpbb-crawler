package net.kulak.psy.data.schema;

public enum InteractionType {
	UNKNOWN, POSITIVE, NEGATIVE, EXTREMELY_NEGATIVE;
	
	public static InteractionType fromString(String s) {
		if (s.equals("P")) {
			return POSITIVE;
		} else if (s.equals("N")) {
			return NEGATIVE;
		} else if (s.equals("E")) {
			return EXTREMELY_NEGATIVE;
		} else {
			return UNKNOWN;
		}
	}
}
