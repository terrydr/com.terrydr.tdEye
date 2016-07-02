cordova-plugin-eyecamera
-------------------------------
####泰瑞眼科拍摄相机 cordova插件

插件名称：cordova-plugin-eyecamera

功能描述：眼科图像拍照，图片浏览 

支持平台：IOS Android

安装：cordova plugin add https://github.com/terrydr/com.terrydr.tdEye.git

卸载：cordova plugin rm cordova-plugin-eyecamera
        
示例:
        tdeye.tdTakePhotos('before', function(result) {
        }, function(error) {
            console.log(error);
        });
