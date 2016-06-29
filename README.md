#cordova-plugin-eyecamera

        插件名称：cordova-plugin-eyecamera

        功能描述：拍照，图片浏览 

        支持平台：安卓，iOS 

        用于：泰瑞眼科

        安装：cordova plugin add https://github.com/terrydr/com.terrydr.tdEye.git

        卸载：cordova plugin rm cordova-plugin-eyecamera
        
        示例:
        tdeye.tdTakePhotos('before', function(result) {
        }, function(error) {
            console.log(error);
        });
