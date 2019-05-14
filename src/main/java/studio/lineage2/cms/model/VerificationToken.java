package studio.lineage2.cms.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

/**
 Created by iRock
 02.11.2015
 */
@Entity
@Table(name = "verification", indexes = { @Index(name = "idx_token", columnList = "token") })
public class VerificationToken {
	private static final int EXPIRATION = 60 * 24;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "token")
	private String token;

	@Column(name = "expire")
	private Date expiryDate;

	@OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
	@JoinColumn(nullable = false, name = "user_id")
	private User user;

	private Date calculateExpiryDate(int expiryTimeInMinutes) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Timestamp(cal.getTime().getTime()));
		cal.add(Calendar.MINUTE, expiryTimeInMinutes);
		return new Date(cal.getTime().getTime());
	}

	public VerificationToken()
	{}

	public VerificationToken(String token, User user)
	{
		this.user = user;
		this.token = token;
		this.expiryDate = calculateExpiryDate(EXPIRATION);
	}

	public User getUser() {
		return user;
	}

	public String getToken() {
		return token;
	}

	public Date getExpiryDate() {
		return expiryDate;
	}
	// standard constructors, getters and setters
}