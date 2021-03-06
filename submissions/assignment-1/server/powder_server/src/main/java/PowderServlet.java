import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;

public class PowderServlet extends javax.servlet.http.HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        ServletContext context = getServletContext();
        res.setContentType("application/json");
        String urlPath = req.getPathInfo();

        context.log("post");

        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("{'message':'missing parameters'}");
            return;
        }

        String[] urlParts = urlPath.split("/");
        if (!isUrlValidPost(urlParts)) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            res.setStatus(HttpServletResponse.SC_ACCEPTED);
            // do any sophisticated processing with urlParts which contains all the url params
            // TODO: process url params in `urlParts`
            res.getWriter().write("{'message':'called writeNewLiftRide'}");
        }
        return;
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        ServletContext context = getServletContext();
        res.setContentType("application/json");
        String urlPath = req.getPathInfo();

        context.log("get");

        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("{'message':'missing parameters'}");
            return;
        }

        String[] urlParts = urlPath.split("/");
        if (urlParts.length == 0) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("{'message':'missing parameters'}");
            return;
        }

        if (urlParts[1].equals("resort")) {
            if (urlParts.length == 4
                    && urlParts[2].equals("day")
                    && urlParts[3].equals("top10vert")
            ) {
                res.getWriter().write("{'message':'called getTopTenVert'}");
                String queryString = req.getQueryString();
                // TODO: process qs
                // res.getWriter().write(queryString);

                return;
            }
        }
        else if (urlParts[1].equals("skiers")) {
            if (urlParts.length == 4
//                    && urlParts[2] in skierIDs
                    && urlParts[3].equals("vertical")
            ) {
                res.setStatus(HttpServletResponse.SC_OK);
                res.getWriter().write("{'message':'called SkierDayVertical'}");
                return;
            }
            else if (urlParts.length == 7
//                    && urlParts[2] in resortIDs
                    && urlParts[3].equals("days")
//                    && urlParts[4] in dayIDs
                    && urlParts[5].equals("skiers")
//                    && urlParts[6] in skiersIDs
            ) {
                res.getWriter().write("{'message':'called SkierResortTotals'}");
                return;
            }
        }

        // default to invalid url
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return;

    }

    private boolean isUrlValidPost(String[] urlParts) {
        // TODO: validate the request url path according to the API spec
        if (urlParts.length == 3
                && urlParts[1].equals("skiers")
                && urlParts[2].equals("liftrides")) {
            return true;
        }
        return false;
    }
}
