package hslpush.user

class GcmUser {
	
	String uuid
	String registrationId
	int lastReportedDisruptionId
	
	static hasMany = [linesOfInterest: LineOfInterest]
    static constraints = {
		linesOfInterest cascade: 'all'
	}
	
}
