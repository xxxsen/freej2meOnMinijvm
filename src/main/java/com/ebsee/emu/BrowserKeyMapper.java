package com.ebsee.emu;

/** Maps DOM/GLFM key codes to the AWT key bindings consumed by FreeJ2ME. */
final class BrowserKeyMapper {
    private static final int SHIFT = 1;
    private static final int LOCATION_STANDARD = 0;
    private static final int LOCATION_NUMPAD = 4;
    private static final char UNDEFINED = '\uffff';

    private BrowserKeyMapper() {
    }

    static Mapping map(int browserKey) {
        return map(browserKey, 0);
    }

    static Mapping map(int browserKey, int modifiers) {
        if ((modifiers & SHIFT) != 0) {
            if (browserKey == 56) return new Mapping(69, '*', LOCATION_NUMPAD);
            if (browserKey == 51) return new Mapping(82, '#', LOCATION_NUMPAD);
        }

        switch (browserKey) {
            case 8: return standard(65);       // Backspace -> CLR
            case 13: return standard(10);      // DOM Enter -> AWT Enter
            case 37:
            case 65: return standard(37);      // Left / A
            case 38:
            case 87: return standard(38);      // Up / W
            case 39:
            case 68: return standard(39);      // Right / D
            case 40:
            case 83: return standard(40);      // Down / S
            case 48: return number(96, '0');
            case 49: return number(103, '1');
            case 50: return number(104, '2');
            case 51: return number(105, '3');
            case 52: return number(100, '4');
            case 53: return number(101, '5');
            case 54: return number(102, '6');
            case 55: return number(97, '7');
            case 56: return number(98, '8');
            case 57: return number(99, '9');
            case 81:
            case 112: return new Mapping(81, 'q', LOCATION_STANDARD);  // Q / F1 -> left soft
            case 69:
            case 113: return new Mapping(87, 'w', LOCATION_STANDARD);  // E / F2 -> right soft
            case 82:
            case 111: return number(82, '#');                           // R / numpad divide
            case 96: return number(96, '0');
            case 97: return number(103, '1');
            case 98: return number(104, '2');
            case 99: return number(105, '3');
            case 100: return number(100, '4');
            case 101: return number(101, '5');
            case 102: return number(102, '6');
            case 103: return number(97, '7');
            case 104: return number(98, '8');
            case 105: return number(99, '9');
            case 106: return number(69, '*');                            // numpad multiply
            default: return null;
        }
    }

    private static Mapping standard(int keyCode) {
        return new Mapping(keyCode, UNDEFINED, LOCATION_STANDARD);
    }

    private static Mapping number(int keyCode, char character) {
        return new Mapping(keyCode, character, LOCATION_NUMPAD);
    }

    static final class Mapping {
        final int keyCode;
        final char character;
        final int location;

        Mapping(int keyCode, char character, int location) {
            this.keyCode = keyCode;
            this.character = character;
            this.location = location;
        }
    }
}
