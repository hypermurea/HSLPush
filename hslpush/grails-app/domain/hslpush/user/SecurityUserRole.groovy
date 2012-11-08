package hslpush.user

import org.apache.commons.lang.builder.HashCodeBuilder

class SecurityUserRole implements Serializable {

	SecurityUser securityUser
	Role role

	boolean equals(other) {
		if (!(other instanceof SecurityUserRole)) {
			return false
		}

		other.securityUser?.id == securityUser?.id &&
			other.role?.id == role?.id
	}

	int hashCode() {
		def builder = new HashCodeBuilder()
		if (securityUser) builder.append(securityUser.id)
		if (role) builder.append(role.id)
		builder.toHashCode()
	}

	static SecurityUserRole get(long securityUserId, long roleId) {
		find 'from SecurityUserRole where securityUser.id=:securityUserId and role.id=:roleId',
			[securityUserId: securityUserId, roleId: roleId]
	}

	static SecurityUserRole create(SecurityUser securityUser, Role role, boolean flush = false) {
		new SecurityUserRole(securityUser: securityUser, role: role).save(flush: flush, insert: true)
	}

	static boolean remove(SecurityUser securityUser, Role role, boolean flush = false) {
		SecurityUserRole instance = SecurityUserRole.findBySecurityUserAndRole(securityUser, role)
		if (!instance) {
			return false
		}

		instance.delete(flush: flush)
		true
	}

	static void removeAll(SecurityUser securityUser) {
		executeUpdate 'DELETE FROM SecurityUserRole WHERE securityUser=:securityUser', [securityUser: securityUser]
	}

	static void removeAll(Role role) {
		executeUpdate 'DELETE FROM SecurityUserRole WHERE role=:role', [role: role]
	}

	static mapping = {
		id composite: ['role', 'securityUser']
		version false
	}
}
