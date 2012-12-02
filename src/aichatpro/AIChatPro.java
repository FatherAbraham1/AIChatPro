package aichatpro;

import aichatpro.helper.FileHelper;
import java.io.*;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import spark.*;
import static spark.Spark.*;

public class AIChatPro {
    public static void main(String[] args) {
        /*if (args.length > 0) {
            port = Integer.parseInt(args[1]);
        }*/

        setPort(port);

        // Index
        get(new Route("/") {
            @Override
            public Object handle(Request req, Response res) {
                res.type("text/html");
                return FileHelper.ReadFile("static/html/hello.html");
            }
        });

        // Test ajax
        // Reverse string foo
        post(new Route("/chat") {
           @Override
           public Object handle(Request req, Response res) {
               String chat = req.queryParams("chat");

               StringBuilder str = new StringBuilder();
               for (int i = chat.length() - 1; i >= 0; i--) {
                  str.append(chat.charAt(i));
               }

               res.type("text/plain");
               return str;
           }
        });

        // Static file handler
        // Get static text files (css/js) and or binary files
        get(new Route("/static/:dir/:filename") {
           @Override
           public Object handle(Request req, Response res) {
                String filename = String.format("static/%s/%s",
                    req.params(":dir"), req.params(":filename"));

                // binary file
                if (req.params(":dir").equals("img")) {
                    byte bytes[] = FileHelper.ReadFileBinary(filename);
                    if (bytes == null) {
                        return null;
                    }

                    HttpServletResponse rawRes = res.raw();
                    try {
                        ServletOutputStream out = rawRes.getOutputStream();
                        out.write(bytes, 0, bytes.length);
                    } catch (IOException ex) {

                    }

                    return rawRes;
                }

                if (req.params(":dir").equals("css")) {
                    res.type("text/css");
                }

                if (req.params(":dir").equals("js")) {
                    res.type("application/javascript");
                }

               // else, non-binary:
               return FileHelper.ReadFile(filename);
           }
        });
    }

    private static int port = 8777;
}
