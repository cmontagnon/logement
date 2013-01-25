package perso.logement;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import perso.logement.core.Annonce;

@SuppressWarnings("serial")
public class AddAnnonceServlet extends HttpServlet {

  private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    String reference = req.getParameter("reference");
    String text = req.getParameter("text");
    double prix = Double.parseDouble(req.getParameter("prix"));
    double superficie = Double.parseDouble(req.getParameter("superficie"));
    try {
      Date date;
      date = formatter.parse(req.getParameter("date"));
      short arrondissement = Short.parseShort(req.getParameter("arrondissement"));
      String quartier = req.getParameter("quartier");
      Annonce annonce = new Annonce(reference, text, prix, superficie, date, arrondissement, quartier);
      pm.makePersistent(annonce);
    } catch (ParseException e) {
      e.printStackTrace();
    } finally {
      pm.close();
    }
    resp.setContentType("text/plain");
    resp.getWriter().println("Hello, world");
  }
}
