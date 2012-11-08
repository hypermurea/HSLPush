package hslpush.user

class SecurityUser {

	transient springSecurityService

	String username
	String password
	boolean enabled
	boolean accountExpired
	boolean accountLocked
	boolean passwordExpired
	
	String pushoverId
	int lastReportedDisruptionId
	String linesOfInterest

	static constraints = {
		username blank: false, unique: true
		password blank: false
		
		pushoverId nullable:true
		linesOfInterest nullable:true
	}

	static mapping = {
		password column: '`password`'
	}

	Set<Role> getAuthorities() {
		SecurityUserRole.findAllBySecurityUser(this).collect { it.role } as Set
	}

	def beforeInsert() {
		encodePassword()
	}

	def beforeUpdate() {
		if (isDirty('password')) {
			encodePassword()
		}
	}

	protected void encodePassword() {
		password = springSecurityService.encodePassword(password)
	}
}
