var jrEye= {
jrTakePhotos: function(successCallback, errorCallback) {
    console.log("invoked");
    cordova.exec(
                 successCallback,
                 errorCallback,
                 "JREye",
                 "jrEyeTakePhotos",
                 []
                 );
    
}
    
}


module.exports = jrEye;
