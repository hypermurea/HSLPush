package hslpush

import grails.plugins.springsecurity.Secured

class ConfigController {

	def springSecurityService
	def pushoverService
	
	static allowedMethods = [index:'GET',
							change:['POST']]
	
    def index() { 
		
		[user: springSecurityService.currentUser]
		
	}
	
	@Secured(["hasRole('ROLE_TWITTER')"])
	def change() {
		
		log.error "update called"
		
		def user = springSecurityService.currentUser
		user.pushoverId = params.pushoverId
		user.linesOfInterest = params.linesOfInterest

		user.save()

		pushoverService.send(user.pushoverId, "Muutit asetuksiasi, ja seuraat seuraavia linjoja: " + user.linesOfInterest)
				
		render "Tietosi on päivitetty: Pushover-tiliisi on lähetetty ilmoitus muutoksesta"
			
	}
	
	@Secured(["hasRole('ROLE_TWITTER')"])
	def test() {
		pushoverService.send(springSecurityService.currentUser.pushoverId, "HSLpush palvelusi on oikein konfiguroitu")
		render "Pushover notification sent to your phone"
	}
	
}
