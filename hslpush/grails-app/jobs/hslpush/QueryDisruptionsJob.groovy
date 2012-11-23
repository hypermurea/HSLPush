package hslpush

import groovyx.net.http.HttpResponseException
import hslpush.user.GcmUser

class QueryDisruptionsJob {

	static triggers = { simple repeatInterval: 30000l // execute job once in 30 seconds
	}

	def gcmService
	def pushoverService

	static PUSHOVER_APP_ID = "1IKVsQLaRKEnGNh4McWccaPLlZPMO2"

	def execute() {

		//def HSL_DISRUPTION_URL = "http://www.poikkeusinfo.fi/xml/v2/fi/291020121800"
		def HSL_DISRUPTION_URL = "http://www.poikkeusinfo.fi/xml/v2/fi"
		def disruptionReport = new XmlSlurper().parse(HSL_DISRUPTION_URL)
		log.error "disruptions in effect: " + disruptionReport.@valid

		if (disruptionReport.@valid.text().toInteger() > 0) {

			def disruptions = disruptionReport.DISRUPTION.findAll {
				it.VALIDITY.@status.text().toInteger() == 1
			}.list()
			disruptions.sort{
				it.@id.text().toInteger()
			} // assumes that disruptions follow an increasing id pattern

			disruptions.each { disruption ->

				def users = GcmUser.find { lastReportedDisruptionId < disruption.@id.text().toInteger() }
				//def users = SecurityUser.findAll()
				
				users.each { user ->
					
					def linesOfInterestCodes = user.linesOfInterest.collect { it.code } 
					
					boolean pushWarranted = false;
					if(disruption.TARGETS.LINE.size() > 0) {
						disruption.TARGETS.LINE.each { line ->
							log.error "checking line against user preferences: " + line.text() + ", lines of interest: " + linesOfInterestCodes
							if(linesOfInterestCodes.contains(line.text())) {
								log.error("push warranted")
								pushWarranted = true;
							}
						}
					} else {
						pushWarranted = true;
					}
					
					log.error("report: " + disruption.INFO.TEXT.text())

					if(pushWarranted) {
						// multicast message would prob work better?
						gcmService.sendMessage(user, disruption.INFO.TEXT.text())
						//pushoverService.send(user.pushoverId, disruption.INFO.TEXT.text())
						user.lastReportedDisruptionId = disruption.@id.text().toInteger()
					}
					
					user.save()
					
				}
			}


		} else {
			log.error "no disruptions currently in effect"
		}
	}
	
	
	/**
	def execute() {
		
				//def HSL_DISRUPTION_URL = "http://www.poikkeusinfo.fi/xml/v2/fi/291020121800"
				def HSL_DISRUPTION_URL = "http://www.poikkeusinfo.fi/xml/v2/fi"
				def disruptionReport = new XmlSlurper().parse(HSL_DISRUPTION_URL)
				log.error "disruptions in effect: " + disruptionReport.@valid
		
				if (disruptionReport.@valid.text().toInteger() > 0) {
		
					def disruptions = disruptionReport.DISRUPTION.findAll {
						it.VALIDITY.@status.text().toInteger() == 1
					}.list()
					disruptions.sort{
						it.@id.text().toInteger()
					} // assumes that disruptions follow an increasing id pattern
		
					disruptions.each { disruption ->
		
						def users = SecurityUser.find { lastReportedDisruptionId < disruption.@id.text().toInteger() }
						//def users = SecurityUser.findAll()
						
						users.each { user ->
							
							def lines = new String(user.linesOfInterest)
							lines = lines.replace(' ', ',')
							def lineCollection = lines.tokenize(",")
							
							boolean pushWarranted = false;
							if(disruption.TARGETS.LINE.size() > 0) {
								disruption.TARGETS.LINE.each { line ->
									log.error "checking line against user preferences: " + line.text() + ", linecollection: " + lineCollection.toArray()
									if(lineCollection.contains(line.text())) {
										log.error("push warranted")
										pushWarranted = true;
									}
								}
							} else {
								pushWarranted = true;
							}
							
							log.error("report: " + disruption.INFO.TEXT.text())
		
							if(pushWarranted) {
								// multicast message would prob work better?
								androidGcmService.sendInstantMessage([test:disruption.INFO.TEXT.text()], [user.pushoverId])
								//pushoverService.send(user.pushoverId, disruption.INFO.TEXT.text())
								user.lastReportedDisruptionId = disruption.@id.text().toInteger()
							}
							
							user.save()
							
						}
					}
		
		
				} else {
					log.error "no disruptions currently in effect"
				}
			}
	
	*/
	

}