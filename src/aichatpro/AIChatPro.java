package aichatpro;

import java.util.ArrayList;
import aichatpro.helper.FileHelper;
import aichatpro.model.Dictionary;
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
               String algo = request.queryParams("algo");
               String history = request.queryParams("history");
               String s = history.concat("\nYou:" + chat + "\nAIChatPRO:");
               response.type("text/plain");

               Dictionary dict;
               if(algo.equals("KMP")) {
                   dict = new Dictionary(Dictionary.KNUTH_MORRIS_PRATT);
               } else {
                   dict = new Dictionary(Dictionary.BOOYER_MOORE);
               }
               dict.readSynonymFromFile("D:/synonym.txt");
               dict.readFAQFromFile("D:/FAQ.txt");
               dict.readStopwordsFromFile("D:/stopwords.txt");
               if(temp.contains(chat)) {
                   return s.concat(dict.justAnswer(chat));
               }
               ArrayList<String> answer = dict.answer(chat);
               ArrayList<Integer> conf = dict.getConfidence();
               System.out.println(answer.size());
               if(answer.size() == 1) {
                   return s.concat(answer.get(0) + " (" + conf.get(0).toString() + "%)\n");
               }
               if(answer.isEmpty()) {
                   return s.concat("Kami tidak tahu apa yang ada tanyakan\n");
               }
               temp = answer;
               s = s.concat("Apakah yang anda maksud?\n");
               for(int i = 0; i < answer.size(); i++) {
                   if(answer.get(i) != null) {
                       s = s.concat(answer.get(i) + " (" + conf.get(i).toString() + "%)\n");
                   }
               }
               return s;
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
    private static ArrayList<String> temp = new ArrayList<String>();
}
