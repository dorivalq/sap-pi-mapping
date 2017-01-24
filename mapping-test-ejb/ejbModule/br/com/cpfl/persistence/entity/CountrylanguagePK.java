package br.com.cpfl.persistence.entity;

import java.io.Serializable;

/**
 * The primary key class for the countrylanguage database table.
 * 
 */
@Embeddable
public class CountrylanguagePK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@Column(insertable=false, updatable=false, unique=true, nullable=false, length=3)
	private String countryCode;

	@Column(unique=true, nullable=false, length=30)
	private String language;

	public CountrylanguagePK() {
	}
	public String getCountryCode() {
		return this.countryCode;
	}
	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}
	public String getLanguage() {
		return this.language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof CountrylanguagePK)) {
			return false;
		}
		CountrylanguagePK castOther = (CountrylanguagePK)other;
		return 
			this.countryCode.equals(castOther.countryCode)
			&& this.language.equals(castOther.language);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.countryCode.hashCode();
		hash = hash * prime + this.language.hashCode();
		
		return hash;
	}
}