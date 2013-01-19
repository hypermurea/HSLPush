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

		log.error "user " + params.uuid + " with linesofinterest: " + params.lof
		
		if(user == null) {
			log.error "creating new user" 
			addNewUser(params.uuid, params.regId, lineList)
		} else {
			log.error "updating user"
			updateUser(user, params.regId, lineList)
		}

		render(status: 200 , text: "OK")
	}

	def private addNewUser(uuid, registrationId, lineList) {
		GcmUser user = new GcmUser(uuid: uuid, registrationId: registrationId)
		user.linesOfInterest = []
		
		// TODO limit number of lines to prevent abuse

		'[{"transportType":5,"name":"Elielinaukio-Latokaski","code":"2195","shortCode":"195"},{"transportType":5,"name":"Elielinaukio-Tapiola","code":"2194","shortCode":"194"}]'
		
		
		
		lineList.each { line ->
			LineOfInterest lof = new LineOfInterest(
				code: line.code, shortCode: line.shortCode,
				transportType: line.transportType, name: line.name, user: user)
			user.linesOfInterest << lof
		}

		user.save()
	}

	def private updateUser(GcmUser user, registrationId, lineList) {
		
		// TODO Limit number of lines to prevent abuse
		
		def lineHash = [:]
		lineList.each { line ->
			lineHash[line.code] = line
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
			LineOfInterest newLof = new LineOfInterest(
				code: code, shortCode: line.shortCode, transportType: line.transportType, name: line.name, user: user)
			user.linesOfInterest << newLof
		}

		user.registrationId = registrationId
		user.save()
	}

}
