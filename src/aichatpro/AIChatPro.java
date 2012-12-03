package aichatpro;

import aichatpro.helper.FileHelper;
import aichatpro.model.Dictionary;
import java.io.*;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import spark.*;
import static spark.Spark.*;

public class AIChatPro {
    public static void main(String[] args) {
        setPort(port);

        // Index
        get(new Route("/") {
            @Override
            public Object handle(Request request, Response response) {
                response.type("text/html");
                return FileHelper.ReadFile("static/html/hello.html");
            }
        });

        // ajax chat
        // controller to processing chat input from user
        post(new Route("/chat") {
           @Override
           public Object handle(Request request, Response response) {
               String chat = request.queryParams("chat");

               Dictionary dict = new Dictionary(Dictionary.KNUTH_MORRIS_PRATT);
               dict.readSynonymFromFile("D:/synonym.txt");
               dict.readFAQFromFile("D:/test.txt");

               response.type("text/plain");
               return dict.answer(chat);
           }
        });

        // Static file handler
        // Mengambil file text static atau binary file
        get(new Route("/static/:dir/:filename") {
           @Override
           public Object handle(Request request, Response response) {
                String filename = String.format("static/%s/%s",
                    request.params(":dir"), request.params(":filename"));

                // untuk file binary
                if (request.params(":dir").equals("img")) {
                    byte bytes[] = FileHelper.ReadFileBinary(filename);
                    if (bytes == null) {
                        return null;
                    }

                    HttpServletResponse rawRes = response.raw();
                    try {
                        ServletOutputStream out = rawRes.getOutputStream();
                        out.write(bytes, 0, bytes.length);
                    } catch (Exception ex) {
                    }

                    return rawRes;
                }

                if (request.params(":dir").equals("js")) {
                    response.type("application/javascript");
                }

                if (request.params(":dir").equals("css")) {
                    response.type("text/css");
                }

               // untuk bukan binary
               return FileHelper.ReadFile(filename);
           }
        });
    }

    private static int port = 8777;
}
