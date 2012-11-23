package hslpush

import static org.junit.Assert.*
import org.junit.*

class ReittiopasTests {

	def reittiopasService
	
    @Before
    void setUp() {
        reittiopasService.grailsApplication = new org.codehaus.groovy.grails.commons.DefaultGrailsApplication()
    }

    @After
    void tearDown() {
        // Tear down logic here
    }

    @Test
    void testFindLinesByName() {
		
		def jsonResponse = reittiopasService.findLinesByName(['4T', '195', '506', '505'])

		jsonResponse.each {
			log.error it.code_short + " : " + it.timetable_url
		}

		fail "it should"
        
    }
}
