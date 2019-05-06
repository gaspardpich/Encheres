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
	private static final String ENCHERE_EXISTANTE = "select * from ENCHERES where no_utilisateur = ? and no_article = ?;";
	private static final String UPDATE_ENCHERE = "update encheres set date_enchere = ?, montant_enchere = ? where no_utilisateur = ? and no_article = ?;";
	private static final String SELECT_ENCHERE_EN_COURS = "SELECT * FROM ENCHERES INNER JOIN "
			+ "(SELECT no_article,MAX(montant_enchere) as enchereMax FROM ENCHERES GROUP BY no_article) as topscore "
			+ "ON ENCHERES.no_article = topscore.no_article JOIN ARTICLES_VENDUS "
			+ "ON ENCHERES.no_article = ARTICLES_VENDUS.no_article "
			+ "AND ENCHERES.montant_enchere = topscore.enchereMax "
			+ "AND date_enchere BETWEEN (SELECT MIN(date_debut_encheres) FROM ARTICLES_VENDUS) "
			+ "AND (SELECT MAX(date_fin_encheres) FROM ARTICLES_VENDUS) "
			+ "order by ARTICLES_VENDUS.date_fin_encheres ASC";
	private static final String SELECT_ENCHERE_EN_COURS_BY_ID = "SELECT * FROM ENCHERES "
			+ "WHERE date_enchere BETWEEN (SELECT MIN(date_debut_encheres) FROM ARTICLES_VENDUS) "
			+ "AND (SELECT MAX(date_fin_encheres) FROM ARTICLES_VENDUS) AND no_utilisateur =?";
	private static final String SELECT_ENCHERE_REMPORTEE = "SELECT * FROM ENCHERES JOIN ARTICLES_VENDUS ON ENCHERES.no_article = ARTICLES_VENDUS.no_article WHERE montant_enchere = prix_vente AND ENCHERES.no_utilisateur = ?";
	private static final String FILTRAGE_CATEGORIE = "SELECT * FROM ENCHERES INNER JOIN "
			+ "(SELECT no_article,MAX(montant_enchere) as enchereMax FROM ENCHERES GROUP BY no_article) as topscore "
			+ "ON ENCHERES.no_article = topscore.no_article JOIN ARTICLES_VENDUS "
			+ "ON ENCHERES.no_article = ARTICLES_VENDUS.no_article "
			+ "AND ENCHERES.montant_enchere = topscore.enchereMax "
			+ "AND date_enchere BETWEEN (SELECT MIN(date_debut_encheres) FROM ARTICLES_VENDUS) "
			+ "AND (SELECT MAX(date_fin_encheres) FROM ARTICLES_VENDUS) "
			+ "AND ENCHERES.no_article in (SELECT ARTICLES_VENDUS.no_article FROM ARTICLES_VENDUS WHERE no_categorie = ?) "
			+ "AND ENCHERES.no_article in (SELECT ARTICLES_VENDUS.no_article FROM ARTICLES_VENDUS WHERE nom_article like ?) "
			+ "order by ARTICLES_VENDUS.date_fin_encheres ASC";
	private static final String FILTRAGE_CATEGORIE_CONNECTE = "SELECT * FROM ENCHERES INNER JOIN "
			+ "(SELECT no_article,MAX(montant_enchere) as enchereMax FROM ENCHERES GROUP BY no_article) as topscore "
			+ "ON ENCHERES.no_article = topscore.no_article JOIN ARTICLES_VENDUS "
			+ "ON ENCHERES.no_article = ARTICLES_VENDUS.no_article "
			+ "AND ENCHERES.montant_enchere = topscore.enchereMax "
			+ "AND ENCHERES.no_article in (SELECT ARTICLES_VENDUS.no_article FROM ARTICLES_VENDUS WHERE no_categorie = ?) "
			+ "AND ENCHERES.no_article in (SELECT ARTICLES_VENDUS.no_article FROM ARTICLES_VENDUS WHERE nom_article like ?) "
			+ "order by ARTICLES_VENDUS.date_fin_encheres ASC";
	private static final String FILTRAGE_SANS_CATEGORIE = "SELECT * FROM ENCHERES INNER JOIN "
			+ "(SELECT no_article,MAX(montant_enchere) as enchereMax FROM ENCHERES GROUP BY no_article) as topscore "
			+ "ON ENCHERES.no_article = topscore.no_article JOIN ARTICLES_VENDUS "
			+ "ON ENCHERES.no_article = ARTICLES_VENDUS.no_article "
			+ "AND ENCHERES.montant_enchere = topscore.enchereMax "
			+ "AND date_enchere BETWEEN (SELECT MIN(date_debut_encheres) FROM ARTICLES_VENDUS) "
			+ "AND (SELECT MAX(date_fin_encheres) FROM ARTICLES_VENDUS) "
			+ "AND ENCHERES.no_article in (SELECT ARTICLES_VENDUS.no_article FROM ARTICLES_VENDUS WHERE nom_article like ?) "
			+ "order by ARTICLES_VENDUS.date_fin_encheres ASC";
	private static final String FILTRAGE_SANS_CATEGORIE_CONNECTE = "SELECT * FROM ENCHERES INNER JOIN "
			+ "(SELECT no_article,MAX(montant_enchere) as enchereMax FROM ENCHERES GROUP BY no_article) as topscore "
			+ "ON ENCHERES.no_article = topscore.no_article JOIN ARTICLES_VENDUS "
			+ "ON ENCHERES.no_article = ARTICLES_VENDUS.no_article "
			+ "AND ENCHERES.montant_enchere = topscore.enchereMax "
			+ "AND ENCHERES.no_article in (SELECT ARTICLES_VENDUS.no_article FROM ARTICLES_VENDUS WHERE nom_article like ?) "
			+ "order by ARTICLES_VENDUS.date_fin_encheres ASC";
	private static final String SELECT_ENCHERE_MAX = "SELECT MAX(montant_enchere) AS enchereMax FROM ENCHERES WHERE no_article = ?";

	/**
	 * Ajout d'une ench�re
	 * 
	 * @param enchere
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public static void ajouter(Enchere enchere) throws SQLException, ClassNotFoundException {
		Connection cnx = null;
		PreparedStatement rqt = null;
		java.sql.Date datSql = new java.sql.Date(enchere.getDateEnchere().getTime());
		if (verifPremiereEnchere(enchere)) {
			try {
				cnx = JDBCTools.getConnection();
				rqt = cnx.prepareStatement(INSERT_ENCHERE);
				rqt.setInt(1, enchere.getNoUtilisateur().getNoUtilisateur());
				rqt.setInt(2, enchere.getNoArticle().getNoArticle());
				rqt.setDate(3, datSql);
				rqt.setInt(4, enchere.getMontantEnchere());
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
				rqt.setDate(3, datSql);
				rqt.setInt(4, enchere.getMontantEnchere());
				rqt.executeUpdate();
			} finally {
				if (rqt != null)
					rqt.close();
				if (cnx != null)
					cnx.close();
			}
		}
	}

	/**
	 * Verifie si une enchere existe déjà sinon insert de celle ci
	 * 
	 * @param enchere
	 * @return
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
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
	 * V�rifie si l'enchere est bien sup�rieure aux ench�res existantes
	 * 
	 * @param enchere
	 * @return
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public static int verifEnchereSup(Enchere enchere) throws SQLException, ClassNotFoundException {
		Connection cnx = null;
		PreparedStatement rqt = null;
		ResultSet rs = null;
		int enchereMax = 0;
		try {
			cnx = JDBCTools.getConnection();
			rqt = cnx.prepareStatement(SELECT_ENCHERE_MAX);
			rqt.setInt(1, enchere.getNoArticle().getNoArticle());
			rs = rqt.executeQuery();
			if (rs.next()) {

				enchereMax = rs.getInt("enchereMax");
			}
		} finally {
			if (rs != null)
				rs.close();
			if (rqt != null)
				rqt.close();
			if (cnx != null)
				cnx.close();
		}
		return enchereMax;
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
			// On parcourt le resultat de la requete et on cr�e les objets li�s �
			// l'ench�re
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
			// On parcourt le resultat de la requete et on cr�e les objets li�s �
			// l'ench�re
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
			// On parcourt le resultat de la requete et on cr�e les objets li�s �
			// l'ench�re
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
	 * Méthode permettant de filtrer la liste des enchères en mode déconnecté
	 * @param contient
	 * @param categorie
	 * @return
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public List<Enchere> filtrageEncheresDeconnecte(String contient, String categorie)
			throws SQLException, ClassNotFoundException {

		ArrayList<Enchere> encheres = new ArrayList<>();
		Connection conFiltre = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		try {
			conFiltre = JDBCTools.getConnection();

			// Si la cat�gorie est non vide, on prend la requ�te classique
			if (!Categorie.ALL.getName().equals(categorie) && categorie != null) {
				preparedStatement = conFiltre.prepareStatement(FILTRAGE_CATEGORIE);
				preparedStatement.setInt(1, Categorie.getNoByName(categorie));
				preparedStatement.setString(2, "%" + contient.trim() + "%");
			}
			// Sinon on prend la version avec toutes les catégories
			else if (Categorie.ALL.getName().equals(categorie)) {
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
	
	/**
	 * Méthode permettant de filtrer la liste des enchères en mode connecté
	 * Dans ce cas, on ne prend pas que les encheres en cours.
	 * @param contient
	 * @param categorie
	 * @return
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public List<Enchere> filtrageEncheresConnecte(String contient, String categorie)
			throws SQLException, ClassNotFoundException {

		ArrayList<Enchere> encheres = new ArrayList<>();
		Connection conFiltre = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		try {
			conFiltre = JDBCTools.getConnection();

			// Si la catégorie est non vide, on prend la requête classique
			if (!Categorie.ALL.getName().equals(categorie) && categorie != null) {
				preparedStatement = conFiltre.prepareStatement(FILTRAGE_CATEGORIE_CONNECTE);
				preparedStatement.setInt(1, Categorie.getNoByName(categorie));
				preparedStatement.setString(2, "%" + contient.trim() + "%");
			}
			// Sinon on prend la version avec toutes les catégories
			else if (Categorie.ALL.getName().equals(categorie)) {
				preparedStatement = conFiltre.prepareStatement(FILTRAGE_SANS_CATEGORIE_CONNECTE);
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

	/**
	 * Verification de l'existance d'une enchere pour un utilisateur sur un article
	 * donnée avec d'autres paramètres
	 * 
	 * @param enchere
	 * @return
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public static Enchere enchereExistante(int noUtilisateur, int noArticle)
			throws SQLException, ClassNotFoundException {
		Connection cnx = null;
		PreparedStatement rqt = null;
		ResultSet rs = null;
		Enchere enchere = null;
		try {
			cnx = JDBCTools.getConnection();
			rqt = cnx.prepareStatement(ENCHERE_EXISTANTE);
			rqt.setInt(1, noUtilisateur);
			rqt.setInt(2, noArticle);
			rs = rqt.executeQuery();
			if (rs.next()) {
				UtilisateurDAO utDAO = new UtilisateurDAO();
				Utilisateur ut = utDAO.getUtilisateurById(rs.getInt("no_utilisateur"));
				ArticleVenduDAO avDAO = new ArticleVenduDAO();
				ArticleVendu av = avDAO.getArticleById(rs.getInt("no_article"));
				enchere = new Enchere(av, ut, rs.getDate("date_enchere"), rs.getInt("montant_enchere"));
			}
		} finally {
			if (rs != null)
				rs.close();
			if (rqt != null)
				rqt.close();
			if (cnx != null)
				cnx.close();
		}
		return enchere;
	}

	/**
	 * V�rifie si l'enchere est bien sup�rieure aux ench�res existantes
	 * 
	 * @param enchere
	 * @return
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public static int getEnchereMax(int noArticle) throws SQLException, ClassNotFoundException {
		Connection cnx = null;
		PreparedStatement rqt = null;
		ResultSet rs = null;
		int enchereMax = 0;
		try {
			cnx = JDBCTools.getConnection();
			rqt = cnx.prepareStatement(SELECT_ENCHERE_MAX);
			rqt.setInt(1, noArticle);
			rs = rqt.executeQuery();
			if (rs.next()) {

				enchereMax = rs.getInt("enchereMax");
			}
		} finally {
			if (rs != null)
				rs.close();
			if (rqt != null)
				rqt.close();
			if (cnx != null)
				cnx.close();
		}
		return enchereMax;
	}

	public static ArrayList<Enchere> listerAchatEnCours(int noUtilisateur, String checkbox1, String checkbox2,
			String checkbox3) throws SQLException, ClassNotFoundException {
		Connection cnx1 = null, cnx2 = null, cnx3 = null;
		PreparedStatement rqt1 = null, rqt2 = null, rqt3 = null;
		ResultSet rs1 = null, rs2 = null, rs3 = null;
		ArrayList<Enchere> listeEncheresAll = new ArrayList<>();
		ArrayList<Enchere> listeEncheresEnCours = new ArrayList<>();
		ArrayList<Enchere> listeMesEncheresEnCours = new ArrayList<>();
		ArrayList<Enchere> listeEncheresRemportees = new ArrayList<>();

		try {
			if ("on".equals(checkbox1)) {
				cnx1 = JDBCTools.getConnection();
				rqt1 = cnx1.prepareStatement(SELECT_ENCHERE_EN_COURS);
				rs1 = rqt1.executeQuery();
				Utilisateur ut = null;
				UtilisateurDAO utDAO = new UtilisateurDAO();
				ArticleVendu articleVendu = null;
				ArticleVenduDAO articleDAO = new ArticleVenduDAO();
				while (rs1.next()) {
					ut = utDAO.getUtilisateurById(noUtilisateur);
					articleVendu = articleDAO.getArticleById(rs1.getInt("no_article"));
					Enchere enchere = new Enchere(articleVendu, ut, rs1.getDate("date_enchere"),
							rs1.getInt("montant_enchere"));
					// On ajoute l'article � la liste
					listeEncheresEnCours.add(enchere);
				}
				// Doublons impossibles à cette étape
				listeEncheresAll.addAll(listeEncheresEnCours);
			}
			if ("on".equals(checkbox2)) {
				cnx2 = JDBCTools.getConnection();
				rqt2 = cnx2.prepareStatement(SELECT_ENCHERE_EN_COURS_BY_ID);
				rqt2.setInt(1, noUtilisateur);
				rs2 = rqt2.executeQuery();
				Utilisateur ut = null;
				UtilisateurDAO utDAO = new UtilisateurDAO();
				ArticleVendu articleVendu = null;
				ArticleVenduDAO articleDAO = new ArticleVenduDAO();
				while (rs2.next()) {
					ut = utDAO.getUtilisateurById(noUtilisateur);
					articleVendu = articleDAO.getArticleById(rs2.getInt("no_article"));
					Enchere enchere = new Enchere(articleVendu, ut, rs2.getDate("date_enchere"),
							rs2.getInt("montant_enchere"));
					// On ajoute l'article � la liste
					listeMesEncheresEnCours.add(enchere);
				}
				// On retire les doublons même si théoriquement il n'y en aura pas
				for (Enchere enchere : listeEncheresAll) {
					for (Enchere enchere2 : listeMesEncheresEnCours) {
						if (enchere.getNoArticle() == enchere2.getNoArticle()
								&& enchere.getNoUtilisateur() == enchere2.getNoUtilisateur()) {
							listeEncheresAll.remove(enchere);
						}
					}
				}
				listeEncheresAll.addAll(listeMesEncheresEnCours);
			}
			if ("on".equals(checkbox3)) {
				cnx3 = JDBCTools.getConnection();
				rqt3 = cnx3.prepareStatement(SELECT_ENCHERE_REMPORTEE);
				rqt3.setInt(1, noUtilisateur);
				rs3 = rqt3.executeQuery();
				Utilisateur ut = null;
				UtilisateurDAO utDAO = new UtilisateurDAO();
				ArticleVendu articleVendu = null;
				ArticleVenduDAO articleDAO = new ArticleVenduDAO();
				while (rs3.next()) {
					ut = utDAO.getUtilisateurById(noUtilisateur);
					articleVendu = articleDAO.getArticleById(rs3.getInt("no_article"));
					Enchere enchere = new Enchere(articleVendu, ut, rs3.getDate("date_enchere"),
							rs3.getInt("montant_enchere"));
					// On ajoute l'article � la liste
					listeMesEncheresEnCours.add(enchere);
				}
				// On retire les doublons même si théoriquement il n'y en aura pas
				for (Enchere enchere : listeEncheresAll) {
					for (Enchere enchere2 : listeEncheresRemportees) {
						if (enchere.getNoArticle().getNoArticle() == enchere2.getNoArticle().getNoArticle()
								&& enchere.getNoUtilisateur().getNoUtilisateur() == enchere2.getNoUtilisateur()
										.getNoUtilisateur()) {
							listeEncheresAll.remove(enchere);
						}
					}
				}
				listeEncheresAll.addAll(listeEncheresRemportees);
			}
		} finally {
			if (rs1 != null)
				rs1.close();
			if (rqt1 != null)
				rqt1.close();
			if (cnx1 != null)
				cnx1.close();
			if (rs2 != null)
				rs2.close();
			if (rqt2 != null)
				rqt2.close();
			if (cnx2 != null)
				cnx2.close();
			if (rs3 != null)
				rs3.close();
			if (rqt3 != null)
				rqt3.close();
			if (cnx3 != null)
				cnx3.close();
		}

		return listeEncheresAll;
	}
}
