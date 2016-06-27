var tdEye= {
tdTakePhotos: function(takeType, successCallback, errorCallback) {
    console.log("tdTakePhotos");
    cordova.exec(
                 successCallback,
                 errorCallback,
                 "TDEye",
                 "tdEyeTakePhotos",
                 [takeType]
                 );
    
}
    ,
 
tdSelectPhotos: function(successCallback, errorCallback) {
    console.log("tdSelectPhotos");
    cordova.exec(
                 successCallback,
                 errorCallback,
                 "TDEye",
                 "tdEyeSelectPhotos",
                 []
                 );
    
}
   ,
               
tdScanPhotos: function(paramDic,successCallback, errorCallback) {
    console.log("tdScanPhotos");
    cordova.exec(
                 successCallback,
                 errorCallback,
                 "TDEye",
                 "tdEyeScanPhotos",
                 [paramDic]
                 );
    
}
    
}

module.exports = tdEye;