<%@page import="org.joda.time.*"%>
<!doctype html>
<html>
<head>
<meta name="layout" content="hslpush" />
<title>HSLPush</title>
<g:javascript library="jquery" />


<g:javascript>
function fadeStatus() {
			$('#updateReport').fadeOut(1600, "linear", function() {});
}
</g:javascript>
</head>
<body>

	<div style="width: 800px;">

		<div style="height: 200px">
			<img src="${resource(dir: 'images', file: 'hslpushlogo.png')}"
				alt="HSL" style="float: left" />
			<div style="font-size: 16px; font-family: Helvetica, sans-serif">
			</div>
		</div>


		<div style="clear: both; width: 800px; height: 100px">
			<div style="width: 400px; height: 100px; float: left">
				<h3>Asenna Pushover puhelimeesi</h3>
			</div>
			<div>
				<a
					href="https://itunes.apple.com/fi/app/pushover-notifications/id506088175?mt=8&uo=4"
					target="itunes_store"><img
					src="http://r.mzstatic.com/images/web/linkmaker/badge_appstore-lrg.gif"
					alt="Pushover Notifications - Superblock (iPhone)"
					style="border: 0;" /></a> <a
					href="http://play.google.com/store/apps/details?id=net.superblock.pushover"><img
					src="${resource(dir: 'images', file: 'get_it_on_play_logo_small.png')}" /></a>
			</div>
		</div>

		<sec:ifNotLoggedIn>

			<div style="clear: both; width: 800px; height: 100px">
				<div style="width: 400px; height: 100px; float: left">
					<h3>Kirjaudu Twitter-tiliäsi käyttäen</h3>
				</div>
				<div>
					<twitterAuth:button />
				</div>
			</div>
		</sec:ifNotLoggedIn>

		<sec:ifAnyGranted roles="ROLE_TWITTER">


			<g:formRemote name="updateForm" method="POST"
				on404="alert('server unavailable. please try again at a later time')"
				update="updateReport" url="[controller:'config', action:'change']"
				after="fadeStatus()">

				<div style="clear: both; width: 800px; height: 100px">

					<div style="width: 400px; height: 100px; float: left">
						<h3>Syötä Pushover-tilisi tunniste</h3>
					</div>
					<div>
						<g:textField name="pushoverId" value="${user.pushoverId}"
							size="35" style="font-size:18px"/>
					</div>


				</div>

				<div style="clear: both; width: 800px; height: 100px">
					<div style="width: 400px; height: 100px; float: left">
						<h3 style="vertical-align:top">Julkisen liikenteen linjat, joista haluat
							tiedotuksia</h3>
					</div>
					<div>
						<g:textField name="linesOfInterest" size="35" value="${user.linesOfInterest}" style="font-size:18px"/>
					</div>
				</div>

				<div style="clear: both; width: 800px; height: 100px">
					<div style="width: 400px; height: 100px; float: left">
					</div>
					<div>
						<g:actionSubmit value="Päivitä tietosi" style="font-size:18px"/>
					</div>
				</div>

			</g:formRemote>

			<div id="updateReport"></div>

	</sec:ifAnyGranted>

	</div>
</body>
</html>
