package hslpush

import grails.test.mixin.*
import org.junit.*

import hslpush.user.GcmUser
import hslpush.user.LineOfInterest

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(GcmController)
@Mock([GcmUser, LineOfInterest])
class GcmControllerTests {
	
	final static String MOCK_UUID = "legal-uuid"
	final static String ORIGINAL_REGID = "originalregid"
	final static String NEW_REGID = "newregid"

	@Test
    void whenNewUserAddedDataIsInserted() {	   
	   def validLofJson = '[{"codes":["2195  1","2195  2"],"transportType":5,"name":"Elielinaukio-Latokaski","shortCode":"195"},{"codes":["1004T 1","1004T 2","1004T42","1004T41"],"transportType":2,"name":"Katajanokan terminaali - Munkkiniemi","shortCode":"4T"}]'
    
	   params.uuid = "validUuid";
	   params.regId ="validRegistrationId";
	   params.lof = validLofJson;
	   
	   controller.index()
	   
	   
	   assert GcmUser.count() == 1
	   def user = GcmUser.findAll()[0]
	   assert LineOfInterest.count() == 6
	   assert user.linesOfInterest.size() == 6
	   
	   assert user.linesOfInterest.collect { it.code }.containsAll(
		   ["2195  1", "2195  2", "1004T 1", "1004T 2", "1004T42", "1004T41"])
	   
	   LineOfInterest lof = LineOfInterest.find { code == "1004T 2" }
	   assert lof.transportType == 2
	   
	   assert response.status == 200
	   assert response.text == "OK"
	   
	}
	
	@Test
	void whenLinesAddedUserIsUpdated() {
		GcmUser user = new GcmUser(uuid: MOCK_UUID, registrationId: ORIGINAL_REGID, linesOfInterest: [])
		user.linesOfInterest << new LineOfInterest(code: "195X", transportType: 1)
		user.linesOfInterest << new LineOfInterest(code: "457", transportType: 7)
		user.save()
		
		params.uuid = MOCK_UUID
		params.regId = NEW_REGID
		params.lof = '[{"codes":["195X"],"transportType":2,"name":"Elielinaukio-Latokaski","shortCode":"195"}, {"codes":["457"],"transportType":2,"name":"Elielinaukio-Latokaski","shortCode":"457"}, {"codes":["3T"],"transportType":4,"name":"Elielinaukio-Latokaski","shortCode":"3T"}]'
		controller.index()
	
		assert GcmUser.count() == 1
		assert LineOfInterest.count() == 3
		user = GcmUser.findAll()[0]
		assert user.linesOfInterest.size() == 3
		assert user.linesOfInterest.collect { it.code }.containsAll(["195X", "457", "3T"])
		assert user.registrationId == NEW_REGID
		
		assert response.status == 200
		assert response.text == "OK"
	}
	
	@Test
	void whenLinesRemovedUserIsUpdated() {
		GcmUser user = new GcmUser(uuid: MOCK_UUID, registrationId: ORIGINAL_REGID, linesOfInterest: [])
		user.linesOfInterest << new LineOfInterest(code: "457", transportType: 7)
		def INITIAL_TRANSPORT_TYPE = 1
		user.linesOfInterest << new LineOfInterest(code: "195X", transportType: INITIAL_TRANSPORT_TYPE)
		user.save()
		
		params.uuid = MOCK_UUID
		params.regId = NEW_REGID
		params.lof = '[{"codes":["195X"],"transportType":2,"name":"Elielinaukio-Latokaski","shortCode":"195"}]'
		controller.index()
	
		assert GcmUser.count() == 1
		assert LineOfInterest.count() == 1
		user = GcmUser.findAll()[0]
		assert user.linesOfInterest.size() == 1
		assert user.linesOfInterest.toArray()[0].code == "195X"
		assert user.linesOfInterest.toArray()[0].transportType == 2 
		assert user.registrationId == NEW_REGID
			
		assert response.status == 200
		assert response.text == "OK"
	}
	
	@Test
	void whenTransportTypeChangedUserIsUpdated() {
		GcmUser user = new GcmUser(uuid: MOCK_UUID, registrationId: ORIGINAL_REGID, linesOfInterest: [])
		user.linesOfInterest << new LineOfInterest(code: "195X", transportType: 1)
		user.linesOfInterest << new LineOfInterest(code: "457", transportType: 7)
		user.save()
		
		params.uuid = MOCK_UUID
		params.regId = NEW_REGID
		params.lof = '[{"codes":["195X"],"transportType":2,"name":"Elielinaukio-Latokaski","shortCode":"195"}]'
		controller.index()
	
		assert GcmUser.count() == 1
		assert LineOfInterest.count() == 1
		user = GcmUser.findAll()[0]
		assert user.linesOfInterest.size() == 1
		assert user.linesOfInterest.toArray()[0].code == "195X"
		assert user.linesOfInterest.toArray()[0].transportType == 2
		assert user.registrationId == NEW_REGID
		
		assert response.status == 200
		assert response.text == "OK"
	}
	
	@Test
	void whenUserWithNoInterestsChangedUserIsUpdated() {
		params.uuid = MOCK_UUID
		params.regId = NEW_REGID
		params.lof = "[]"
		
		controller.index()
		
		assert GcmUser.count() == 1
		def user = GcmUser.findAll()[0]
		assert user.registrationId == NEW_REGID
		assert user.uuid == MOCK_UUID
		assert LineOfInterest.count() == 0
		assert response.status == 200
		assert response.text == "OK"
		
		response.reset()
		params.lof = '[{"codes":["2194  1","2194  2"],"transportType":5,"name":"Elielinaukio-Tapiola","shortCode":"194"}]'
		
		controller.index()
		
		assert GcmUser.count() == 1
		user = GcmUser.findAll()[0]
		assert user.registrationId == NEW_REGID
		assert user.uuid == MOCK_UUID
		assert LineOfInterest.count() == 2
		assert user.linesOfInterest.collect { it.code }.containsAll(["2194  1", "2194  2"])
		assert response.status == 200
		assert response.text == "OK"
	}
	
	@Test
	void interestedUsersSignaledOnCode() {
		
		GcmUser user = new GcmUser(uuid: MOCK_UUID, registrationId: ORIGINAL_REGID, linesOfInterest: [])
		user.linesOfInterest << new LineOfInterest(code: "195", transportType: 1)
		user.save()
		GcmUser user2 = new GcmUser(uuid: MOCK_UUID + "2", registrationId: ORIGINAL_REGID + "2", linesOfInterest: [])
		user2.linesOfInterest << new LineOfInterest(code: "195", transportType: 1)
		user2.linesOfInterest << new LineOfInterest(code: "505", transportType: 7)
		params.code = "505"
		user2.save()
		
		controller.signal()
	
		
	}
	
	
}
