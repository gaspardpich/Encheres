package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import bo.ArticleVendu;
import bo.Categorie;
import bo.Enchere;
import bo.Utilisateur;
import jdbc.JDBCTools;

public class EnchereDAO {

	private static final String INSERT_ENCHERE = "insert into encheres(no_utilisateur, no_article, date_enchere, montant_enchere) values (?,?,?,?)";
	private static final String SELECT_ENCHERE = "select count(*) as nbre  from ENCHERES where no_utilisateur = ? and no_article = ?;";
	private static final String UPDATE_ENCHERE = "update encheres set date_enchere = ?, montant_enchere = ? where no_utilisateur = ? and no_article = ?;";
	private static final String SELECT_ENCHERE_EN_COURS = "SELECT * FROM ENCHERES WHERE date_enchere BETWEEN (SELECT MIN(date_debut_encheres) FROM ARTICLES_VENDUS) AND (SELECT MAX(date_fin_encheres) FROM ARTICLES_VENDUS)";
	private static final String SELECT_ENCHERE_EN_COURS_BY_ID = "SELECT * FROM ENCHERES "
			+ "WHERE date_enchere BETWEEN (SELECT MIN(date_debut_encheres) FROM ARTICLES_VENDUS) "
			+ "AND (SELECT MAX(date_fin_encheres) FROM ARTICLES_VENDUS) AND no_utilisateur =?";
	private static final String SELECT_ENCHERE_REMPORTEE = "SELECT * FROM ENCHERES JOIN ARTICLES_VENDUS ON ENCHERES.no_article = ARTICLES_VENDUS.no_article WHERE montant_enchere = prix_vente AND ENCHERES.no_utilisateur = ?";
	private static final String FILTRAGE_CATEGORIE = "select e.* from ARTICLES_VENDUS a "
			+ " left join ENCHERES e on e.no_article = a.no_article "
			+ " where a.no_categorie = ? and a.nom_article like ? ";
	private static final String FILTRAGE_SANS_CATEGORIE = "select e.* from ARTICLES_VENDUS a "
			+ " left join ENCHERES e on e.no_article = a.no_article where a.nom_article like ? ";

	public static void ajouter(Enchere enchere) throws SQLException, ClassNotFoundException {
		Connection cnx = null;
		PreparedStatement rqt = null;
		if (enchere.achatPossible()) {
			if (verifPremiereEnchere(enchere)) {
				try {
					cnx = JDBCTools.getConnection();
					rqt = cnx.prepareStatement(INSERT_ENCHERE);
					rqt.setInt(1, enchere.getNoUtilisateur().getNoUtilisateur());
					rqt.setInt(2, enchere.getNoArticle().getNoArticle());
					rqt.setDate(3, enchere.getDateEnchere());
					rqt.setInt(4, enchere.getMontant_enchere());
					rqt.executeUpdate();
				} finally {
					if (rqt != null)
						rqt.close();
					if (cnx != null)
						cnx.close();
				}
			} else {
				try {
					cnx = JDBCTools.getConnection();
					rqt = cnx.prepareStatement(UPDATE_ENCHERE);
					rqt.setInt(1, enchere.getNoUtilisateur().getNoUtilisateur());
					rqt.setInt(2, enchere.getNoArticle().getNoArticle());
					rqt.setDate(3, enchere.getDateEnchere());
					rqt.setInt(4, enchere.getMontant_enchere());
					rqt.executeUpdate();
				} finally {
					if (rqt != null)
						rqt.close();
					if (cnx != null)
						cnx.close();
				}
			}
		}
	}

	public static Boolean verifPremiereEnchere(Enchere enchere) throws SQLException, ClassNotFoundException {
		Connection cnx = null;
		PreparedStatement rqt = null;
		ResultSet rs = null;
		Boolean bool = false;
		try {
			cnx = JDBCTools.getConnection();
			rqt = cnx.prepareStatement(SELECT_ENCHERE);
			rqt.setInt(1, enchere.getNoUtilisateur().getNoUtilisateur());
			rqt.setInt(2, enchere.getNoArticle().getNoArticle());
			rs = rqt.executeQuery();
			if (rs.next()) {
				if (rs.getInt("nbre") == 0) {
					bool = true;
				}
			}
		} finally {
			if (rs != null)
				rs.close();
			if (rqt != null)
				rqt.close();
			if (cnx != null)
				cnx.close();
		}
		return bool;
	}

