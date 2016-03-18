var jrEye= {
jrTakePhotos: function(successCallback, errorCallback) {
    console.log("jrTakePhotos");
    cordova.exec(
                 successCallback,
                 errorCallback,
                 "JREye",
                 "jrEyeTakePhotos",
                 []
                 );
    
}
    ,
 
jrScanPhotos: function(successCallback, errorCallback) {
    console.log("jrScanPhotos");
    cordova.exec(
                 successCallback,
                 errorCallback,
                 "JREye",
                 "jrEyeScanPhotos",
                 []
                 );
    
}
    
}


module.exports = jrEye;
