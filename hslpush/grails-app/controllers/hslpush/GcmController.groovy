package hslpush

import grails.converters.JSON
import groovy.json.JsonSlurper
import hslpush.user.GcmUser
import hslpush.user.LineOfInterest

class GcmController {

	static allowedMethods = [signal:'GET', index:'POST']
	
	def gcmService
	
	def signal() {

		def signalThese = LineOfInterest.findAllByCodeAndTransportType(params.code, params.transportType) 
		
		signalThese.each {
			gcmService.sendMessage(it.user, "Line " + it.code + " signaled")
		}

		render (status: 200, text: "OK")
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
		
		// TODO limit number of lines to prevent abuse

		lineList.each { line ->
			line.codes.each { code ->
				LineOfInterest lof = new LineOfInterest(code: code, transportType: line.transportType, user: user)
				user.linesOfInterest << lof
			}
		}

		user.save()
	}

	def private updateUser(GcmUser user, registrationId, lineList) {
		
		// TODO Limit number of lines to prevent abuse
		
		def lineHash = [:]
		lineList.each { line ->
			line.codes.each { code ->
				lineHash[code] = line
			}
		}

		def toDelete = user.linesOfInterest.findAll { !lineHash.containsKey(it.code) }
		toDelete.each {
			lineHash.remove(it.code)
			user.linesOfInterest.remove(it)
			it.delete() 
		}
		
		def toUpdate = user.linesOfInterest.findAll { lineHash.containsKey(it.code) }
		user.linesOfInterest.each {
			if(it.transportType != lineHash[it.code].transportType) {
				it.transportType = lineHash[it.code].transportType
				it.save()
			}
			lineHash.remove(it.code)
		}
		
		lineHash.each { code, line ->
			LineOfInterest newLof = new LineOfInterest(code:code, transportType: line.transportType, user: user)
			user.linesOfInterest << newLof
		}

		user.registrationId = registrationId
		user.save()
	}

}
