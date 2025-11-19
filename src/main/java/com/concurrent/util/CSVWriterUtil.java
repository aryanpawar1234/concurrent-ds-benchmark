package com.concurrent.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CSVWriterUtil {

    private final PrintWriter writer;

    public CSVWriterUtil(String filePath) throws IOException {
        this.writer = new PrintWriter(new FileWriter(filePath));
    }

    public void writeHeader(String... columns) {
        writer.println(String.join(",", columns));
    }

    public void writeRow(Object... values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            sb.append(values[i]);
            if (i < values.length - 1) sb.append(",");
        }
        writer.println(sb.toString());
    }

    public void close() {
        writer.close();
    }
}
