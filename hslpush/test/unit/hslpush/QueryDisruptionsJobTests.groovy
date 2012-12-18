package hslpush

import static org.junit.Assert.*

import grails.test.mixin.*
import grails.test.mixin.support.*
import groovy.text.SimpleTemplateEngine

import hslpush.user.*


/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
// TODO Don't know why this in the default template
@TestMixin(GrailsUnitTestMixin)
@Mock([GcmUser, LineOfInterest])
class QueryDisruptionsJobTests {
	
	QueryDisruptionsJob job
	
	final MOCK_UUID = "mockuuid"
	final MOCK_REGID = "originelli-regid"
	static int userSequence;
	
    void setUp() {
		job = new QueryDisruptionsJob()
    }

    void tearDown() {
    }
	
	def AREA_DISRUPTION_XML = '''<?xml version="1.0" encoding="ISO-8859-1"?>
		<DISRUPTIONS time="2012-10-29T18:00:00" valid="1" cancelled="0">
			<DISRUPTION id="16164" type="2" source="2">
				<VALIDITY status="1" from="2012-10-29T17:02:00" to="2012-10-29T18:17:00"/>
					<INFO><TEXT lang="fi"><![CDATA[${message}]]></TEXT></INFO>
					<TARGETS>
						<LINETYPE id="${transportType}"/>
					</TARGETS>
			</DISRUPTION>
		</DISRUPTIONS>'''
	
	def EMPTY_TARGETS_DISRUPTION_XML = '''<?xml version="1.0" encoding="ISO-8859-1"?>
		<DISRUPTIONS time="2012-10-29T18:00:00" valid="1" cancelled="0">
			<DISRUPTION id="16164" type="2" source="2">
				<VALIDITY status="1" from="2012-10-29T17:02:00" to="2012-10-29T18:17:00"/>
					<INFO><TEXT lang="fi"><![CDATA[${message}]]></TEXT></INFO>
					<TARGETS/>
			</DISRUPTION>
		</DISRUPTIONS>'''

    void testGeneralDisruptionSignaledWhenTransportTypeMatch() {
		
		final GENERAL_DISRUPTION_TRANSPORT_TYPE = 1
		def AREA_DISRUPTION = ["transportType":GENERAL_DISRUPTION_TRANSPORT_TYPE, "message":"area message"]
				
		def user1 = createUser([new LineOfInterest(code: "195X", transportType: 5)])
		def user2 = createUser([new LineOfInterest(code: "457", transportType: GENERAL_DISRUPTION_TRANSPORT_TYPE)])

		def gcmServiceControl = mockFor(GcmService)
		gcmServiceControl.demand.sendMessage {GcmUser user, String message -> 
			assert user == user2
			assert message == AREA_DISRUPTION["message"]	
		}
		
		job.gcmService = gcmServiceControl.createMock()
		
		job.hslDisruptionUrl = getXmlStringReader(AREA_DISRUPTION_XML, AREA_DISRUPTION)
		job.execute()
		
		gcmServiceControl.verify()
		
    }
	
	void testLineDisruptionSignaledWhenLineAndTransportTypeMatch() {
		
		def DESIRED_DISRUPTION = [ "code":"7B", "transportType":8, "message":"correct message here"]
		
		def DISRUPTION_XML = '''<?xml version="1.0" encoding="ISO-8859-1"?>
		<DISRUPTIONS time="2012-10-29T18:00:00" valid="1" cancelled="0">
			<DISRUPTION id="16164" type="2" source="2">
				<VALIDITY status="1" from="2012-10-29T17:02:00" to="2012-10-29T18:17:00"/>
					<INFO><TEXT lang="fi"><![CDATA[${message}]]></TEXT></INFO>
					<TARGETS>
						<LINE id="${code}" direction="2" linetype="${transportType}">14</LINE>
					</TARGETS>
			</DISRUPTION>
		</DISRUPTIONS>'''
		
		def user1 = createUser(new LineOfInterest(code:"457", transportType: 7))
		def user2 = createUser(new LineOfInterest(code:"7B", transportType: 8))
		def user3 = createUser(new LineOfInterest(code:"7B", transportType: 3))
		
		def gcmServiceControl = mockFor(GcmService)
		gcmServiceControl.demand.sendMessage {GcmUser user, String message ->
			assert user == user2
			assert message == DESIRED_DISRUPTION["message"]
		}
		
		job.gcmService = gcmServiceControl.createMock()
		
		job.hslDisruptionUrl = getXmlStringReader(DISRUPTION_XML, DESIRED_DISRUPTION)
		job.execute()
		
		gcmServiceControl.verify()
	}
	
	void testAreaDisruptionNotSignaledWhenDisruptionIdHasPassed() {
		fail "implement me"
	}
	
	void testLineDisruptionNotSignaledWhenDisruptionIdHasPassed() {
		fail "implement me"
	}
	
	void testAllUsersSignaledWhenGeneralDisruptionOccurs() {
		fail "implement me"
	}
	
	void testAllUsersSignaledWhenNoTargetsAreSpecified() {
		def user1 = createUser([])
		def user2 = createUser([new LineOfInterest(code:"195", transportType: 8)])
		def user3 = createUser(
			[new LineOfInterest(code:"498", transportType: 7),
			new LineOfInterest(code:"75", transportType: 9),
			new LineOfInterest(code:"58", transportType: 1)])

		def DESIRED_MESSAGE = "message goes here"
		def gcmServiceControl = mockFor(GcmService)
		gcmServiceControl.demand.sendMessage(3) {GcmUser user, String message ->
			assert message == DESIRED_MESSAGE
		}
		

		job.hslDisruptionUrl = getXmlStringReader(EMPTY_TARGETS_DISRUPTION_XML, ["message": DESIRED_MESSAGE])
		job.gcmService = gcmServiceControl.createMock()
		job.execute()
		
		assert GcmUser.findAll().size() == 3
		
		gcmServiceControl.verify()
	}
	
	
	def getXmlStringReader(xml, values) {
		new StringReader(new SimpleTemplateEngine().createTemplate(xml).make(values).toString())
	}
	
	def createUser(lines) {
		int id = ++userSequence;
		GcmUser user = new GcmUser(uuid: MOCK_UUID + id, registrationId: MOCK_REGID + id, linesOfInterest: [], lastReportedDisruptionId: 0)
		lines.each { user.linesOfInterest << it }
		user.save()
		user
	}
	
}
