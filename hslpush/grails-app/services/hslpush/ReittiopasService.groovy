package hslpush

import static groovyx.net.http.ContentType.JSON

class ReittiopasService {

	def BASE_URL = 'http://api.reittiopas.fi'
	def PATH = '/hsl/prod'

	def grailsApplication

	def findLinesByName(queryStrings) {

		withHttp(uri: BASE_URL, contentType: JSON) {
			headers.Accept = 'application/json'
			def http = get(path : PATH,
			query : [ 	
				request : 'lines',
				format : 'json',
				query : queryStrings.join('|'),
				user : grailsApplication.config.reittiopas.api.userId,
				pass : grailsApplication.config.reittiopas.api.password ]
			) { resp, json ->
				
				return json
			}

		}
	}

}
