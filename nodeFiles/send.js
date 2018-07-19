
var admin = require("firebase-admin");
var request = require('request');

const dbChild_notification = 'notificationRequests';
const API_KEY = "AAAAkbYOhA8:APA91bE0DG-upwIS9iphX80FDCeTS5UZH1tUyRRJXuWgNlv2QfMZIK-f7qIvnMsG1gYxB9PqO741XcZh_DIM47NeVOY02ijBLBZ3oDktQdahaKoHS6mBf3CY-3jw6_oVUfAYFkcbilMq"; // FCM server API key

var serviceAccount = require("C:/Users/leong/AndroidStudioProjects/EatLah/node_modules/eatlah-fe598-firebase-adminsdk-va9vx-86923cbae5.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://eatlah-fe598.firebaseio.com"
});
ref = admin.database().ref();

function listenForNotificationRequests() {
  var requests = ref.child(dbChild_notification);
  requests.on('child_added', function(requestSnapshot) {
    var request = requestSnapshot.val();
    sendNotificationToUser(
      request.uid,
      request.messageTitle,
      request.messageBody,
      request.is_background,
      request.timestamp,
      function() {
        // remove the pushed notification upon completion.
        console.log(JSON.stringify(requestSnapshot) + " onSuccess");
        requests.child(requestSnapshot.key).remove();
        console.log("removed requestSnapshot from db successfully");
      }
    );
  }, function(err) {
    console.error(err);
  });
};

/**
 * sends a notification to user_id
 */
function sendNotificationToUser(user_id, message_title, message_body, _is_background, _timestamp, onSuccess) {
  request({
    url: 'https://fcm.googleapis.com/fcm/send',
    method: 'POST',
    headers: {
      'Content-Type' :' application/json',
      'Authorization': 'key='+API_KEY
    },
    body: JSON.stringify({
      data: {
        messageTitle: message_title,
        messageBody: message_body,
        is_background: _is_background,
        timestamp: _timestamp,
        payload: {
          notification: {
            title: message_title,
            body: message_body
          },
          data: {
            title: message_title,
            body: message_body
          }
        }
      },
      to : '/topics/' + user_id,
    })
  }, function(error, response, body) {
    if (error) { console.error(error); }
    else if (response.statusCode >= 400) {
      console.error('HTTP Error: '+response.statusCode+' - '+response.statusMessage);
    }
    else {
      onSuccess();
    }
  });
}

// start listening
listenForNotificationRequests();
