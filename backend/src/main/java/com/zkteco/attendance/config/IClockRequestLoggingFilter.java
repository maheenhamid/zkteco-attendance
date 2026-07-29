package com.zkteco.attendance.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Logs every request under /iclock/** (method, full URL incl. query string,
 * raw body, AND Spring/Tomcat's already-parsed parameter map) at INFO level.
 * Temporary diagnostic aid for figuring out exactly which endpoints/parameters
 * a specific F18 firmware actually calls, since ADMS implementations vary
 * across firmware versions.
 *
 * The parameter map is logged separately from the raw body because
 * ContentCachingRequestWrapper does NOT reliably capture form-urlencoded POST
 * bodies - the servlet container parses those into request.getParameterMap()
 * directly, often without ever going through the wrapper's own input stream,
 * so relying on the raw body alone can show "empty" even when real data was
 * successfully bound to @RequestParam arguments.
 */
@Component
@Slf4j
public class IClockRequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (!request.getRequestURI().startsWith("/iclock")) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper wrapped = new ContentCachingRequestWrapper(request);
        filterChain.doFilter(wrapped, response);

        String query = wrapped.getQueryString();
        String body = new String(wrapped.getContentAsByteArray(), StandardCharsets.UTF_8);
        String contentType = wrapped.getContentType();
        String params = wrapped.getParameterMap().entrySet().stream()
                .map(e -> e.getKey() + "=" + String.join(",", e.getValue()))
                .collect(Collectors.joining("&"));

        log.info("[ICLOCK-RAW] {} {}{} status={} contentType=[{}] rawBody=[{}] parsedParams=[{}]",
                wrapped.getMethod(), wrapped.getRequestURI(),
                query != null ? "?" + query : "", response.getStatus(), contentType, body, params);
    }
}
