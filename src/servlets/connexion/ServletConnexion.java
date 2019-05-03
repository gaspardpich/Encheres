package servlets.connexion;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bo.Utilisateur;
import dal.UtilisateurDAO;

/**
 * Servlet qui servira � se connecter au site.
 * 
 * @author adeloffre2018
 *
 */
public class ServletConnexion extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final int CINQ_MINUTES = 5 * 60;
	private static final int SE_SOUVENIR = 7 * 24 * 60 * 60; // On se souvient de l'utilisateur pendant 7 jours.

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			doProcess(request, response);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected void doProcess(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {
		UtilisateurDAO DAOIdentification;
		String identifiant = request.getParameter("identifiant");
		String motDePasse = request.getParameter("motdepasse");
		Utilisateur utilisateur = null;
		HttpSession session = request.getSession();

		Cookie ck = null;

		DAOIdentification = new UtilisateurDAO();
		utilisateur = DAOIdentification.verifIdentification(identifiant, motDePasse);
		ck = new Cookie("connexion", "-1");

		if (utilisateur != null) {
			// le cookie est valide 10 minutes
			ck.setValue(utilisateur.getPseudo());
			// Si on n'a pas coch� se souvenir de moi
			if ("on".equals(request.getParameter("remember-me"))) {
				ck.setMaxAge(SE_SOUVENIR);
			} else {
				ck.setMaxAge(CINQ_MINUTES);
			}

			// Si on a coch� , il faudra mettre SE_SOUVENIR pour �tre m�moris� 30
			// jours
			session.setAttribute("utilisateur", utilisateur);
			// On ajoute le cookie
			response.addCookie(ck);
			this.getServletContext().getRequestDispatcher("/filtre").forward(request, response);
		} else {
			// Les identifiants sont incorrects, on laisse le cookie à -1
			response.addCookie(ck);
			request.setAttribute("error", "connexionerror");
			this.getServletContext().getRequestDispatcher("/connexion.jsp").forward(request, response);
		}
	}
}
