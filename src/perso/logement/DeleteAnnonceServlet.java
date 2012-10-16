package perso.logement;

import static java.lang.Integer.parseInt;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class DeleteAnnonceServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(DeleteAnnonceServlet.class.getName());

  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    int year = parseInt(req.getParameter("year"));
    int month = parseInt(req.getParameter("month"));
    int day = parseInt(req.getParameter("day"));
    try {
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.YEAR, year);
      cal.set(Calendar.MONTH, month - 1);
      cal.set(Calendar.DAY_OF_MONTH, day);
      cal.set(Calendar.HOUR_OF_DAY, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MILLISECOND, 0);
      Query query = pm.newQuery(Annonce.class, "date >= startDate");
      query.declareParameters("java.util.Date startDate");
      List<Annonce> annonces = (List<Annonce>) query.execute(cal.getTime());
      log.log(Level.INFO, "deleting " + annonces.size() + " annonces");
      pm.deletePersistentAll(annonces);
    } finally {
      pm.close();
    }
    resp.setContentType("text/plain");
    resp.getWriter().println("Hello, world");
  }
}
