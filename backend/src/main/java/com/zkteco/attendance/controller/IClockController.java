package com.zkteco.attendance.controller;

import com.zkteco.attendance.service.IClockService;
import com.zkteco.attendance.util.IClockParser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ZKTeco F18 ADMS/iClock device protocol endpoints. No JWT - devices
 * authenticate purely via the SN query parameter, per the ADMS spec.
 * All responses are text/plain, exactly as the device firmware expects.
 */
@RestController
@RequestMapping("/iclock")
@RequiredArgsConstructor
public class IClockController {

    private final IClockService iClockService;

    @GetMapping(value = "/cdata", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> handshake(@RequestParam("SN") String serialNumber,
                                             @RequestParam(value = "options", required = false) String options) {
        return ResponseEntity.ok(iClockService.handshake(serialNumber));
    }

    @PostMapping(value = "/cdata", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> pushData(@RequestParam("SN") String serialNumber,
                                            @RequestParam(value = "table", required = false) String table,
                                            @RequestBody(required = false) String body) {
        return ResponseEntity.ok(iClockService.receiveData(serialNumber, table, body));
    }

    @GetMapping(value = "/getrequest", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getRequest(@RequestParam("SN") String serialNumber) {
        return ResponseEntity.ok(iClockService.getPendingCommand(serialNumber));
    }

    /**
     * The real ADMS spec has the device report command execution results on
     * POST /iclock/devicecmd (not an endpoint name we invented) - some
     * firmware also GETs it with the same query params, so both methods are
     * accepted. "/device-command-result" is kept mapped to the same handler
     * for backward compatibility with anything already pointed at it.
     *
     * Some F18 firmware puts ID/Return/CMD in the raw POST body but labels it
     * Content-Type: application/octet-stream instead of the usual form-encoded
     * type - the servlet container then never auto-populates @RequestParam from
     * it, so the body is read and parsed manually here as a fallback whenever
     * the query-string values aren't present.
     */
    @RequestMapping(value = {"/devicecmd", "/device-command-result"}, method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> commandResult(@RequestParam("SN") String serialNumber,
                                                 @RequestParam(value = "ID", required = false) Long queryCommandId,
                                                 @RequestParam(value = "Return", required = false) Integer queryReturnCode,
                                                 @RequestParam(value = "CMD", required = false) String queryCmd,
                                                 @RequestBody(required = false) String body) {
        Map<String, String> bodyParams = IClockParser.parseFormBody(body);

        Long commandId = queryCommandId != null ? queryCommandId : parseLong(bodyParams.get("ID"));
        Integer returnCode = queryReturnCode != null ? queryReturnCode : parseInt(bodyParams.get("Return"));
        String cmd = queryCmd != null ? queryCmd : bodyParams.get("CMD");

        return ResponseEntity.ok(iClockService.recordCommandResult(serialNumber, commandId, returnCode, cmd));
    }

    private static Long parseLong(String value) {
        try {
            return value == null ? null : Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer parseInt(String value) {
        try {
            return value == null ? null : Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
