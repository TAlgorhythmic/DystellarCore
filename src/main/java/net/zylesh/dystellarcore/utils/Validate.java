package net.zylesh.dystellarcore.utils;

public class Validate {

    private static final String validateNameRegex = "^[a-zA-Z0-9!@#$%^&*()\\-_=+\\[{\\]};:'\",.<>/?]{1,16}$";
    private static final String validateSignRegex = "^[a-zA-Z0-9!@#$%^&*()\\-_=+ \\[{\\]};:'\",.<>/?]{0,16}$";

    public static boolean validateName(String s) {
        return !s.contains(" ") && s.matches(validateNameRegex);
    }

    public static boolean validateSign(String s) {
        return s.matches(validateSignRegex);
    }
}
