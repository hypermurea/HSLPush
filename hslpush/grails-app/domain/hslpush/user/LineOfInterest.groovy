package hslpush.user

class LineOfInterest {
	
	String code
	int transportType

	static belongsTo = [user:GcmUser]
	
    static constraints = {
    }
}
