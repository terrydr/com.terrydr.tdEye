var jrEye= {
jrTakePhotos: function(takeType, successCallback, errorCallback) {
    console.log("jrTakePhotos");
    cordova.exec(
                 successCallback,
                 errorCallback,
                 "JREye",
                 "jrEyeTakePhotos",
                 [takeType]
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
