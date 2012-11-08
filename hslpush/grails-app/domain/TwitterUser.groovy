import com.the6hours.grails.springsecurity.twitter.TwitterUserDomain
import hslpush.user.SecurityUser

class TwitterUser implements TwitterUserDomain {

    int uid
    String screenName
    String tokenSecret
    String token

	static belongsTo = [user: SecurityUser]

	static constraints = {
		uid unique: true
	}
}
