package studio.lineage2.cms.model;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import java.util.Collection;

/**
 Created by iRock
 17.10.2015
 */
@Entity
@Table(name = "users")
public class User implements UserDetails
{
	@Id
	@GeneratedValue(generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	@Column(name = "id")
	private long id;

	@NotEmpty(message = "{registration.empty_username}")
	@Email(message = "{registration.invalid_username}")
	@Column(name = "username")
	private String username;

	@Size(min = 8, message = "{registration.invalid_password}")
	@Column(name = "password")
	private String password;

	@Column(name = "is_admin")
	private boolean isAdmin;

	@Column(name = "active")
	private boolean active;

	public User()
	{
	}

	public long getId()
	{
		return id;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public boolean isActive()
	{
		return active;
	}

	public void setActive(boolean active)
	{
		this.active = active;
	}

	public boolean getIsAdmin()
	{
		return isAdmin;
	}

	public void setIsAdmin(boolean isAdmin)
	{
		this.isAdmin = isAdmin;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities()
	{
		if(getIsAdmin())
		{
			return AuthorityUtils.createAuthorityList("ROLE_USER", "ROLE_ADMIN");
		}
		return AuthorityUtils.createAuthorityList("ROLE_USER");
	}

	@Override
	public String getUsername()
	{
		return username;
	}

	@Override
	public String getPassword()
	{
		return password;
	}

	@Override
	public boolean isAccountNonExpired()
	{
		return true;
	}

	@Override
	public boolean isAccountNonLocked()
	{
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired()
	{
		return true;
	}

	@Override
	public boolean isEnabled()
	{
		return true;
	}
}