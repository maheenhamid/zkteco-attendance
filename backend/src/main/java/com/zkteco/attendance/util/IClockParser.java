package com.zkteco.attendance.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Parses the tab/line-delimited body ZKTeco devices POST to /iclock/cdata. */
public final class IClockParser {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private IClockParser() {
    }

    public static List<AttLogEntry> parseAttLog(String body) {
        List<AttLogEntry> entries = new ArrayList<>();
        if (body == null || body.trim().isEmpty()) {
            return entries;
        }

        for (String rawLine : body.split("\\r?\\n")) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] fields = line.split("\t");
            if (fields.length < 2) {
                continue;
            }
            try {
                String pin = fields[0].trim();
                LocalDateTime time = LocalDateTime.parse(fields[1].trim(), TIME_FORMAT);
                String status = fields.length > 2 ? fields[2].trim() : null;
                String verify = fields.length > 3 ? fields[3].trim() : null;
                entries.add(new AttLogEntry(pin, time, status, verify, line));
            } catch (Exception ignored) {
                // Malformed line from the device - skip it rather than fail the whole batch.
            }
        }
        return entries;
    }

    @Getter
    @AllArgsConstructor
    public static class AttLogEntry {
        private final String enrollNo;
        private final LocalDateTime punchTime;
        private final String punchType;
        private final String verifyMode;
        private final String rawLine;
    }

    /**
     * Parses the tab-separated "KEY=VALUE" lines a device sends when pushing its
     * local USERINFO table back to the server (used to "pull" users enrolled
     * directly on the device - see {@code IClockService.pullUsersFromDevice}).
     * Example line: {@code PIN=1\tName=John Doe\tPri=0\tPasswd=\tCard=12345\tGrp=1}
     */
    public static List<UserInfoEntry> parseUserInfo(String body) {
        List<UserInfoEntry> entries = new ArrayList<>();
        if (body == null || body.trim().isEmpty()) {
            return entries;
        }

        for (String rawLine : body.split("\\r?\\n")) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }

            Map<String, String> fields = new LinkedHashMap<>();
            for (String token : line.split("\t")) {
                int eq = token.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                fields.put(token.substring(0, eq).trim().toUpperCase(), token.substring(eq + 1).trim());
            }

            String pin = fields.get("PIN");
            if (pin == null || pin.isEmpty()) {
                continue;
            }

            String name = fields.getOrDefault("NAME", "PIN " + pin);
            String pri = fields.getOrDefault("PRI", "0");
            String card = fields.get("CARD");

            int priCode;
            try {
                priCode = Integer.parseInt(pri);
            } catch (NumberFormatException e) {
                priCode = 0;
            }

            entries.add(new UserInfoEntry(pin, name, priCode, card, line));
        }
        return entries;
    }

    @Getter
    @AllArgsConstructor
    public static class UserInfoEntry {
        private final String enrollNo;
        private final String name;
        private final int privilegeCode;
        private final String cardNo;
        private final String rawLine;
    }

    /**
     * Parses user records out of an OPERLOG push. Some F18 firmware never
     * sends a dedicated USERINFO table at all - it reports both enrolled
     * users AND fingerprint templates as OPERLOG lines instead, prefixed
     * with a record-type tag before the first tab-separated KEY=VALUE pair:
     * {@code USER PIN=3\tName=Mona\tPri=0\tCard=\t...} (user record) or
     * {@code FP PIN=3\tFID=6\tSize=1580\tValid=1\tTMP=<base64 template>} (a
     * fingerprint template, which this system has no use for). Only "USER"
     * lines are extracted; everything else (FP, OPLOG, ...) is skipped.
     */
    public static List<UserInfoEntry> parseOperLogUsers(String body) {
        List<UserInfoEntry> entries = new ArrayList<>();
        if (body == null || body.trim().isEmpty()) {
            return entries;
        }

        for (String rawLine : body.split("\\r?\\n")) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }

            String[] tabFields = line.split("\t");
            if (tabFields.length == 0) {
                continue;
            }

            // The first tab-separated field is "<RECORD_TYPE> <FIRST_KEY>=<VALUE>",
            // e.g. "USER PIN=3" - space-separated, not tab-separated.
            String[] head = tabFields[0].trim().split(" ", 2);
            if (head.length < 2 || !"USER".equalsIgnoreCase(head[0].trim())) {
                continue;
            }

            Map<String, String> fields = new LinkedHashMap<>();
            putKeyValue(fields, head[1]);
            for (int i = 1; i < tabFields.length; i++) {
                putKeyValue(fields, tabFields[i]);
            }

            String pin = fields.get("PIN");
            if (pin == null || pin.isEmpty()) {
                continue;
            }

            String name = fields.getOrDefault("NAME", "PIN " + pin);
            String pri = fields.getOrDefault("PRI", "0");
            String card = fields.get("CARD");

            int priCode;
            try {
                priCode = Integer.parseInt(pri);
            } catch (NumberFormatException e) {
                priCode = 0;
            }

            entries.add(new UserInfoEntry(pin, name, priCode, card, line));
        }
        return entries;
    }

    private static void putKeyValue(Map<String, String> fields, String token) {
        int eq = token.indexOf('=');
        if (eq <= 0) {
            return;
        }
        fields.put(token.substring(0, eq).trim().toUpperCase(), token.substring(eq + 1).trim());
    }

    /**
     * Parses an "&"-joined KEY=VALUE body, e.g. {@code ID=10&Return=0&CMD=DATA UPDATE USERINFO}.
     * Some F18 firmware posts the /iclock/devicecmd result this way while mislabeling the
     * Content-Type as application/octet-stream, so the servlet container never auto-populates
     * request parameters from it - this has to be parsed from the raw body manually instead.
     */
    public static Map<String, String> parseFormBody(String body) {
        Map<String, String> result = new LinkedHashMap<>();
        if (body == null || body.trim().isEmpty()) {
            return result;
        }

        for (String pair : body.trim().split("&")) {
            int eq = pair.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            String key = pair.substring(0, eq).trim();
            String value = decode(pair.substring(eq + 1).trim());
            result.put(key, value);
        }
        return result;
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException | IllegalArgumentException e) {
            return value;
        }
    }
}
