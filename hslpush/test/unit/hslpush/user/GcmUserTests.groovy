package hslpush.user

import grails.test.mixin.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(GcmUser)
class GcmUserTests {
	
    void testSearchingWithMultipleLineCodesReturnsResults() {
		getUsers().each { it.save() }
		
		def c = GcmUser.createCriteria()
		def result = c.list {
			lt("lastReportedDisruptionId", 100)
			linesOfInterest {
				or {
					eq("code", "505")
					eq("code", "59")
				}
			}
		}
		
		result.collect { it.uuid }.toSet() == ["uuid1", "uuid3"]
	
		
    }
	
	void testSearchingWithDynamicMultipleLineQueryReturnsResults() {

		getUsers().each { it.save() }
		
		def lines = ["505", "59"]
		def c = GcmUser.createCriteria()
		def result = c.list {
			lt("lastReportedDisruptionId", 100)
			linesOfInterest {
				or {
					System.err.println "testing whether closures are really code: " + lines
					lines.each {
						eq("code", it)
					}
				}
			}
		}
		
		result.collect { it.uuid }.toSet() == ["uuid1", "uuid3"]
	
		
	}
	
	def getUsers() {
		final int SAFE_DISRUPTION_ID = 0
		GcmUser user1 = new GcmUser(uuid:"uuid1", registrationId:"regId1", linesOfInterest:[], latestDisruptionId:SAFE_DISRUPTION_ID)
		user1.linesOfInterest << new LineOfInterest(code:"59", transportType:2)
		user1.linesOfInterest << new LineOfInterest(code:"194", transportType:1)
		GcmUser user2 = new GcmUser(uuid:"uuid2", registrationId:"regId2", linesOfInterest:[], latestDisruptionId:SAFE_DISRUPTION_ID)
		user2.linesOfInterest << new LineOfInterest(code:"5", transportType:3)
		user2.linesOfInterest << new LineOfInterest(code:"194", transportType:1)
		GcmUser user3 = new GcmUser(uuid:"uuid3", registrationId:"regId3", linesOfInterest:[], latestDisruptionId:SAFE_DISRUPTION_ID)
		user3.linesOfInterest << new LineOfInterest(code:"505", transportType:3)
		user3.linesOfInterest << new LineOfInterest(code:"57", transportType:2)
		
		return [user1, user2, user3]
	}
	
	
}
