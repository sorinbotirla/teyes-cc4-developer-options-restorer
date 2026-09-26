import com.interfaz.teyesdeveloperoptions.TeyesPasswordParser;
public final class ParserSmokeTest {
    private static int checks;
    private static void expect(String input, String expected) {
        if (!TeyesPasswordParser.describe(input).contains(expected)) throw new AssertionError(expected);
        checks++;
    }
    public static void main(String[] args) {
        expect(null, "Suggested historical key: 502105");
        expect("", "NOT read from this device");
        expect(",,,,", "incomplete format");
        expect("502105,123,456,789,012", "currently stored key matches");
        expect("654321,123,456,789,012", "Different key confirmed");
        expect("654321,123,456", "Key read from this device: 654321");
        expect("502105,502105,456", "conflicts with another menu code");
        expect("502105,123,502105", "conflicts with another menu code");
        expect(" 502105,123,456", "not a numeric key");
        expect("abc,123,456", "not a numeric key");
        expect("502105", "incomplete format");
        expect("502105,", "incomplete format");
        String denied = TeyesPasswordParser.unavailable("Access denied.");
        if (!denied.contains("502105") || !denied.contains("cannot be confirmed")) throw new AssertionError("Read failure fallback");
        System.out.println((checks + 1) + " parser checks passed");
    }
}