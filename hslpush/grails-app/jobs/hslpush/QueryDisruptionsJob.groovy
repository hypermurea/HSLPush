package hslpush

import groovyx.net.http.HttpResponseException
import hslpush.user.GcmUser

class QueryDisruptionsJob {

	public static final GENERAL_DISRUPTION_CODE = 14

	static triggers = { simple repeatInterval: 30000l // execute job once in 30 seconds
	}

	def gcmService
	def pushoverService
	def hslDisruptionUrl = "http://www.poikkeusinfo.fi/xml/v2/fi"

	def execute() {

		//def HSL_DISRUPTION_URL = "http://www.poikkeusinfo.fi/xml/v2/fi/291020121800"
		//def HSL_DISRUPTION_URL = "http://www.poikkeusinfo.fi/xml/v2/fi"
		def disruptionReport = new XmlSlurper().parse(hslDisruptionUrl)
		log.info "disruptions in effect: " + disruptionReport.@valid

		if (disruptionReport.@valid.text().toInteger() > 0) {

			def disruptions = disruptionReport.DISRUPTION.findAll {
				it.VALIDITY.@status.text().toInteger() == 1
			}.list()
			disruptions.sort{
				it.@id.text().toInteger()
			}

			disruptions.each { disruption ->

				if(disruption.TARGETS.LINE.size() > 0) {
					log.error("line disruption to users")
					signalUsersInterestedInLine(disruption)
				} else
				if(disruption.TARGETS.LINETYPE.size() == 1) {
					log.error("linetype disruption to users")
					signalUsersInterestedInLineType(disruption)
				} else {
					log.error("signal all users")
					signalAllUsers(disruption)
				}

		}
	} else {
		log.info("No disruptions in effect")
	}
}

def signalUsersInterestedInLine(disruption) {
	def disruptionId = disruption.@id.text().toInteger()
	def lines = disruption.TARGETS.LINE.collect { line -> ["code": line.@id.text(), "linetype": line.@linetype.text().toInteger() ]}

	def interestedUsers = GcmUser.createCriteria().list {
		lt("lastReportedDisruptionId", disruptionId)
		linesOfInterest {
			or {
				lines.each { line ->
					and {
						eq("code", line["code"])
						eq("transportType", line["linetype"] )
					}
				}
			}
		}
	}

	sendMessages(disruption.@id.text().toInteger(), disruption.INFO.TEXT.text(), interestedUsers)
}

def signalUsersInterestedInLineType(disruption) {
	def disruptionId = disruption.@id.text().toInteger()
	def lineType = disruption.TARGETS.LINETYPE.@id.text().toInteger()
	
	if(lineType == GENERAL_DISRUPTION_CODE) {
		sendMessages(disruption, GcmUser.findAll { lastReportedDisruptionId < disruptionId } )
	} else {

		def interestedUsers = GcmUser.createCriteria().list {
			lt("lastReportedDisruptionId", disruptionId)
			linesOfInterest {
				eq("transportType", lineType)
			}
		}
		log.error("interested users: " + interestedUsers.size())
		sendMessages(disruptionId, disruption.INFO.TEXT.text(), interestedUsers)
	}
}

def signalAllUsers(disruption) {
	def disruptionId = disruption.@id.text().toInteger()
	sendMessages(disruptionId, disruption.INFO.TEXT.text(), GcmUser.findAll { lastReportedDisruptionId < disruptionId })
}

def sendMessages(disruptionId, message, users) {
	users.each { user ->
		log.error "sending a message to user: " + user.uuid
		gcmService.sendMessage(user, message)
		user.lastReportedDisruptionId = disruptionId
		user.save()
	}
}

}