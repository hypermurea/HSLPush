package hslpush.user

class LineOfInterest {
	
	String code
	String shortCode
	String name
	int transportType

	static belongsTo = [user:GcmUser]
	
    static constraints = {
    }
}