	/**
	 * Methode pour retourner les encheres en cours
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<Enchere> selectEncheresEnCours() throws SQLException {
		List<Enchere> encheres = new ArrayList<>();
		PreparedStatement preparedStatement = null;
		Connection conSelectAll = null;
		ResultSet rs = null;

		try {
			conSelectAll = JDBCTools.getConnection();
			preparedStatement = conSelectAll.prepareStatement(SELECT_ENCHERE_EN_COURS);
			rs = preparedStatement.executeQuery();
			ArticleVenduDAO articleDAO = new ArticleVenduDAO();
			UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
			// On parcourt le resultat de la requete et on cr�e les objets li�s � l'ench�re
			while (rs.next()) {
				ArticleVendu articleVendu = articleDAO.getArticleById(rs.getInt("no_article"));
				Utilisateur utilisateur = utilisateurDAO.getUtilisateurById(rs.getInt("no_utilisateur"));
				Enchere e = new Enchere(articleVendu, utilisateur, rs.getDate("date_enchere"),
						rs.getInt("montant_enchere"));
				encheres.add(e);
			}

		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (rs != null)
				rs.close();
			if (preparedStatement != null)
				preparedStatement.close();
			if (conSelectAll != null)
				conSelectAll.close();
		}
		return encheres;
	}

	/**
	 * Methode pour retourner les encheres en cours de l'utilisateur
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<Enchere> selectEncheresUtilisateur(int idutilisateur) throws SQLException {
		List<Enchere> encheres = new ArrayList<>();
		PreparedStatement preparedStatement = null;
		Connection conSelect = null;
		ResultSet rs = null;

		try {
			conSelect = JDBCTools.getConnection();
			preparedStatement = conSelect.prepareStatement(SELECT_ENCHERE_EN_COURS_BY_ID);
			preparedStatement.setInt(1, idutilisateur);
			rs = preparedStatement.executeQuery();
			ArticleVenduDAO articleDAO = new ArticleVenduDAO();
			UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
			// On parcourt le resultat de la requete et on cr�e les objets li�s � l'ench�re
			while (rs.next()) {
				ArticleVendu articleVendu = articleDAO.getArticleById(rs.getInt("no_article"));
				Utilisateur utilisateur = utilisateurDAO.getUtilisateurById(rs.getInt("no_utilisateur"));
				Enchere e = new Enchere(articleVendu, utilisateur, rs.getDate("date_enchere"),
						rs.getInt("montant_enchere"));
				encheres.add(e);
			}

		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (rs != null)
				rs.close();
			if (preparedStatement != null)
				preparedStatement.close();
			if (conSelect != null)
				conSelect.close();
		}
		return encheres;
	}
	
	/**
	 * Methode pour retourner les encheres remport�es de l'utilisateur
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<Enchere> selectEncheresRemporteesUtilisateur(int idutilisateur) throws SQLException {
		List<Enchere> encheres = new ArrayList<>();
		PreparedStatement preparedStatement = null;
		Connection conSelect = null;
		ResultSet rs = null;

		try {
			conSelect = JDBCTools.getConnection();
			preparedStatement = conSelect.prepareStatement(SELECT_ENCHERE_REMPORTEE);
			preparedStatement.setInt(1, idutilisateur);
			rs = preparedStatement.executeQuery();
			ArticleVenduDAO articleDAO = new ArticleVenduDAO();
			UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
			// On parcourt le resultat de la requete et on cr�e les objets li�s � l'ench�re
			while (rs.next()) {
				ArticleVendu articleVendu = articleDAO.getArticleById(rs.getInt("no_article"));
				Utilisateur utilisateur = utilisateurDAO.getUtilisateurById(rs.getInt("no_utilisateur"));
				Enchere e = new Enchere(articleVendu, utilisateur, rs.getDate("date_enchere"),
						rs.getInt("montant_enchere"));
				encheres.add(e);
			}

		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (rs != null)
				rs.close();
			if (preparedStatement != null)
				preparedStatement.close();
			if (conSelect != null)
				conSelect.close();
		}
		return encheres;
	}

	public List<Enchere> filtrageVenteEnCours(String contient, String categorie)
			throws SQLException, ClassNotFoundException {

		ArrayList<Enchere> encheres = new ArrayList<>();
		Connection conFiltre = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		System.out.println(categorie);
		try {
			conFiltre = JDBCTools.getConnection();

			// Si la cat�gorie est non vide, on prend la requ�te classique
			if (!"".equals(categorie) && categorie != null) {
				preparedStatement = conFiltre.prepareStatement(FILTRAGE_CATEGORIE);
				preparedStatement.setInt(1, Categorie.getNoByName(categorie));
				preparedStatement.setString(2, "%" + contient.trim() + "%");
			}
			// Sinon on prend la version sans la cat�gorie
			else if (contient != null) {
				preparedStatement = conFiltre.prepareStatement(FILTRAGE_SANS_CATEGORIE);
				preparedStatement.setString(1, "%" + contient.trim() + "%");
			} else {
				preparedStatement = conFiltre.prepareStatement(SELECT_ENCHERE_EN_COURS);
			}

			rs = preparedStatement.executeQuery();

			while (rs.next()) {
				int identifiantUtilisateur = rs.getInt("no_utilisateur");
				int identifiantArticle = rs.getInt("no_article");
				UtilisateurDAO utDAO = new UtilisateurDAO();
				ArticleVenduDAO avDAO = new ArticleVenduDAO();
				Utilisateur ut = utDAO.getUtilisateurById(identifiantUtilisateur);
				ArticleVendu av = avDAO.getArticleById(identifiantArticle);
				Enchere enchere = new Enchere(av, ut, rs.getDate("date_enchere"), rs.getInt("montant_enchere"));

				// On ajoute l'enchere � la liste
				encheres.add(enchere);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (preparedStatement != null)
				preparedStatement.close();
			if (conFiltre != null)
				conFiltre.close();
		}

		return encheres;
	}
}
