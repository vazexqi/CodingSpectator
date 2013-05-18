package edu.illinois.codingtracker.codeskimmer;

import java.util.List;

import edu.illinois.codingtracker.operations.UserOperation;

/**
 * 
 * @author Connor Simmons
 *
 */
public class UsernameFilter extends Filter {

	private String username;

	public UsernameFilter(String username) {
		this.setUsername(username);
	}

	@Override
	public void init(List<UserOperation> operations) {
	}

	@Override
	public boolean matchesOperation(UserOperation operation) {
		return operation.getUsername() != null
				&& operation.getUsername().equals(username);
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UsernameFilter other = (UsernameFilter) obj;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}
}
