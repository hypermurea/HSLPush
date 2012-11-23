<%@page import="org.joda.time.*"%>
<!doctype html>
<html>
<head>
<meta name="layout" content="hslpush" />
<title>HSLPush</title>
<g:javascript library="jquery" />

<g:javascript>
	function showPopup() {
		$('#popupUpdated').popup('open').delay(600).popup('close');
	}
</g:javascript>

</head>
<body>

	<div data-role="page">

		<div data-role="header">
			<h1>HSLPush</h1>
			<!-- <img src="${resource(dir: 'images', file: 'hslpushlogo.png')}"
				alt="HSL" style="float: left" />
			-->
		</div>


		<div data-role="content">

			<p>Käyttääksesi HSLPush-palvelua tarvitset puhelimeesi
				Pushover-sovelluksen. Se on saatavilla iPhonelle ja Androidille.</p>

			<div class="ui-grid-a">
				<div class="ui-block-a">
					<a class="ui-link"
						href="https://itunes.apple.com/fi/app/pushover-notifications/id506088175?mt=8&uo=4"
						target="itunes_store"><img
						src="http://r.mzstatic.com/images/web/linkmaker/badge_appstore-lrg.gif"
						alt="Pushover Notifications - Superblock (iPhone)"
						style="border: 0;" /></a>
				</div>
				<div class="ui-block-b">
					<a
						href="http://play.google.com/store/apps/details?id=net.superblock.pushover"><img
						src="${resource(dir: 'images', file: 'get_it_on_play_logo_small.png')}" /></a>
				</div>
			</div>

			<sec:ifNotLoggedIn>
				<p>Kirjaudu palveluun Twitter-tunnuksillasi, jotta voit
					tallentaa joukkoliikenteen linjat, joista haluat tiedotuksia</p>
				<div class="ui-grid-solo">
					<div class="ui-block-a">
						<twitterAuth:button />
					</div>
				</div>
			</sec:ifNotLoggedIn>

			<sec:ifAnyGranted roles="ROLE_TWITTER">

				<g:formRemote name="updateForm" method="POST"
					on404="alert('server unavailable. please try again at a later time')"
					url="[controller:'config', action:'change']" onComplete="showPopup()" data-ajax="false">

					<div data-role="fieldcontain">
						<label for="name">Pushover ID:</label>
						<g:textField name="pushoverId" value="${user.pushoverId}" />
					</div>

					<div data-role="fieldcontain">
						<label for="name">Joukkoliikenteen linjat:</label>
						<g:textField name="linesOfInterest"
							value="${user.linesOfInterest}"/>
					</div>

					<g:actionSubmit value="Päivitä tietosi"/>

				</g:formRemote>
				
				<div data-role="popup" id="popupUpdated">
					<p>Tietosi on päivitetty. Sinulle on lähetetty Pushover-viesti varmistuksena.<p>
				</div>
			</sec:ifAnyGranted>
		</div>

		<div data-role="footer"></div>

	</div>
</body>
</html>
