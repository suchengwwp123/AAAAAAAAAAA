package com.example.authority.config.wrapper;



import com.example.authority.utils.XssCleaner;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class XssRequestWrapper extends HttpServletRequestWrapper {

    private byte[] body;

    public XssRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        String json = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);

        if (json != null && !json.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> map = mapper.readValue(json, Map.class);
                Map<String, Object> cleaned = cleanMap(map);
                this.body = mapper.writeValueAsBytes(cleaned);
            } catch (Exception e) {
                this.body = json.getBytes(StandardCharsets.UTF_8);
            }
        } else {
            this.body = new byte[0];
        }
    }

    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream bais = new ByteArrayInputStream(body);
        return new ServletInputStream() {
            @Override public boolean isFinished() { return bais.available() == 0; }
            @Override public boolean isReady() { return true; }
            @Override public void setReadListener(ReadListener listener) {}
            @Override public int read() { return bais.read(); }
        };
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }

    @Override
    public String getParameter(String name) {
        return XssCleaner.clean(super.getParameter(name));
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) return null;
        return Arrays.stream(values).map(XssCleaner::clean).toArray(String[]::new);
    }

    // 清洗 JSON 的 map
    private Map<String, Object> cleanMap(Map<String, Object> input) {
        Map<String, Object> cleaned = new HashMap<>();
        for (Map.Entry<String, Object> entry : input.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String) {
                cleaned.put(entry.getKey(), XssCleaner.clean((String) value));
            } else if (value instanceof Map) {
                cleaned.put(entry.getKey(), cleanMap((Map<String, Object>) value));
            } else if (value instanceof List) {
                cleaned.put(entry.getKey(), cleanList((List<?>) value));
            } else {
                cleaned.put(entry.getKey(), value);
            }
        }
        return cleaned;
    }

    private List<Object> cleanList(List<?> list) {
        List<Object> cleaned = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof String) {
                cleaned.add(XssCleaner.clean((String) item));
            } else if (item instanceof Map) {
                cleaned.add(cleanMap((Map<String, Object>) item));
            } else {
                cleaned.add(item);
            }
        }
        return cleaned;
    }
}
