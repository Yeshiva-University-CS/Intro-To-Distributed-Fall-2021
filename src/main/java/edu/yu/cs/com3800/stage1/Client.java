package edu.yu.cs.com3800.stage1;

import java.io.IOException;

public interface Client {
    //public ClientImpl(String hostName, int hostPort) throws MalformedURLException
    class Response {
        private int code;
        private String body;

        public Response(int code, String body) {
            this.code = code;
            this.body = body;
        }

        public int getCode() {
            return this.code;
        }

        public String getBody() {
            return this.body;
        }
    }

    void sendCompileAndRunRequest(String src) throws IOException;
    Response getResponse() throws IOException;
}