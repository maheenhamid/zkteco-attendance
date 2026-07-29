package com.zkteco.attendance.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IClockParserTest {

    @Test
    void parsesWellFormedAttLogLines() {
        String body = "1\t2026-07-27 08:15:00\t0\t1\t0\t0\t0\t0\t0\n" +
                      "2\t2026-07-27 08:16:30\t1\t2\t0\t0\t0\t0\t0";

        List<IClockParser.AttLogEntry> entries = IClockParser.parseAttLog(body);

        assertEquals(2, entries.size());
        assertEquals("1", entries.get(0).getEnrollNo());
        assertEquals(LocalDateTime.of(2026, 7, 27, 8, 15, 0), entries.get(0).getPunchTime());
        assertEquals("0", entries.get(0).getPunchType());
        assertEquals("1", entries.get(0).getVerifyMode());
    }

    @Test
    void skipsMalformedLinesWithoutFailingTheBatch() {
        String body = "1\t2026-07-27 08:15:00\t0\t1\n" +
                      "garbage-line-no-tabs\n" +
                      "3\tnot-a-date\t0\t1\n" +
                      "\n" +
                      "4\t2026-07-27 09:00:00\t0\t1";

        List<IClockParser.AttLogEntry> entries = IClockParser.parseAttLog(body);

        assertEquals(2, entries.size());
        assertEquals("1", entries.get(0).getEnrollNo());
        assertEquals("4", entries.get(1).getEnrollNo());
    }

    @Test
    void emptyBodyProducesNoEntries() {
        assertTrue(IClockParser.parseAttLog(null).isEmpty());
        assertTrue(IClockParser.parseAttLog("  ").isEmpty());
    }
}
