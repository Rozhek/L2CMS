package studio.lineage2.cms.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

/**
 Created by iRock
 02.11.2015
 */
@Entity
@Table(name = "bonus")
public class Bonus
{
	private static final int EXPIRATION = 60 * 24 * 30;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "code", nullable = false, unique = true)
	private String code;

	@Column(name = "value", nullable = false)
	private long value;

	@Column(name = "expire")
	private Date expiryDate;

	@Column(name = "activate_by_user")
	private long userId;

	private Date calculateExpiryDate(int expiryTimeInMinutes) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Timestamp(cal.getTime().getTime()));
		cal.add(Calendar.MINUTE, expiryTimeInMinutes);
		return new Date(cal.getTime().getTime());
	}

	public Bonus()
	{}

	public Bonus(String code, long value)
	{
		this.code = code;
		this.value = value;
		this.expiryDate = calculateExpiryDate(EXPIRATION);
	}

	public void activate(long userId) {
		this.userId = userId;
	}

	public boolean isActivated() {
		return userId != 0;
	}

	public String getCode() {
		return code;
	}

	public long getValue() {
		return value;
	}

	public Date getExpiryDate() {
		return expiryDate;
	}
	// standard constructors, getters and setters
	public long getUserId() {
		return userId;
	}
}