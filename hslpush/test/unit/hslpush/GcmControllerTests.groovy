package hslpush

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
	   def validLofJson = 
	   '[{"transportType":12,"name":"M-juna","code":"3002M","shortCode":"M"},{"transportType":2,"name":"J?tk?saari - T??l? - Arabia","code":"1008","shortCode":"8"},{"transportType":5,"name":"Viikki - Kumpula - Pasila - Otaniemi - Pohjois-Tapiola","code":"2506","shortCode":"506"},{"transportType":12,"name":"L-juna","code":"3002L","shortCode":"L"},{"transportType":12,"name":"L-juna","code":"3002LY2","shortCode":"L"}]'
    
	   params.uuid = "validUuid";
	   params.regId ="validRegistrationId";
	   params.lof = validLofJson;
	   
	   controller.index()
	   
	   
	   assert GcmUser.count() == 1
	   def user = GcmUser.findAll()[0]
	   assert LineOfInterest.count() == 5
	   assert user.linesOfInterest.size() == 5
	   
	   assert user.linesOfInterest.collect { it.code }.containsAll(
		   ["3002M", "1008", "2506", "3002L", "3002LY2"])
	   
	   LineOfInterest lof = LineOfInterest.find { code == "1008" }
	   assert lof.transportType == 2
	   
	   assert response.status == 200
	   assert response.text == "OK"
	   
	}
	
	@Test
	void whenLinesAddedUserIsUpdated() {
		GcmUser user = new GcmUser(uuid: MOCK_UUID, registrationId: ORIGINAL_REGID, linesOfInterest: [])
		user.linesOfInterest << new LineOfInterest(code: "1004", shortCode: "4", name: "Katajanokka - Munkkiniemi", transportType: 1)
		user.linesOfInterest << new LineOfInterest(code: "1064", shortCode: "64", name: "Rautatientori - It?-Pakila", transportType: 1)
		user.save()
		
		params.uuid = MOCK_UUID
		params.regId = NEW_REGID
		params.lof = '[{"transportType":2,"name":"Katajanokka - Munkkiniemi","code":"1004","shortCode":"4"},{"transportType":1,"name":"Rautatientori - It?-Pakila","code":"1064","shortCode":"64"},{"transportType":1,"name":"Lauttasaari-Rautatientori - L?nsi-Pakila","code":"1066A","shortCode":"66A"}]'
		controller.index()
	
		assert GcmUser.count() == 1
		assert LineOfInterest.count() == 3
		user = GcmUser.findAll()[0]
		assert user.linesOfInterest.size() == 3
		assert user.linesOfInterest.collect { it.code }.containsAll(["1004", "1064", "1066A"])
		assert user.registrationId == NEW_REGID
		
		assert response.status == 200
		assert response.text == "OK"
	}
	
	@Test
	void whenLinesRemovedUserIsUpdated() {
		GcmUser user = new GcmUser(uuid: MOCK_UUID, registrationId: ORIGINAL_REGID, linesOfInterest: [])
		user.linesOfInterest << new LineOfInterest(code: "1004", shortCode: "4", name: "Katajanokka - Munkkiniemi", transportType: 1)
		user.linesOfInterest << new LineOfInterest(code: "1064", shortCode: "64", name: "Rautatientori - It?-Pakila", transportType: 2)
		user.save()
		
		params.uuid = MOCK_UUID
		params.regId = NEW_REGID
		params.lof = '[{"transportType":1,"name":"Rautatientori - It?-Pakila","code":"1064","shortCode":"64"}]'
		controller.index()
	
		assert GcmUser.count() == 1
		assert LineOfInterest.count() == 1
		user = GcmUser.findAll()[0]
		assert user.linesOfInterest.size() == 1
		assert user.linesOfInterest.toArray()[0].code == "1064"
		assert user.linesOfInterest.toArray()[0].transportType == 1 
		assert user.registrationId == NEW_REGID
			
		assert response.status == 200
		assert response.text == "OK"
	}
	
	@Test
	void whenTransportTypeChangedUserIsUpdated() {
		GcmUser user = new GcmUser(uuid: MOCK_UUID, registrationId: ORIGINAL_REGID, linesOfInterest: [])
		user.linesOfInterest << new LineOfInterest(code: "2195", shortCode: "195",  name: "Elielinaukio-Latokaski", transportType: 1)
		user.save()
		
		params.uuid = MOCK_UUID
		params.regId = NEW_REGID
		params.lof = '[{"transportType":5,"name":"Elielinaukio-Latokaski","code":"2195","shortCode":"195"}]'
		controller.index()
	
		assert GcmUser.count() == 1
		assert LineOfInterest.count() == 1
		user = GcmUser.findAll()[0]
		assert user.linesOfInterest.size() == 1
		assert user.linesOfInterest.toArray()[0].code == "2195"
		assert user.linesOfInterest.toArray()[0].transportType == 5
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
		params.lof = 
		'[{"transportType":5,"name":"Elielinaukio-Latokaski","code":"2195","shortCode":"195"},{"transportType":5,"name":"Elielinaukio-Tapiola","code":"2194","shortCode":"194"}]'

		
		controller.index()
		
		assert GcmUser.count() == 1
		user = GcmUser.findAll()[0]
		assert user.registrationId == NEW_REGID
		assert user.uuid == MOCK_UUID
		assert LineOfInterest.count() == 2
		assert user.linesOfInterest.collect { it.code }.containsAll(["2195", "2194"])
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
		
		// TODO no assertions, is this supposed to test something?
				
	}
	
	
}
