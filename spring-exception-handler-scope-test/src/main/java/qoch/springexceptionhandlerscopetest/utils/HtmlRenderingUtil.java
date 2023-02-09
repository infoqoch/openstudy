package qoch.springexceptionhandlerscopetest.utils;

import org.springframework.core.io.ClassPathResource;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class HtmlRenderingUtil {
    public static void staticViewWriter(HttpServletResponse response, String path) {
        response.setContentType("text/html; charset=utf-8");

        ClassPathResource classPathResource = new ClassPathResource(path);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(classPathResource.getInputStream()));
             PrintWriter writer = response.getWriter();) {

            String line = null;
            while ((line = br.readLine()) != null) {
                writer.println(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}