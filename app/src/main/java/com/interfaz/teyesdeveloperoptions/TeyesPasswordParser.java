package com.interfaz.teyesdeveloperoptions;

/** Interprets the vendor dialog's exact split/comparison order; never writes settings. */
public final class TeyesPasswordParser {
    private TeyesPasswordParser() {}
    public static final String LAST_KNOWN_KEY = "502105";
    public static final String REFERENCE_FIRMWARE = "25490.3833.20260905.005748";

    public static String unavailable(String reason) {
        return reason + "\n\nSuggested historical key: " + LAST_KNOWN_KEY
                + "\nThis was the last discovered key on CC4 Pro firmware " + REFERENCE_FIRMWARE
                + ". It was NOT read from this device and is not confirmed for the current firmware."
                + "\n\nA different current key cannot be confirmed without a successful read.";
    }

    public static String describe(String value) {
        if (value == null) return unavailable("The device returned no stored key.");
        if (value.isEmpty()) return unavailable("The stored key is empty.");
        String[] fields = value.split(",");
        // Vendor reads fields 1 and 2 before comparing debug field 0.
        if (fields.length < 3) return unavailable("The stored value has an incomplete format.");
        String key = fields[0];
        if (!key.matches("[0-9]+")) return unavailable("The stored debug field is not a numeric key.");
        if (key.equals(fields[1]) || key.equals(fields[2])) {
            return unavailable("The stored debug field conflicts with another menu code; the Debug branch cannot be confirmed.");
        }
        return "Key read from this device: " + key + "\n\n"
                + (LAST_KNOWN_KEY.equals(key)
                    ? "Confirmed: the currently stored key matches the last discovered key (502105)."
                    : "Different key confirmed: this device currently stores " + key
                        + ", instead of the last discovered key (502105). Use the key read above.")
                + "\n\nThis confirms the stored value, not successful entry into the vendor Debug menu."
                + "\nReference firmware: " + REFERENCE_FIRMWARE + ".";
    }
}