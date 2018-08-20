package by.epam.task4.model;

import java.util.Date;

public class Certificate {

	private String registredBy;
	private Date registrationDate;
	private Date expireDate;
	
	public String getRegistredBy() {
		return registredBy;
	}
	
	public void setRegistredBy(String registredBy) {
		this.registredBy = registredBy;
	}
	public Date getRegistredDate() {
		return registrationDate;
	}
	public void setRegistredDate(Date registredDate) {
		this.registrationDate = registredDate;
	}
	public Date getExpireDate() {
		return expireDate;
	}
	public void setExpireDate(Date expireDate) {
		this.expireDate = expireDate;
	}
}