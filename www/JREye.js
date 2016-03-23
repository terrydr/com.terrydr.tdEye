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
 
jrSelectPhotos: function(successCallback, errorCallback) {
    console.log("jrSelectPhotos");
    cordova.exec(
                 successCallback,
                 errorCallback,
                 "JREye",
                 "jrEyeSelectPhotos",
                 []
                 );
    
}
   ,
               
jrScanPhotos: function(paramDic,successCallback, errorCallback) {
    console.log("jrScanPhotos");
    cordova.exec(
                 successCallback,
                 errorCallback,
                 "JREye",
                 "jrEyeScanPhotos",
                 [paramDic]
                 );
    
}
    
}

module.exports = jrEye;