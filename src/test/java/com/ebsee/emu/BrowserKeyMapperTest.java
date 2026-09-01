package com.ebsee.emu;

public final class BrowserKeyMapperTest {
    public static void main(String[] args) {
        assertMapping(13, 10, '\uffff', 0, "enter");
        assertMapping(37, 37, '\uffff', 0, "left arrow");
        assertMapping(87, 38, '\uffff', 0, "W as up");
        assertMapping(81, 81, 'q', 0, "Q as left soft key");
        assertMapping(69, 87, 'w', 0, "E as right soft key");
        assertMapping(53, 101, '5', 4, "number row 5 as phone 5");
        assertMapping(49, 103, '1', 4, "number row 1 through FreeJ2ME's phone layout");
        assertMapping(57, 99, '9', 4, "number row 9 through FreeJ2ME's phone layout");
        assertMapping(97, 103, '1', 4, "keypad 1 through FreeJ2ME's phone layout");

        if (BrowserKeyMapper.map(66) != null) {
            throw new AssertionError("unbound browser keys must not reach the MIDlet");
        }
    }

    private static void assertMapping(int browserKey, int awtKey, char character, int location, String label) {
        BrowserKeyMapper.Mapping mapping = BrowserKeyMapper.map(browserKey);
        if (mapping == null || mapping.keyCode != awtKey || mapping.character != character ||
                mapping.location != location) {
            throw new AssertionError(label + " mapping mismatch");
        }
    }
}
