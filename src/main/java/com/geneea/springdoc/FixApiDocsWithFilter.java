package com.geneea.springdoc;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

@Configuration
@EnableWebMvc
@Profile("reformat_apidocs_filter")
public class FixApiDocsWithFilter implements WebMvcConfigurer {

    @Bean
    public FilterRegistrationBean<DocsFormatterFilter> loggingFilter() {
        FilterRegistrationBean<DocsFormatterFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new DocsFormatterFilter());
        registrationBean.addUrlPatterns("/api-docs");
        return registrationBean;
    }

    private static class DocsFormatterFilter implements Filter {

        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
            ByteResponseWrapper byteResponseWrapper = new ByteResponseWrapper((HttpServletResponse) servletResponse);
            ByteRequestWrapper byteRequestWrapper = new ByteRequestWrapper((HttpServletRequest) servletRequest);

            filterChain.doFilter(byteRequestWrapper, byteResponseWrapper);

            String jsonResponse = new String(byteResponseWrapper.getBytes(), servletResponse.getCharacterEncoding());
            String result = jsonResponse
                    .substring(1, jsonResponse.length() - 1)
                    .replaceAll("\\\\\"", "\"");
            servletResponse.getOutputStream().write(result.getBytes(servletResponse.getCharacterEncoding()));
        }

        static class ByteResponseWrapper extends HttpServletResponseWrapper {

            private PrintWriter writer;
            private ByteOutputStream output;

            public byte[] getBytes() {
                writer.flush();
                return output.getBytes();
            }

            public ByteResponseWrapper(HttpServletResponse response) {
                super(response);
                output = new ByteOutputStream();
                writer = new PrintWriter(output);
            }

            @Override
            public PrintWriter getWriter() {
                return writer;
            }

            @Override
            public ServletOutputStream getOutputStream() {
                return output;
            }
        }

        static class ByteRequestWrapper extends HttpServletRequestWrapper {

            byte[] requestBytes = null;
            private ByteInputStream byteInputStream;


            public ByteRequestWrapper(HttpServletRequest request) throws IOException {
                super(request);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                InputStream inputStream = request.getInputStream();

                byte[] buffer = new byte[4096];
                int read = 0;
                while ((read = inputStream.read(buffer)) != -1) {
                    baos.write(buffer, 0, read);
                }

                replaceRequestPayload(baos.toByteArray());
            }

            @Override
            public BufferedReader getReader() {
                return new BufferedReader(new InputStreamReader(getInputStream()));
            }

            @Override
            public ServletInputStream getInputStream() {
                return byteInputStream;
            }

            public void replaceRequestPayload(byte[] newPayload) {
                requestBytes = newPayload;
                byteInputStream = new ByteInputStream(new ByteArrayInputStream(requestBytes));
            }
        }

        static class ByteOutputStream extends ServletOutputStream {

            private ByteArrayOutputStream bos = new ByteArrayOutputStream();

            @Override
            public void write(int b) {
                bos.write(b);
            }

            public byte[] getBytes() {
                return bos.toByteArray();
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }
        }

        static class ByteInputStream extends ServletInputStream {

            private InputStream inputStream;

            public ByteInputStream(final InputStream inputStream) {
                this.inputStream = inputStream;
            }

            @Override
            public int read() throws IOException {
                return inputStream.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }
        }

    }
}
