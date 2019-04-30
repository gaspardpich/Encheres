package bo;

public enum EtatVenteEnchere {

	VEC("vec","Vente en cours"),
	VND("vnd","Vente non d�but�e"),
	VET("vet","vente termin�e"),
	ENO("eno","ench�re ouverte"),
	ENC("enc","ench�re en cours"),
	ENR("enr","ench�re termin�e");
	
	private String nom;
	private String description;
	//Constructeur
	EtatVenteEnchere(String nom, String description){
		this.nom = nom;
		this.description = description;
	}
	public String getNom() {
		return nom;
	}
	public void setNom(String nom) {
		this.nom = nom;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	
}
