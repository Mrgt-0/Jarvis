package com.jarvis.Analyzer.PMD;
import jakarta.activation.DataSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

class StringDataSource implements DataSource {
    private final String name;
    private final String content;

    public StringDataSource(String name, String content) {
        this.name = name;
        this.content = content;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        // Для PMD анализа нам нужен только InputStream
        // Возвращаем пустой OutputStream или выбрасываем исключение
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                throw new IOException("Output not supported for StringDataSource");
            }
        };
    }

    @Override
    public String getContentType() {
        return "";
    }

    @Override
    public String getName() {
        return name;
    }
}