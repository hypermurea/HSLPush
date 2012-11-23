package hslpush

import grails.converters.JSON
import groovy.json.JsonSlurper
import hslpush.user.GcmUser
import hslpush.user.LineOfInterest

class GcmController {

	def gcmService

	def signal() {
		
				GcmUser.findAll().each { user ->
					gcmService.sendMessage(user, "test signal from hslpush")
				}
		
				render "done."
			}
	
	def index() {

		def user = GcmUser.findByUuid(params.uuid)
		def lineList = new JsonSlurper().parseText(params.lof)

		if(user == null) {
			addNewUser(params.uuid, params.regId, lineList)
		} else {
			updateUser(user, params.regId, lineList)
		}

		render(status: 200 , text: "OK")
	}
	
	def private addNewUser(uuid, registrationId, lineList) {
		GcmUser user = new GcmUser(uuid: uuid, registrationId: registrationId)
		user.linesOfInterest = []

		lineList.each { line ->
			line.codes.each { code ->
				LineOfInterest lof = new LineOfInterest(code: code, transportType: line.transportType, user: user)
				user.linesOfInterest << lof
			}
		}
		
		user.save()
	}
	
	def private updateUser(GcmUser user, registrationId, lineList) {
		def lineHash = [:]
		lineList.each { line ->
			line.codes.each { code ->
				lineHash[code] = line
			}
		}
		
		user.linesOfInterest.each { lineOfInterest ->
			if(lineHash.containsKey(lineOfInterest.code)) {
				if(lineHash[lineOfInterest.code].transportType != lineOfInterest.transportType) {
					lineOfInterest.transportType = lineHash[lineOfInterest.code].transportType
				}
				lineHash.remove(lineOfInterest.code)
			} else {
				user.linesOfInterest.remove(lineOfInterest)
				lineOfInterest.delete()
			}
		}
		
		lineHash.each { code, line ->
			LineOfInterest newLof = new LineOfInterest(code:code, transportType: line.transportType)
			user.linesOfInterest << newLof
		}

		user.registrationId = registrationId
		user.save()
	}

}
