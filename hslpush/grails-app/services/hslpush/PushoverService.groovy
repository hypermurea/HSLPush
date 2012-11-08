package hslpush

class PushoverService {

	def send(pushoverId, message) {
		withHttp(uri: "https://api.pushover.net") {
			headers.Accept = 'application/json'
			def http = post(path : '/1/messages.json',
			query : [ token : grailsApplication.config.hslpush.pushover.appId, user : pushoverId, message : message])
		}
	}
}
