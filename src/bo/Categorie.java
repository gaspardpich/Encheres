package bo;

public enum Categorie {
	// Objets directement construits
	INFO(1, "Informatique"), AMEU(2, "Ameublement"), VETE(3, "V�tement"), SPOR(4, "Sport&Loisirs");

	private int noCategorie = -1;
	private String name = "";

	// Constructeur
	Categorie(int noCategorie, String name) {
		this.name = name;
		this.noCategorie = noCategorie;
	}

	public String toString() {
		return name;
	}

	public int getNoCategorie() {
		return noCategorie;
	}

	public void setNoCategorie(int noCategorie) {
		this.noCategorie = noCategorie;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static int getNoByName(String name) {
		int noCat = -1;

		switch (name) {
		case "Informatique":
			noCat = 1;
			break;
		case "Ameublement":
			noCat = 2;
			break;
		case "V�tement":
			noCat = 3;
			break;
		case "Sport&Loisirs":
			noCat = 4;
			break;
		default:
			break;
		}

		return noCat;
	}

}
