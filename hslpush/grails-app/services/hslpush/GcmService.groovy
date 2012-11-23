package hslpush

import com.google.android.gcm.server.*
import hslpush.user.GcmUser

class GcmService {

	def grailsApplication
	
    def sendMessage(user, message) {
		log.error("got here")
		def sender = new Sender(grailsApplication.config.gcm.api.key)
		def DISRUPTION_DESCRIPTION = "desc"
		def messageFrame = new Message.Builder().addData(DISRUPTION_DESCRIPTION, message).build()
		def result = sender.send(messageFrame, user.registrationId, grailsApplication.config.gcm.send.retries.toInteger())
		
		if(result.getMessageId() != null) {
			if(result.getCanonicalRegistrationId() != null) {
				log.error "canonical registration id provided, changing entry"
				gcmUser.registrationId = result.getCanonicalRegistrationId()
				gcmUser.save()
			}
		} else {
			def error = result.getErrorCodeName()
			if(error == Constants.ERROR_NOT_REGISTERED) {
				log.error "User not registered with '" + user.registrationId + ", deleting account info." 
				//gcmUser.delete()
			} else {
				log.error "error occurred, identifier: " + result.getErrorCodeName()
			}
		}
		log.error "finished."
    }
	
}
