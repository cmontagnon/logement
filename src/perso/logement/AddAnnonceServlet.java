package perso.logement;

import java.io.IOException;
import java.util.Date;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class AddAnnonceServlet extends HttpServlet {

  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    String reference = req.getParameter("reference");
    String text = req.getParameter("text");
    double prix = Double.parseDouble(req.getParameter("prix"));
    double superficie = Double.parseDouble(req.getParameter("superficie"));
    Date date = new Date();
    short arrondissement = Short.parseShort(req.getParameter("arrondissement"));
    String quartier = req.getParameter("quartier");
    try {
      Annonce annonce = new Annonce(reference, text, prix, superficie, date, arrondissement, quartier);
      pm.makePersistent(annonce);
    } finally {
      pm.close();
    }
    resp.setContentType("text/plain");
    resp.getWriter().println("Hello, world");
  }
}
